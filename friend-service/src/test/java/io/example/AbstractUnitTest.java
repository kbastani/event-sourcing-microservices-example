package io.example;

import io.example.config.DataSourceConfiguration;
import io.example.domain.FriendService;
import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = AbstractUnitTest.Initializer.class, classes = {
		DataSourceConfiguration.class,
		DataSourceAutoConfiguration.class,
		FriendService.class
})
@ActiveProfiles("test")
public abstract class AbstractUnitTest {

	@ClassRule
	public static GenericContainer postgres = new GenericContainer("postgres:9.6.8")
			.withExposedPorts(5432)
			.withEnv("POSTGRES_PASSWORD", "password")
			.withEnv("POSTGRES_USER", "postgres");

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
			String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", postgres.getContainerIpAddress(),
					postgres.getMappedPort(5432), "postgres");

			TestPropertyValues values = TestPropertyValues.of(
					"postgres.host=" + postgres.getContainerIpAddress(),
					"postgres.port=" + postgres.getMappedPort(5432),
					"postgres.url=" + jdbcUrl,
					"postgres.database-name=postgres",
					"spring.application.name=friend-service",
					"spring.datasource.data-username=postgres",
					"spring.datasource.data-password=password",
					"spring.datasource.url=" + jdbcUrl,
					"eureka.client.enabled=false");

			values.applyTo(configurableApplicationContext);
		}
	}

}
