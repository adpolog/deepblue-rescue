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