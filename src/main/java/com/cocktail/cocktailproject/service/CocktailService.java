package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.entity.Preparazione;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    // Crea un nuovo cocktail
    public CocktailDTO createCocktail(CocktailDTO cocktailDTO) {
        Cocktail cocktail = convertToEntity(cocktailDTO);
        Cocktail saved = cocktailRepository.save(cocktail);
        return convertToDTO(saved);
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

    // Converte DTO in Entity
    private Cocktail convertToEntity(CocktailDTO dto) {
        Cocktail cocktail = new Cocktail();
        cocktail.setNome(dto.getNome());
        cocktail.setDescrizione(dto.getDescrizione());
        cocktail.setTempoPreparazioneMinutes(dto.getTempoPreparazioneMinutes());
        cocktail.setNote(dto.getNote());
        return cocktail;
    }
}