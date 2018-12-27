package io.example;

import io.example.domain.User;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;

/**
 * The microservice that manages the {@link User} domain.
 *
 * @author Kenny Bastani
 */
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
        new SpringApplicationBuilder(UserServiceApplication.class).web(WebApplicationType.REACTIVE).run(args);
	}

    @Configuration
    @EnableBinding(Source.class)
    class StreamConfig {
    }
}
