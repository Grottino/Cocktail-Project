package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.dto.CreateCocktailRequestDTO;
import com.cocktail.cocktailproject.dto.IngredientiDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.entity.Preparazione;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import com.cocktail.cocktailproject.repository.UserFavoritoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CocktailService - Logica di business per gestione cocktail
 * 
 * Responsabilità:
 * - Operazioni CRUD (Create, Read, Update, Delete) sui cocktail
 * - Conversione tra Entity (database) e DTO (client)
 * - Validazione dei dati in ingresso
 * - Gestione ingredienti e step di preparazione
 * - Cancellazioni cascata (preparazione + favoriti)
 */
@Service
public class CocktailService {

    private final CocktailRepository cocktailRepository;
    private final IngredienteRepository ingredienteRepository;
    private final PreparazioneRepository preparazioneRepository;
    private final UserFavoritoRepository userFavoritoRepository;

    // Constructor Injection (Spring 4.3+): più testabile, immutabile e esplicito
    public CocktailService(
            CocktailRepository cocktailRepository,
            IngredienteRepository ingredienteRepository,
            PreparazioneRepository preparazioneRepository,
            UserFavoritoRepository userFavoritoRepository) {
        this.cocktailRepository = cocktailRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.preparazioneRepository = preparazioneRepository;
        this.userFavoritoRepository = userFavoritoRepository;
    }

