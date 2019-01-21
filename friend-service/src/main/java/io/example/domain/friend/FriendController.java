package io.example.domain.friend;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.validation.constraints.NotNull;

/**
 * Controller responsible for exposing REST API interface for managing {@link Friend} entities.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@Transactional
@Validated
public class FriendController {

	private final Logger logger = Loggers.getLogger(FriendController.class);
	private final Source messageBroker;
	private final FriendService friendService;

	public FriendController(Source messageBroker, FriendService friendService) {
		this.messageBroker = messageBroker;
		this.friendService = friendService;
	}

	/**
	 * Find a {@link Friend} record by its unique identifier.
	 *
	 * @param id is the id of the unique identifier for the {@link Friend} entity.
	 * @return a {@link Mono<Friend>} that will emit the result of the find operation.
	 */
	@GetMapping(path = "/friends/{id}")
	public Mono<Friend> getFriend(@PathVariable Long id) {
		return friendService.find(id);
	}

	/**
	 * Finds a user's friends by their identifiers. The result is just an edge-list of associations by their unique
	 * userId.
	 *
	 * @param userId is the FK relationship to the user stored in the user service.
	 * @return a {@link Flux<Friend>} containing a sequence of {@link Friend} entities 0..Many.
	 */
	@GetMapping(path = "/users/{userId}/friends")
	public Flux<Friend> getFriends(@PathVariable Long userId) {
		return friendService.findUserFriends(userId);
	}

	/**
	 * Adds a friend. This is a reactive application-level dual-write which requires that two external systems
	 * cooperate in a single transaction that must be rolled back if either the database fails or message broker fails.
	 *
	 * @param userId   is the ID of the user
	 * @param friendId is the ID of the friend
	 * @return a {@link ResponseEntity} indicating the result of the delete operation.
	 */
    @PostMapping(path = "/users/{userId}/commands/addFriend")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<?> addFriend(
            @NotNull(message = "UserId must not equal null") @PathVariable Long userId,
            @NotNull(message = "FriendId must not equal null") @RequestParam("friendId") Long friendId
    ) {
		// Take the producer mono and flat map it to a sequence of steps to create a new user
		return friendService.exists(userId, friendId).flatMap(exists -> {
			if (exists) {
				return Mono.error(new HttpClientErrorException(HttpStatus.CONFLICT, "The friendship already exists"));
			} else {
				var friend = new Friend(userId, friendId);

                // Execute a dual-write to the local database and the shared Kafka cluster using an atomic commit
                return friendService.create(friend, entity -> Mono.fromRunnable(() -> {
					// If the database operation fails, a domain event should not be sent to the message broker
					logger.info(String.format("Database request is pending transaction commit to broker: %s",
							entity.toString()));
					try {
						FriendEvent event = new FriendEvent(entity, EventType.FRIEND_ADDED);
						// Set the entity payload after it has been updated in the database, but before committed
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
					// since we don't want to block threads in reactors default thread pool,
					// we have to run blocking stuff in separate pool
				}).subscribeOn(Schedulers.elastic()).then());
			}
		});
	}

	/**
	 * Removes a friend. This is an application-level dual-write, which means that blocking is necessary to make sure
	 * that transactional rollback occurs if either the database write fails or the message broker fails.
	 *
	 * @param userId   is the ID of the user
	 * @param friendId is the ID of the friend
	 * @return a {@link ResponseEntity} indicating the result of the delete operation.
	 */
	@PostMapping(path = "/users/{userId}/commands/removeFriend")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	// no content requires absense of the body, so it have to be void
	public Mono<Void> removeFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
		Assert.state(userId != null, "UserId must not equal null");
		Assert.state(friendId != null, "FriendId must not equal null");

		// Take the producer mono and flat map it to a sequence of steps to create a new user
		// Execute a dual-write to the local database and the shared Kafka cluster using an atomic commit
		return friendService.delete(new Friend(userId, friendId), entity -> {
			// If the database operation fails, a domain event should not be sent to the message broker
			logger.info(String.format("Database request is pending transaction commit to broker: %s",
					entity.toString()));
			try {
				FriendEvent event = new FriendEvent(entity, EventType.FRIEND_REMOVED);
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
		})
		.then();
	}
}
