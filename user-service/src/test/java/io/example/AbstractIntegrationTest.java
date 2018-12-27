package io.example;

import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UserServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer();

    @ClassRule
    public static GenericContainer postgres = new GenericContainer("postgres:9.6.8")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_PASSWORD", "password")
            .withEnv("POSTGRES_USER", "postgres")
            .withEnv("POSTGRES_DATABASE", "user-db");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/", postgres.getContainerIpAddress(),
                    postgres.getMappedPort(5432));
            TestPropertyValues values = TestPropertyValues.of(
                    "postgres.host=" + postgres.getContainerIpAddress(),
                    "postgres.port=" + postgres.getMappedPort(5432),
                    "postgres.url=" + jdbcUrl,
                    "spring.datasource.url=" + jdbcUrl,
                    "spring.cloud.stream.kafka.binder.brokers=" + kafka.getBootstrapServers(),
                    "eureka.client.enabled=false");
            values.applyTo(configurableApplicationContext);
        }
    }

}
