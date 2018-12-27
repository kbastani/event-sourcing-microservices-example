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
 * Controller for the {@link User} API.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@Transactional
public class UserController {

    private final UserRepository userRepository;
    private final Source source;
    private final KafkaDualWriter kafkaDualWriter;

    public UserController(UserRepository userRepository, Source source, KafkaDualWriter kafkaDualWriter) {
        this.userRepository = userRepository;
        this.source = source;
        this.kafkaDualWriter = kafkaDualWriter;
    }

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody Mono<User> user) {
        AtomicReference<Long> id = new AtomicReference<>();
        return user.flatMap(u -> {
            id.set(u.getId());
            return kafkaDualWriter.dualWrite(source,
                    () -> userRepository.getUser(id.get()),
                    () -> userRepository.save(u).doOnSuccess(entity -> id.set(entity.getId())),
                    new UserEvent(u, EventType.USER_CREATED),
                    (entity) -> {
                        if (entity != null) {
                            throw new HttpClientErrorException(HttpStatus.CONFLICT, "User entity already exists");
                        }
                    }, 30000L);
        });
    }

    @GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<User> getUser(@PathVariable("userId") Long userId) {
        return userRepository.getUser(userId);
    }

    @PutMapping(path = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<User> updateUser(@PathVariable("userId") Long userId, @RequestBody User user) {
        user.setId(userId);
        System.out.println(user.toString());
        return kafkaDualWriter.dualWrite(source,
                () -> userRepository.getUser(userId).doOnSuccess(existingUser -> {
                    if (existingUser == null) {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                                "The user with the supplied ID does not exist");
                    }
                }).flatMap(u -> userRepository.save(user)),
                () -> userRepository.save(user),
                new UserEvent(user, EventType.USER_UPDATED),
                (entity) -> {
                }, 30000L);
    }
}
