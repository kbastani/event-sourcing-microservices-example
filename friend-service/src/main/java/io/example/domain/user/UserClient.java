package io.example.domain.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserClient {

	private final WebClient userWebClient;

	public UserClient(@Qualifier("userWebClient") WebClient userWebClient) {
		this.userWebClient = userWebClient;
	}

	public Mono<User> getUser(Long userId) {
		return userWebClient.get()
				.uri("v1/users/{userId}", userId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(User.class)
				.single();
	}
}
