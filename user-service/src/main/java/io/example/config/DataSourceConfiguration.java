package io.example.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.data.r2dbc.function.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.function.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.support.R2dbcExceptionTranslator;
import org.springframework.data.relational.core.conversion.BasicRelationalConverter;
import org.springframework.data.relational.core.conversion.RelationalConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

/**
 * This class configures reactive database access using R2DBC with Postgres. Since R2DBC does not allow using JPA
 * repositories, schema creation must be handled manually or using a tool, like Liquibase. This class configures R2DBC
 * while also configuring Liquibase to be able to manage the schema creation and migration.
 *
 * @author Kenny Bastani
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "io.example")
@Profile({"kubernetes", "docker", "development", "test"})
public class DataSourceConfiguration extends AbstractR2dbcConfiguration {

	@Value("${postgres.host}")
	private String postgresHost;

	@Value("${postgres.port}")
	private Integer postgresPort;

	@Value("${postgres.database-name}")
	private String databaseName;

	@Value("${spring.application.name}")
	private String applicationName;

	private DataSourceProperties dataSourceProperties;

	DataSourceConfiguration(DataSourceProperties dataSourceProperties) {
		this.dataSourceProperties = dataSourceProperties;
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		return getPostgresqlConnectionFactory();
	}

	@NotNull
	private PostgresqlConnectionFactory getPostgresqlConnectionFactory() {
		return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
				.applicationName(applicationName)
				.database(databaseName)
				.host(postgresHost)
				.port(postgresPort)
				.username(dataSourceProperties.getDataUsername())
				.password(dataSourceProperties.getDataPassword()).build());
	}

	@Bean(name = "transactionalDatabaseClient")
	public TransactionalDatabaseClient transactionalDatabaseClient(ReactiveDataAccessStrategy dataAccessStrategy,
	                                                               R2dbcExceptionTranslator exceptionTranslator) {
		Assert.notNull(dataAccessStrategy, "DataAccessStrategy must not be null!");
		Assert.notNull(exceptionTranslator, "ExceptionTranslator must not be null!");
		return TransactionalDatabaseClient.builder().connectionFactory(this.connectionFactory())
				.dataAccessStrategy(dataAccessStrategy).exceptionTranslator(exceptionTranslator).build();
	}

	@Bean
	public MappingR2dbcConverter converter(RelationalMappingContext mappingContext,
	                                       R2dbcCustomConversions r2dbcCustomConversions) {
		Assert.notNull(mappingContext, "MappingContext must not be null!");
		BasicRelationalConverter converter = new BasicRelationalConverter(mappingContext, r2dbcCustomConversions);
		return new MappingR2dbcConverter(converter);
	}

	@Bean
	@ConfigurationProperties("spring.datasource")
	@LiquibaseDataSource
	public DataSource dataSource(DataSourceProperties properties) {
		return new SimpleDriverDataSource(new org.postgresql.Driver(), properties.getUrl(),
				properties.getDataUsername(), properties.getDataPassword());
	}
}