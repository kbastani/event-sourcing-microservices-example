package io.example;

import io.example.util.KafkaDualWriter;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
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

    private final Source source;
    private final FriendRepository friendRepository;
    private final KafkaDualWriter kafkaDualWriter;
    private final Logger log = Logger.getLogger(FriendController.class.getName());

    public FriendController(Source source, FriendRepository friendRepository, KafkaDualWriter kafkaDualWriter) {
        this.source = source;
        this.friendRepository = friendRepository;
        this.kafkaDualWriter = kafkaDualWriter;
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
        return kafkaDualWriter.dualWrite(source, friendRepository.getFriend(userId, friendId), friendRepository.save(friend),
                new FriendEvent(friend, EventType.FRIEND_ADDED), 30000L);
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
    public Mono<Void> removeFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
        // Check if friend relationship already exists
        return kafkaDualWriter.dualDelete(source, friendRepository.getFriend(userId, friendId), friendRepository.delete(new Friend(userId, friendId)),
                new FriendEvent(EventType.FRIEND_REMOVED), 30000L);
    }
}
