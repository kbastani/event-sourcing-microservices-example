package io.example;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * This event stream processor monitors a stream of events from multiple microservices and builds an eventually
 * consistent view of all domain data as a graph in Neo4j.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
@EnableNeo4jRepositories(value = {"io.example.domain.user", "io.example.domain.friend"})
@EntityScan({"io.example.domain.friend.entity", "io.example.domain.user.entity"})
public class RecommendationService {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RecommendationService.class).run(args);
    }
}

