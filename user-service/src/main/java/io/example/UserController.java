package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

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
    private final Logger log = Logger.getLogger(UserController.class.getName());

    public UserController(UserRepository userRepository, Source source) {
        this.userRepository = userRepository;
        this.source = source;
    }

    @PostMapping(path = "/users")
    public Mono<User> createUser(@RequestBody User user) {
        User userResult = userRepository.save(user).block();

        log.info("User created: " + user.toString());
        source.output().send(MessageBuilder
                .withPayload(new UserEvent(userResult, EventType.USER_CREATED)).build());

        return Mono.justOrEmpty(userResult);
    }
}
