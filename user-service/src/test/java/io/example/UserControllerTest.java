package io.example;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Assert;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ContextConfiguration(initializers = UserControllerTest.Initializer.class)
public class UserControllerTest {

    private static final String OUTPUT_TOPIC = "user";

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private UserRepository userRepository;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:alpine")
            .withDatabaseName("user-db")
            .withUsername("postgres")
            .withPassword("password");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.empty()
                    .and("spring.datasource.url=" + postgres.getJdbcUrl())
                    .and("spring.cloud.stream.kafka.binder.brokers=" + embeddedKafka.getEmbeddedKafka().getBrokersAsString())
                    .and("eureka.client.enabled=false");
            values.applyTo(configurableApplicationContext);
        }
    }

    @BeforeClass
    public static void setup() {
        postgres.start();
        postgres.waitingFor(new DockerHealthcheckWaitStrategy());
    }

    @Before
    public void setUp() {
        //Mono<User> user = userRepository.save(new User("Kenny", "Bastani"));
    }

    @After
    public void after() {
        embeddedKafka.after();
        postgres.stop();
    }

    @Test
    public void contextLoads() {
        Assert.notEmpty(embeddedKafka.getEmbeddedKafka().getKafkaServers(), "The kafka server list should not be empty");
    }


    @Ignore
    public void testCreateUser() {
        User expected = new User("Kenny", "Bastani");
        expected.setId(1L);

        this.webClient.post().uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(s -> Mono.just(expected), User.class)
                .exchange().returnResult(User.class).getResponseBody().single()
                .doOnNext(f -> Matchers.equalTo(f).matches(expected));
    }
}