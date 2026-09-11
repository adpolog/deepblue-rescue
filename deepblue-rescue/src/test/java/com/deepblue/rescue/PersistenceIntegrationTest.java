package com.deepblue.rescue;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
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
    void testFlywayMigrationsLoaded() {
        // Comprueba que las migraciones de Flyway se ejecutaron correctamente en la BD real
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version IN ('1', '2')",
                Integer.class
        );
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testRescueCenterBasicOperations() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
        centerRepository.save(center);

        Optional<RescueCenter> found = centerRepository.findByCode("DB-CAR");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("DeepBlue Caribbean");
        assertThat(centerRepository.count()).isGreaterThan(0);
    }

    @Test
    void testRetoIntegradorScenario() {
        // 1. Centro
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
        centerRepository.save(center);

        // 2. Caso
        RescueCase rescueCase = new RescueCase("RES-2026-100", LocalDate.of(2026, 8, 18), "Bahía Concha", RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase);
        caseRepository.save(rescueCase);

        // 3. Animal y Expediente Médico (1:1)
        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);

        MedicalRecord record = new MedicalRecord(BigDecimal.valueOf(27.80), "STABLE", "Injury caused by fishing net", "Possible plastic ingestion");
        animal.assignMedicalRecord(record);
        animalRepository.save(animal);

        // 4. Especialista y Experticia (N:M)
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();

        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", true);
        specialist.addExpertise(trauma);
        specialist.addExpertise(rehabilitation);
        specialistRepository.save(specialist);

        // 5. Tratamientos
        Treatment t1 = new Treatment(LocalDateTime.of(2026, 8, 18, 10, 0), TreatmentType.WOUND_CARE, "Cleaning of left front flipper");
        Treatment t2 = new Treatment(LocalDateTime.of(2026, 8, 19, 11, 30), TreatmentType.HYDRATION, "Subcutaneous fluid therapy");

        animal.addTreatment(t1);
        specialist.addTreatment(t1);

        animal.addTreatment(t2);
        specialist.addTreatment(t2);

        treatmentRepository.saveAll(List.of(t1, t2));

        // Verificaciones finales del Reto
        assertThat(caseRepository.findByCaseCode("RES-2026-100")).isPresent();
        assertThat(caseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION)).isNotEmpty();
        assertThat(animalRepository.findByRescueCaseRescueCenterCode("DB-CAR")).isNotEmpty();
        assertThat(treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId())).hasSize(2);
    }
}