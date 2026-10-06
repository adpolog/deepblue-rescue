package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.AnimalService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalServiceImpl(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public AnimalResponse findByCode(String animalCode) {
        return animalRepository.findByAnimalCode(animalCode)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + animalCode));
    }

    @Override
    public List<AnimalResponse> findAnimalsInRehabilitation() {
        return animalRepository.findAnimalsInRehabilitation().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canReceiveTreatment(String animalCode) {
        Animal animal = animalRepository.findByAnimalCode(animalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + animalCode));
        return animal.getRescueCase().getStatus() != RescueStatus.RELEASED;
    }


    private AnimalResponse mapToResponse(Animal animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getAnimalCode(),
                animal.getCommonName(),
                animal.getScientificName(),
                animal.getSex(),
                animal.getTrackingDeviceCode()

        );
    }
}