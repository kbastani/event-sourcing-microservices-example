package io.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * This event stream processor monitors a stream of events from multiple microservices and builds an eventually
 * consistent view of all domain data as a graph in Neo4j.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AggregateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateServiceApplication.class, args);
    }

}
