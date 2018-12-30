package io.example.domain;

import io.example.util.KafkaDualWriter;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the main REST API for the {@link User} service.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@Transactional
public class UserController {

    private final Source messageBroker;
    private final UserRepository userRepository;
    private final KafkaDualWriter kafkaDualWriter;

    public UserController(UserRepository userRepository, Source messageBroker, KafkaDualWriter kafkaDualWriter) {
        this.userRepository = userRepository;
        this.messageBroker = messageBroker;
        this.kafkaDualWriter = kafkaDualWriter;
    }

    @GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<User> getUser(@PathVariable("userId") Long userId) {
        return userRepository.getUser(userId);
    }

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody Mono<User> user) {
        // Create an atomic reference for the user identity to create a context for the newly created user
        AtomicReference<Long> id = new AtomicReference<>();

        // Take the producer mono and flat map it to a sequence of steps to create a new user
        return user.flatMap(u -> {
            // Set the atomic reference to the user identity submitted in the payload
            id.set(u.getId());

            // Execute a dual-write to the local database and the shared Kafka cluster using an atomic commit
            return kafkaDualWriter.dualWrite(messageBroker,
                    // The lookup function used by the dual writer
                    () -> userRepository.getUser(id.get()),
                    // The save function used by the dual writer. Sets the atomic reference to the assigned ID.
                    () -> userRepository.save(u).doOnSuccess(entity -> id.set(entity.getId())),
                    new UserEvent(u, EventType.USER_CREATED),
                    (entity) -> {
                        // If the lookup function already has an entity, throw an error
                        if (entity != null)
                            throw new HttpClientErrorException(HttpStatus.CONFLICT, "User entity already exists");
                    }, 30000L);
        });
    }


    @PutMapping(path = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<User> updateUser(@PathVariable("userId") Long userId, @RequestBody Mono<User> user) {
        // Apply the dual write to the local database and the shared Kafka cluster using an atomic commit
        return user.flatMap(u -> {
            // Overwrite the payload's ID with the userId provided through the URI
            u.setId(userId);

            return kafkaDualWriter.dualWrite(messageBroker,
                    // Check if the user exists and then apply the update using a flatMap
                    () -> userRepository.getUser(userId).doOnSuccess(entity -> {
                        if (entity == null)
                            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "That user does not exist");
                    }).flatMap(entity -> userRepository.save(u)),
                    () -> userRepository.save(u), new UserEvent(u, EventType.USER_UPDATED),
                    (entity) -> {
                        // Let the entity pass through to the next step
                    }, 30000L);
        });
    }
}