    /**
     * Ottieni tutti i cocktail con paginazione (metodo raccomandato)
     * @param pageable Parametri di paginazione (page, size, sort)
     * @return Pagina di cocktail con metadata (totalElements, totalPages, etc.)
     */
    public Page<CocktailDTO> getAllCocktails(Pageable pageable) {
        return cocktailRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Ottieni un cocktail specifico per ID
     * @param id ID univoco del cocktail
     * @return Optional contenente il cocktail se trovato, altrimenti vuoto
     */
    public Optional<CocktailDTO> getCocktailById(Long id) {
        return cocktailRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Cerca cocktail per nome senza paginazione (versione legacy)
     * @param nome Stringa da cercare nel nome (case-insensitive, ricerca parziale)
     * Cerca cocktail per nome con paginazione (metodo raccomandato)
     * @param nome Stringa da cercare nel nome (case-insensitive, ricerca parziale)
     * @param pageable Parametri di paginazione
     * @return Pagina di cocktail che corrispondono alla ricerca
     */
    public Page<CocktailDTO> searchByName(String nome, Pageable pageable) {
        return cocktailRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Crea un nuovo cocktail completo con ingredienti e preparazione
     * 
     * Validazioni:
     * - Nome obbligatorio e non vuoto
     * - Minimo 2 ingredienti richiesti
     * - No ingredienti duplicati nella stessa richiesta
     * 
     * Comportamento:
     * - Gli ingredienti non esistenti vengono creati automaticamente
     * - I nomi ingredienti sono normalizzati (lowercase, trim)
     * - Gli step di preparazione sono numerati automaticamente
     * 
     * @param requestDTO Dati del cocktail con lista ingredienti e istruzioni
     * @return CocktailDTO del cocktail appena creato con tutti i dettagli
     * @throws IllegalArgumentException Se validazione fallisce
     */
    @Transactional
    public CocktailDTO createCocktail(CreateCocktailRequestDTO requestDTO) {
        // Validazione: verificare che ci siano almeno 2 ingredienti
        if (requestDTO.getIngredienti() == null || requestDTO.getIngredienti().size() < 2) {
            throw new IllegalArgumentException("Il cocktail deve contenere almeno 2 ingredienti");
        }
        
        // Validazione: nome non vuoto
        if (requestDTO.getNome() == null || requestDTO.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del cocktail non può essere vuoto");
        }
        
        // Crea l'entità Cocktail
        Cocktail cocktail = new Cocktail();
        cocktail.setNome(requestDTO.getNome().trim());
        cocktail.setDescrizione(requestDTO.getDescrizione());
        cocktail.setTempoPreparazioneMinutes(requestDTO.getTempoPreparazioneMinutes());
        cocktail.setNote(requestDTO.getNote());
        
        // Salva il cocktail
        Cocktail savedCocktail = cocktailRepository.save(cocktail);
        
        // Processa gli ingredienti
        Set<Long> processedIngredients = new HashSet<>();
        int stepOrder = 1;
        
        for (CreateCocktailRequestDTO.IngredienteStepDTO ingredienteStep : requestDTO.getIngredienti()) {
            // Normalizza il nome ingrediente (lowercase, trim)
            String nomeNormalizzato = ingredienteStep.getNome().trim().toLowerCase();
            
            // Verifica di non avere duplicati nella stessa richiesta
            if (!processedIngredients.isEmpty()) {
                for (Long ingredId : processedIngredients) {
                    Ingrediente existing = ingredienteRepository.findById(ingredId).orElse(null);
                    if (existing != null && existing.getNome().toLowerCase().equals(nomeNormalizzato)) {
                        throw new IllegalArgumentException("Ingrediente duplicato: " + ingredienteStep.getNome());
                    }
                }
            }
            
            // Cerca l'ingrediente nel database (case-insensitive)
            Optional<Ingrediente> existingIngrediente = ingredienteRepository.findByNomeIgnoreCase(nomeNormalizzato);
            
            Ingrediente ingrediente;
            if (existingIngrediente.isPresent()) {
                ingrediente = existingIngrediente.get();
            } else {
                // Crea un nuovo ingrediente
                ingrediente = new Ingrediente();
                ingrediente.setNome(nomeNormalizzato);
                ingrediente = ingredienteRepository.save(ingrediente);
            }
            
            processedIngredients.add(ingrediente.getId());
            
            // Crea lo step di preparazione
            Preparazione preparazione = new Preparazione();
            preparazione.setCocktailId(savedCocktail.getId());
            preparazione.setIngredienteId(ingrediente.getId());
            preparazione.setQuantita(ingredienteStep.getQuantita());
            preparazione.setUnita(ingredienteStep.getUnita());
            preparazione.setStepOrder(stepOrder);
            
            // Se la preparazione è fornita, usarla; altrimenti usare il default
            if (requestDTO.getPreparazione() != null && !requestDTO.getPreparazione().trim().isEmpty()) {
                preparazione.setIstruzione(requestDTO.getPreparazione().trim());
            } else {
                preparazione.setIstruzione("Mescolare gli ingredienti");
            }
            
            preparazioneRepository.save(preparazione);
            stepOrder++;
        }
        
        return convertToDTO(savedCocktail);
    }

    /**
     * Crea un nuovo cocktail (versione legacy senza ingredienti)
     * Usare createCocktail(CreateCocktailRequestDTO) per creare cocktail completi
     * 
     * @param cocktailDTO Dati base del cocktail
     * @return CocktailDTO del cocktail creato
     */
    @Transactional
    public CocktailDTO createCocktail(CocktailDTO cocktailDTO) {
        Cocktail cocktail = convertToEntity(cocktailDTO);
        Cocktail saved = cocktailRepository.save(cocktail);
        return convertToDTO(saved);
    }

    /**
     * Aggiorna un cocktail esistente (supporta aggiornamenti parziali)
     * 
     * Solo i campi non-null nel DTO vengono aggiornati.
     * Gli altri campi mantengono il valore precedente.
     * 
     * @param id ID del cocktail da aggiornare
     * @param cocktailDTO Dati da aggiornare (campi null vengono ignorati)
     * @return Optional contenente il cocktail aggiornato, o vuoto se non trovato
     */
    @Transactional
    public Optional<CocktailDTO> updateCocktail(Long id, CocktailDTO cocktailDTO) {
        return cocktailRepository.findById(id)
                .map(existing -> {
                    // Aggiorna solo i campi non-null ricevuti nel DTO
                    if (cocktailDTO.getNome() != null) {
                        existing.setNome(cocktailDTO.getNome());
                    }
                    if (cocktailDTO.getDescrizione() != null) {
                        existing.setDescrizione(cocktailDTO.getDescrizione());
                    }
                    if (cocktailDTO.getTempoPreparazioneMinutes() != null) {
                        existing.setTempoPreparazioneMinutes(cocktailDTO.getTempoPreparazioneMinutes());
                    }
                    if (cocktailDTO.getNote() != null) {
                        existing.setNote(cocktailDTO.getNote());
                    }
                    Cocktail updated = cocktailRepository.save(existing);
                    return convertToDTO(updated);
                });
    }

    /**
     * Elimina un cocktail e tutti i dati correlati (cancellazione cascata)
     * 
     * Sequenza di eliminazione:
     * 1. Elimina tutti i favoriti che puntano al cocktail
     * 2. Elimina tutti gli step di preparazione
     * 3. Elimina il cocktail stesso
     * 
     * Nota: gli ingredienti NON vengono eliminati (possono essere usati da altri cocktail)
     * 
     * @param id ID del cocktail da eliminare
     * @return true se eliminato con successo, false se non trovato
     */
    @Transactional
    public boolean deleteCocktail(Long id) {
        if (cocktailRepository.existsById(id)) {
            // Prima elimina i favoriti associati
            userFavoritoRepository.deleteByCocktailId(id);
            // Poi elimina la preparazione
            preparazioneRepository.deleteByCocktailId(id);
            // Infine elimina il cocktail
            cocktailRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Converte un'entity Cocktail in DTO per il client
     * 
     * Arricchisce i dati con:
     * - Step di preparazione ordinati
     * - Nome ingrediente per ogni step (join con tabella ingredienti)
     * 
     * @param cocktail Entity dal database
     * @return DTO completo pronto per il client
     */
    private CocktailDTO convertToDTO(Cocktail cocktail) {
        CocktailDTO dto = new CocktailDTO();
        dto.setId(cocktail.getId());
        dto.setNome(cocktail.getNome());
        dto.setDescrizione(cocktail.getDescrizione());
        dto.setTempoPreparazioneMinutes(cocktail.getTempoPreparazioneMinutes());
        dto.setNote(cocktail.getNote());
        
        // Carica gli step ordinati
        List<Preparazione> steps = preparazioneRepository.findByCocktailIdOrderByStepOrderAsc(cocktail.getId());
        
        // Carica tutti gli ingredienti una volta sola (evita N+1 query problem)
        Map<Long, String> ingredientiMap = new HashMap<>();
        for (Preparazione step : steps) {
            if (!ingredientiMap.containsKey(step.getIngredienteId())) {
                String nomeIngrediente = ingredienteRepository.findById(step.getIngredienteId())
                        .map(Ingrediente::getNome)
                        .orElse("Ingrediente sconosciuto");
                ingredientiMap.put(step.getIngredienteId(), nomeIngrediente);
            }
        }
        
        // Converte ogni step in StepPreparazioneDTO
        List<CocktailDTO.StepPreparazioneDTO> stepsDTO = steps.stream()
                .map(step -> new CocktailDTO.StepPreparazioneDTO(
                        step.getStepOrder(),
                        ingredientiMap.get(step.getIngredienteId()),
                        step.getQuantita() != null ? step.getQuantita().toString() : null,
                        step.getUnita(),
                        step.getIstruzione()
                ))
                .collect(Collectors.toList());
        
        dto.setPreparazione(stepsDTO);
        return dto;
    }

    /**
     * Converte un DTO in entity Cocktail (solo campi base)
     * @param dto DTO ricevuto dal client
     * @return Entity Cocktail pronta per il salvataggio
     */
    private Cocktail convertToEntity(CocktailDTO dto) {
        Cocktail cocktail = new Cocktail();
        cocktail.setNome(dto.getNome());
        cocktail.setDescrizione(dto.getDescrizione());
        cocktail.setTempoPreparazioneMinutes(dto.getTempoPreparazioneMinutes());
        cocktail.setNote(dto.getNote());
        return cocktail;
    }

    /**
     * Converte un'entity Ingrediente in DTO
     * @param ingrediente Entity dal database
     * @return DTO semplice con solo il nome
     */
    private IngredientiDTO convertToDTO(Ingrediente ingrediente) {
        return new IngredientiDTO(ingrediente.getNome());
    }

    /**
     * Ottieni tutti gli ingredienti disponibili con paginazione (metodo raccomandato)
     * @param pageable Parametri di paginazione
     * @return Pagina di ingredienti con metadata
     */
    public Page<IngredientiDTO> getAllIngredients(Pageable pageable) {
        return ingredienteRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
}