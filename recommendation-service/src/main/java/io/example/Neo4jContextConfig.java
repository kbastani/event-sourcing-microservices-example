package io.example;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableNeo4jRepositories(value = {"io.example.domain.user", "io.example.domain.friend"}, considerNestedRepositories = true)
public class Neo4jContextConfig {

    @Value("${spring.data.neo4j.uri:http://localhost:7474}")
    private String uri;

    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
        return new org.neo4j.ogm.config.Configuration.Builder()
                .uri(uri)
                .build();
    }

    @Bean
    public SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configuration) {
        return new SessionFactory(configuration, "io.example.domain.friend.entity", "io.example.domain.user.entity");
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        Neo4jTransactionManager transactionManager = new Neo4jTransactionManager(sessionFactory);
        transactionManager.setValidateExistingTransaction(true);
        return transactionManager;
    }
}
