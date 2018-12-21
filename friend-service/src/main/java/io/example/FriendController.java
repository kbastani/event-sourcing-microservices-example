package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller responsible for exposing REST API interface for managing {@link Friend} domain entities.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@Transactional
public class FriendController {

    private final Source eventStream;
    private final FriendRepository friendRepository;
    private final Logger log = Logger.getLogger(FriendController.class.getName());

    public FriendController(Source eventStream, FriendRepository friendRepository) {
        this.eventStream = eventStream;
        this.friendRepository = friendRepository;
    }

    /**
     * Finds a user's friends by their identifiers. The result is just an edge-list of associations by their unique
     * userId.
     *
     * @param userId is the FK relationship to the user stored in the user service.
     * @return
     */
    @GetMapping(path = "/users/{userId}/friends")
    public Flux<Friend> getFriends(@PathVariable Long userId) {
        // This method does not need to block because it only writes to one system.
        return Optional.of(friendRepository.getFriends(userId))
                .orElseThrow(() -> new RuntimeException("Could not retrieve friends for the supplied user id"));
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
    public Mono<Friend> addFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
        Friend friend = new Friend(userId, friendId);

        // This condition only occurs if there is no existing friendship for the request
        log.info("Friend request received: " + friend.toString());

        // This operation is a reactive application-level dual-write, which requires a 2-phase workflow
        return friendRepository.getFriend(userId, friendId).doOnNext(f -> {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "Friendship already exists");
        }).switchIfEmpty(friendRepository.save(friend).doOnError((ex -> {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }))).doOnNext((f) -> {
            // If the database operation fails, an event should not be sent to the message broker
            log.info(String.format("Database operation for friend request is pending a dual-write to Kafka: %s",
                    friend.toString()));

            try {
                // Attempt to perform a reactive dual-write to Kafka by sending a domain event
                eventStream.output().send(MessageBuilder
                        .withPayload(new FriendEvent(new FriendMessage(f.getUserId(), f.getFriendId()),
                                EventType.FRIEND_ADDED)).build());
                // The application dual-write was a success and the database transaction can commit
            } catch (Exception ex) {
                log.log(Level.FINER, String.format("Friend request dual-write to Kafka failed: %s", friend.toString()));

                // Rollback the transaction
                throw new RuntimeException(ex.getMessage(), ex);
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
    @PutMapping(path = "/users/{userId}/commands/removeFriend")
    public Mono<Friend> removeFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
        // Check if friend relationship already exists
        return friendRepository.getFriend(userId, friendId).doOnSuccessOrError((friend, error) -> {
            if (error == null && friend != null) {
                // The result MUST be blocked since this operation is a dual-write. If the database fails, an event
                // should not be sent to the message broker.
                friendRepository.delete(friend).doOnSuccess(f -> {
                    // Broadcast a new domain event if and only if the database operation is a success.
                    eventStream.output().send(MessageBuilder
                            .withPayload(new FriendEvent(new FriendMessage(userId, friendId), EventType.FRIEND_REMOVED))
                            .build());
                });
            } else if (error != null) {
                throw new RuntimeException("The friend relationship could not be deleted", error);
            }
        });
    }
}
