package com.deepblue.rescue;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(
                    "postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");

    @Autowired
    private RescueCenterRepository centerRepository;

    @Autowired
    private RescueCaseRepository caseRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldEstablishConnectionAndVerifyContext() {
        Assertions.assertTrue(postgres.isRunning());
        Assertions.assertNotNull(animalRepository);
    }

    @Test
    void shouldSaveAndFindAnimalByCode() {
        RescueCenter center = new RescueCenter();
        center.setCode("CTR-01");
        center.setName("Central Rescue");
        center = centerRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("CASE-01");
        rescueCase.setStatus(RescueStatus.ACTIVE);
        rescueCase.setRescueCenter(center);
        rescueCase = caseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("ANM-01");
        animal.setCommonName("Puma");
        animal.setRescueCase(rescueCase);
        animalRepository.save(animal);

        var found = animalRepository.findByAnimalCode("ANM-01");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Puma", found.get().getCommonName());
    }

    @Test
    void shouldFindAnimalsByCommonNameIgnoreCase() {
        RescueCenter center = new RescueCenter();
        center.setCode("CTR-02");
        center.setName("Wildlife Center");
        center = centerRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("CASE-02");
        rescueCase.setStatus(RescueStatus.ACTIVE);
        rescueCase.setRescueCenter(center);
        rescueCase = caseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("ANM-02");
        animal.setCommonName("Golden Eagle");
        animal.setRescueCase(rescueCase);
        animalRepository.save(animal);

        var foundList = animalRepository.findByCommonNameContainingIgnoreCase("golden");
        Assertions.assertFalse(foundList.isEmpty());
        Assertions.assertEquals("Golden Eagle", foundList.get(0).getCommonName());
    }

    @Test
    void shouldFindAnimalsByRescueCaseStatus() {
        RescueCenter center = new RescueCenter();
        center.setCode("CTR-03");
        center.setName("Sanctuary Center");
        center = centerRepository.save(center);

        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("CASE-03");
        rescueCase.setStatus(RescueStatus.RESOLVED);
        rescueCase.setRescueCenter(center);
        rescueCase = caseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("ANM-03");
        animal.setCommonName("Sea Turtle");
        animal.setRescueCase(rescueCase);
        animalRepository.save(animal);

        var foundList = animalRepository.findByRescueCaseStatus(RescueStatus.RESOLVED);
        Assertions.assertFalse(foundList.isEmpty());
        Assertions.assertEquals("ANM-03", foundList.get(0).getAnimalCode());
    }

    @Test
    void shouldFindActiveSpecialistsByExpertise() {
        Expertise expertise = new Expertise();
        expertise.setName("Wildlife Rescue");
        expertise = expertiseRepository.save(expertise);

        Specialist specialist = new Specialist();
        specialist.setFirstName("Jane");
        specialist.setLastName("Doe");
        specialist.setActive(true);
        specialist.getExpertiseAreas().add(expertise);
        specialistRepository.save(specialist);

        var foundList = specialistRepository.findActiveByExpertise("Wildlife Rescue");
        Assertions.assertFalse(foundList.isEmpty());
        Assertions.assertEquals("Doe", foundList.get(0).getLastName());
    }
}