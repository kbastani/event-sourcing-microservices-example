package io.example.domain.user;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserClient {

	private final WebClient.Builder userWebClient;

	public UserClient(WebClient.Builder userWebClient) {
		this.userWebClient = userWebClient;
	}

	public Mono<User> getUser(Long userId) {

		Mono<User> user;

		try {
			user = userWebClient.baseUrl("http://user-service/")
					.build()
					.get()
					.uri("v1/users/{userId}", userId)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.bodyToMono(User.class)
					.single()
					.log();
		} catch (Exception ex) {
			throw new RuntimeException("Could not retrieve user", ex);
		}

		return user;
	}
}
