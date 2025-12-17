package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.dto.CreateCocktailRequestDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.entity.Preparazione;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CocktailService - Logica di business
 * Contiene CRUD e conversione Entity ↔ DTO
 */
@Service
public class CocktailService {

    @Autowired
    private CocktailRepository cocktailRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private PreparazioneRepository preparazioneRepository;

    // Ottieni tutti i cocktail
    public List<CocktailDTO> getAllCocktails() {
        return cocktailRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Ottieni un cocktail per ID
    public Optional<CocktailDTO> getCocktailById(Long id) {
        return cocktailRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Cerca cocktail per nome
    public List<CocktailDTO> searchByName(String nome) {
        return cocktailRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Crea un nuovo cocktail con ingredienti e preparazione
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

   

    // Aggiorna un cocktail esistente
    public Optional<CocktailDTO> updateCocktail(Long id, CocktailDTO cocktailDTO) {
        return cocktailRepository.findById(id)
                .map(existing -> {
                    existing.setNome(cocktailDTO.getNome());
                    existing.setDescrizione(cocktailDTO.getDescrizione());
                    existing.setTempoPreparazioneMinutes(cocktailDTO.getTempoPreparazioneMinutes());
                    existing.setNote(cocktailDTO.getNote());
                    Cocktail updated = cocktailRepository.save(existing);
                    return convertToDTO(updated);
                });
    }

    // Elimina un cocktail
    @Transactional
    public boolean deleteCocktail(Long id) {
        if (cocktailRepository.existsById(id)) {
            preparazioneRepository.deleteByCocktailId(id);
            cocktailRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Converte Entity in DTO (aggiunge gli step di ricetta)
    private CocktailDTO convertToDTO(Cocktail cocktail) {
        CocktailDTO dto = new CocktailDTO();
        dto.setId(cocktail.getId());
        dto.setNome(cocktail.getNome());
        dto.setDescrizione(cocktail.getDescrizione());
        dto.setTempoPreparazioneMinutes(cocktail.getTempoPreparazioneMinutes());
        dto.setNote(cocktail.getNote());
        
        // Carica gli step ordinati
        List<Preparazione> steps = preparazioneRepository.findByCocktailIdOrderByStepOrderAsc(cocktail.getId());
        
        // Converte ogni step in StepPreparazioneDTO
        List<CocktailDTO.StepPreparazioneDTO> stepsDTO = steps.stream()
                .map(step -> {
                    String nomeIngrediente = ingredienteRepository.findById(step.getIngredienteId())
                            .map(Ingrediente::getNome)
                            .orElse("Ingrediente sconosciuto");
                    
                    return new CocktailDTO.StepPreparazioneDTO(
                            step.getStepOrder(),
                            nomeIngrediente,
                            step.getQuantita() != null ? step.getQuantita().toString() : null,
                            step.getUnita(),
                            step.getIstruzione()
                    );
                })
                .collect(Collectors.toList());
        
        dto.setPreparazione(stepsDTO);
        return dto;
    }

    
}