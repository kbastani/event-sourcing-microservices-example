package io.example;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

/**
 * This class configures reactive database access using R2DBC with Postgres. Since R2DBC does not
 * allow using JPA repositories, schema creation must be handled manually or using a tool, like Liquibase.
 * This class configures R2DBC while also configuring Liquibase to be able to manage the schema creation
 * and migration.
 *
 * @author Kenny Bastani
 */
@Configuration
@EnableR2dbcRepositories
@Profile({"development", "docker"})
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

    @NotNull
    @Override
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


    @Bean
    @ConfigurationProperties("spring.datasource")
    @LiquibaseDataSource
    public DataSource dataSource(DataSourceProperties properties) {
        return new SingleConnectionDataSource(properties.getUrl(),
                properties.getDataUsername(), properties.getDataPassword(), true);
    }
}