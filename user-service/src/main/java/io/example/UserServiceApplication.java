package io.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;

/**
 * The microservice that manages the {@link User} domain.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
        new SpringApplicationBuilder(UserServiceApplication.class).run(args);
	}

    @Configuration
    @EnableBinding(Source.class)
    class StreamConfig {
    }
}
