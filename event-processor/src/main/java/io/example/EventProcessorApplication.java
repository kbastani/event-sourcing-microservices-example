package io.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This event stream processor monitors a stream of events from multiple
 * microservices and builds an eventually consistent view of all domain data
 * as a graph in Neo4j.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
public class EventProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventProcessorApplication.class, args);
    }

}
