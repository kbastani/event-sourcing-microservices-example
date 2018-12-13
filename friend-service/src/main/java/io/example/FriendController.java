package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

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

    public FriendController(Source eventStream, FriendRepository friendRepository) {
        this.eventStream = eventStream;
        this.friendRepository = friendRepository;
    }

    @GetMapping(path = "/users/{userId}/friends")
    public Flux<Friend> getFriends(@PathVariable Long userId) {
        return Optional.of(friendRepository.findAllByUserId(userId))
                .orElseThrow(() -> new RuntimeException("Could not retrieve friends for the supplied user id"));
    }

    @PostMapping(path = "/users/{userId}/commands/addFriend")
    public Mono<Friend> addFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
        Friend friend;

        // Check if friend relationship already exists
        if (friendRepository.findFriends(userId, friendId).block() == null) {
            friend = new Friend(userId, friendId);

            // Save friend relationship
            friend = friendRepository.save(friend).block();

            // Broadcast a new domain event
            eventStream.output().send(MessageBuilder
                    .withPayload(new FriendEvent(new FriendMessage(userId, friendId), EventType.FRIEND_ADDED))
                    .build());
        } else {
            return Mono.justOrEmpty(Optional.empty());
        }

        return Mono.justOrEmpty(friend);
    }

    @PutMapping(path = "/users/{userId}/commands/removeFriend")
    public Mono<?> removeFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {

        // Fetch friend relationship
        Mono<Friend> friend = friendRepository.findFriends(userId, friendId);

        if (friend != null) {
            // Delete friend relationship
            Mono<Void> result = friendRepository.deleteAll(friend);

            result.block();

            // Broadcast a new domain event
            eventStream.output().send(MessageBuilder
                    .withPayload(new FriendEvent(new FriendMessage(userId, friendId), EventType.FRIEND_REMOVED)).build());
        } else {
            return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }

        return Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }
}
