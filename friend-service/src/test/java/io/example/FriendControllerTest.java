package io.example;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FriendServiceApplication.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ContextConfiguration(initializers = FriendControllerTest.Initializer.class)
public class FriendControllerTest {

    private static final String OUTPUT_TOPIC = "friend";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private FriendRepository friendRepository;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("password");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.empty().and("spring.datasource.url=" + postgres.getJdbcUrl());
            values.applyTo(configurableApplicationContext);
        }
    }


    @BeforeClass
    public static void setup() {
        //postgreSQLContainer.setPortBindings(Collections.singletonList(new PortBinding(new Ports.Binding("172.0.0.1", "5432"), ExposedPort.tcp(5432))));
        postgres.start();
        postgres.waitingFor(new DockerHealthcheckWaitStrategy());
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
        embeddedKafka.getEmbeddedKafka().setAdminTimeout(30000);
        System.setProperty("eureka.client.enabled", "false");
    }

    @Before
    public void setUp() {
        Friend f1 = new Friend(1L, 2L),
                f2 = new Friend(3L, 3L),
                f3 = new Friend(3L, 2L),
                f4 = new Friend(3L, 1L),
                f5 = new Friend(4L, 2L),
                f6 = new Friend(5L, 2L);

        Flux<Friend> result = friendRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5, f6));
    }

    @After
    public void after() {
        embeddedKafka.after();
        postgres.stop();
    }


    @Test
    public void testCreateFriend() {
        Friend expected = new Friend(5L, 1L);
        this.webClient.post().uri("/v1/friends")
                .accept(MediaType.APPLICATION_JSON)
                .body(s -> Mono.just(expected), Friend.class)
                .exchange().returnResult(Friend.class).getResponseBody().single()
                .doOnNext(f -> Matchers.equalTo(f).matches(expected))
                .subscribe();
    }

    @Test
    public void testDeleteFriend() {
        this.webClient.delete().uri("/v1/friends/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().returnResult(Friend.class).getResponseBody().then()
                .subscribe();

        friendRepository.getFriend(5L, 1L).map(f -> f)
                .doOnNext(f -> Matchers.isEmptyOrNullString().matches(f))
                .subscribe();
    }
}