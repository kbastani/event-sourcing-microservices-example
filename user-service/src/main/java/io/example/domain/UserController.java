package io.example.domain;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * This is the main REST API for the {@link User} service.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@Transactional
public class UserController {

	private final Logger logger = Loggers.getLogger(UserController.class);
	private final Source messageBroker;
	private final UserService userService;

	public UserController(Source messageBroker, UserService userService) {
		this.messageBroker = messageBroker;
		this.userService = userService;
	}

	@GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	public Mono<User> getUser(@PathVariable("userId") Long userId) {
		return userService.find(userId);
	}

	@PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<User> createUser(@RequestBody Mono<User> user) {
		// user will be never null with mono - spring will map it to Mono.empty() in that case

		// Take the producer mono and flat map it to a sequence of steps to create a new user
		return user.flatMap(u -> {
			// Execute a dual-write to the local database and the shared Kafka cluster using an atomic commit
			return userService.create(u, entity -> {
				// If the database operation fails, a domain event should not be sent to the message broker
				logger.info(String.format("Database request is pending transaction commit to broker: %s",
						entity.toString()));
				try {
					UserEvent event = new UserEvent(entity, EventType.USER_CREATED);
					// Set the entity payload after it has been updated in the database, but before being committed
					event.setSubject(entity);
					// Attempt to perform a reactive dual-write to message broker by sending a domain event
					messageBroker.output().send(MessageBuilder.withPayload(event).build(), 30000L);
					// The application dual-write was a success and the database transaction can commit
				} catch (Exception ex) {
					logger.error(String.format("A dual-write transaction to the message broker has failed: %s",
							entity.toString()), ex);
					// This error will cause the database transaction to be rolled back
					throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
							"A transactional error occurred");
				}
			});
		});
	}


	@PutMapping(path = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<User> updateUser(@PathVariable("userId") Long userId, @RequestBody User user) {
		Assert.state(user != null, "User payload must not equal null");
		Assert.state(userId != null, "The userId must not equal null");
		Assert.state(user.getId().equals(userId), "The userId supplied in the URI path does not match the payload");

		// Execute a dual-write to the local database and the shared Kafka cluster using an atomic commit
		return userService.update(user, entity -> {
			// If the database operation fails, a domain event should not be sent to the message broker
			logger.info(String.format("Database request is pending transaction commit to broker: %s",
					entity.toString()));
			try {
				UserEvent event = new UserEvent(entity, EventType.USER_UPDATED);
				// Set the entity payload after it has been updated in the database, but before being committed
				event.setSubject(entity);
				// Attempt to perform a reactive dual-write to message broker by sending a domain event
				messageBroker.output().send(MessageBuilder.withPayload(event).build(), 30000L);
				// The application dual-write was a success and the database transaction can commit
			} catch (Exception ex) {
				logger.error(String.format("A dual-write transaction to the message broker has failed: %s",
						entity.toString()), ex);
				// This error will cause the database transaction to be rolled back
				throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
						"A transactional error occurred");
			}
		});
	}
}
