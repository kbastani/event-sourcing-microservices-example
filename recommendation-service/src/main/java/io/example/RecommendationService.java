package io.example;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * This event stream processor monitors a stream of events from multiple microservices and builds an eventually
 * consistent view of all domain data as a graph in Neo4j.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
public class RecommendationService {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RecommendationService.class).run(args);
    }
}

