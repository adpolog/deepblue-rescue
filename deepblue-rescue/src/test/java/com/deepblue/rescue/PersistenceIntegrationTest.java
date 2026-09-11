import org.testcontainers.postgresql.PostgreSQLContainer


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
    void testFlywayMigrationsLoaded() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version IN ('1', '2')",
                Integer.class
        );
        assertThat(count).isEqualTo(2);
    }
    @Test
    void testInheritedMethods() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");

        RescueCenter savedCenter = centerRepository.save(center);
        assertThat(savedCenter.getId()).isNotNull();

        boolean exists = centerRepository.existsById(savedCenter.getId());
        assertThat(exists).isTrue();

        Optional<RescueCenter> found = centerRepository.findById(savedCenter.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("DB-CAR");
        assertThat(found.get().getName()).isEqualTo("DeepBlue Caribbean Center");
        assertThat(found.get().getCity()).isEqualTo("Santa Marta");

        long total = centerRepository.count();
        assertThat(total).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testRescueCenterOneToManyRelation() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");

        RescueCase case1 = new RescueCase("RES-2026-001", LocalDate.of(2026, 8, 1), "Playa Salguero", RescueStatus.ADMITTED);
        RescueCase case2 = new RescueCase("RES-2026-002", LocalDate.of(2026, 8, 2), "Taganga", RescueStatus.UNDER_EVALUATION);

        center.addCase(case1);
        center.addCase(case2);
        centerRepository.save(center);

        RescueCenter savedCenter = centerRepository.findByCode("DB-CAR").orElseThrow();
        assertThat(savedCenter.getRescueCases()).hasSize(2);

        RescueCase foundCase1 = caseRepository.findByCaseCode("RES-2026-001").orElseThrow();
        RescueCase foundCase2 = caseRepository.findByCaseCode("RES-2026-002").orElseThrow();

        assertThat(foundCase1.getRescueCenter().getId()).isEqualTo(savedCenter.getId());
        assertThat(foundCase2.getRescueCenter().getId()).isEqualTo(savedCenter.getId());
    }

    @Test
    void testAnimalOneToOneMedicalRecordRelation() {
        RescueCenter center = new RescueCenter("DB-CAR-2", "DeepBlue Center 2", "Cartagena");
        centerRepository.save(center);

        RescueCase rescueCase = new RescueCase("RES-2026-002", LocalDate.of(2026, 8, 5), "Barú", RescueStatus.ADMITTED);
        center.addCase(rescueCase);
        caseRepository.save(rescueCase);

        Animal animal = new Animal("AN-2026-002", "Loggerhead Turtle", "Caretta caretta", AnimalSex.MALE);
        rescueCase.assignAnimal(animal);

        MedicalRecord record = new MedicalRecord(
                BigDecimal.valueOf(28.40),
                "STABLE",
                "Left front flipper injury",
                "Initial rescue observations"
        );
        animal.assignMedicalRecord(record);
        animalRepository.save(animal);

        assertThat(animal.getId()).isNotNull();
        assertThat(record.getId()).isNotNull();

        Animal foundAnimal = animalRepository.findById(animal.getId()).orElseThrow();
        assertThat(foundAnimal.getMedicalRecord()).isNotNull();
        assertThat(foundAnimal.getMedicalRecord().getInitialWeight()).isEqualByComparingTo(BigDecimal.valueOf(28.40));
        assertThat(foundAnimal.getMedicalRecord().getInitialCondition()).isEqualTo("STABLE");
        assertThat(foundAnimal.getMedicalRecord().getInjuries()).isEqualTo("Left front flipper injury");
    }

    @Test
    void testSpecialistExpertiseJPQL() {
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();
        Expertise marineMammals = expertiseRepository.findByNameIgnoreCase("Marine Mammals").orElseThrow();
        Expertise marineBirds = expertiseRepository.findByNameIgnoreCase("Marine Birds").orElseThrow();

        Specialist elena = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", true);
        elena.addExpertise(trauma);
        elena.addExpertise(rehabilitation);
        specialistRepository.save(elena);

        Specialist mateo = new Specialist("SPEC-002", "Mateo", "Pérez", "mateo@deepblue.org", true);
        mateo.addExpertise(marineMammals);
        mateo.addExpertise(rehabilitation);
        specialistRepository.save(mateo);

        Specialist sofia = new Specialist("SPEC-003", "Sofia", "Gómez", "sofia@deepblue.org", true);
        sofia.addExpertise(marineBirds);
        sofia.addExpertise(trauma);
        specialistRepository.save(sofia);

        List<Specialist> traumaSpecialists = specialistRepository.findActiveByExpertise("Trauma");

        assertThat(traumaSpecialists)
                .hasSize(2)
                .extracting(Specialist::getFirstName)
                .containsExactlyInAnyOrder("Elena", "Sofia");
    }

    @Test
    void testCreateTreatments() {

        RescueCenter center = new RescueCenter("DB-CAR-T", "DeepBlue Center Treatments", "Santa Marta");
        centerRepository.save(center);

        RescueCase rescueCase = new RescueCase("RES-2026-T1", LocalDate.of(2026, 8, 10), "Taganga", RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase);
        caseRepository.save(rescueCase);

        Animal animal = new Animal("AN-2026-T1", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        animalRepository.save(animal);

        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();
        Expertise marineMammals = expertiseRepository.findByNameIgnoreCase("Marine Mammals").orElseThrow();

        Specialist elena = new Specialist("SPEC-001", "Elena", "Vargas", "elena.treatments@deepblue.org", true);
        elena.addExpertise(trauma);
        elena.addExpertise(rehabilitation);
        specialistRepository.save(elena);

        Specialist mateo = new Specialist("SPEC-002", "Mateo", "Pérez", "mateo.treatments@deepblue.org", true);
        mateo.addExpertise(marineMammals);
        specialistRepository.save(mateo);


        Treatment t1 = new Treatment(LocalDateTime.of(2026, 8, 10, 9, 0), TreatmentType.WOUND_CARE, "Cleaning and disinfection of wound");
        Treatment t2 = new Treatment(LocalDateTime.of(2026, 8, 11, 10, 30), TreatmentType.HYDRATION, "Subcutaneous fluids administration");
        Treatment t3 = new Treatment(LocalDateTime.of(2026, 8, 12, 14, 0), TreatmentType.OBSERVATION, "Post-treatment general behavior review");

        animal.addTreatment(t1);
        elena.addTreatment(t1);

        animal.addTreatment(t2);
        elena.addTreatment(t2);

        animal.addTreatment(t3);
        mateo.addTreatment(t3);

        treatmentRepository.saveAll(List.of(t1, t2, t3));

        assertThat(treatmentRepository.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void testTreatmentQueryMethodChronological() {

        RescueCenter center = new RescueCenter("DB-CAR-Q", "DeepBlue Center Query", "Santa Marta");
        centerRepository.save(center);

        RescueCase rescueCase = new RescueCase("RES-2026-Q1", LocalDate.of(2026, 8, 10), "Taganga", RescueStatus.IN_REHABILITATION);
        center.addCase(rescueCase);
        caseRepository.save(rescueCase);

        Animal animal = new Animal("AN-2026-Q1", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        animalRepository.save(animal);

        Specialist elena = new Specialist("SPEC-Q1", "Elena", "Vargas", "elena.query@deepblue.org", true);
        Specialist mateo = new Specialist("SPEC-Q2", "Mateo", "Pérez", "mateo.query@deepblue.org", true);
        specialistRepository.saveAll(List.of(elena, mateo));

        Treatment t1 = new Treatment(LocalDateTime.of(2026, 8, 10, 9, 0), TreatmentType.WOUND_CARE, "Wound care");
        Treatment t2 = new Treatment(LocalDateTime.of(2026, 8, 11, 10, 30), TreatmentType.HYDRATION, "Hydration");
        Treatment t3 = new Treatment(LocalDateTime.of(2026, 8, 12, 14, 0), TreatmentType.OBSERVATION, "Observation");

        animal.addTreatment(t1);
        elena.addTreatment(t1);

        animal.addTreatment(t2);
        elena.addTreatment(t2);

        animal.addTreatment(t3);
        mateo.addTreatment(t3);

        treatmentRepository.saveAll(List.of(t1, t2, t3));


        List<Treatment> animalTreatments = treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId());


        assertThat(animalTreatments)
                .hasSize(3)
                .containsExactly(t1, t2, t3);

        assertThat(animalTreatments.get(0).getSpecialist().getFirstName()).isEqualTo("Elena");
        assertThat(animalTreatments.get(2).getSpecialist().getFirstName()).isEqualTo("Mateo");
    }

}