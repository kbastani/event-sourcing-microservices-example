package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
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

    @RequestMapping(path = "/users/{userId}/friends", method = RequestMethod.GET)
    public HttpEntity<?> getFriends(@PathVariable Long userId, Pageable pageable,
                                    PagedResourcesAssembler<Friend> assembler) {
        return Optional.of(friendRepository.findAllByUserId(userId, pageable))
                .map(a -> new ResponseEntity<>(assembler.toResource(a), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not retrieve friends for the supplied user id"));
    }

    @RequestMapping(path = "/users/{userId}/commands/addFriend", method = RequestMethod.POST)
    public HttpEntity<?> addFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {
        Friend friend;

        // Check if friend relationship already exists
        if (!friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            friend = new Friend(userId, friendId);

            // Save friend relationship
            friendRepository.save(friend);

            // Broadcast a new domain event
            eventStream.output().send(MessageBuilder
                    .withPayload(new FriendEvent(new FriendMessage(userId, friendId), EventType.FRIEND_ADDED)).build());
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(friend, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/users/{userId}/commands/removeFriend", method = RequestMethod.PUT)
    public HttpEntity<?> removeFriend(@PathVariable Long userId, @RequestParam("friendId") Long friendId) {

        // Fetch friend relationship
        Friend friend = friendRepository.findFriendByUserIdAndFriendId(userId, friendId);

        if (friend != null) {
            // Delete friend relationship
            friendRepository.delete(friend);

            // Broadcast a new domain event
            eventStream.output().send(MessageBuilder
                    .withPayload(new FriendEvent(new FriendMessage(userId, friendId), EventType.FRIEND_REMOVED)).build());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
