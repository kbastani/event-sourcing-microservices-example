package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.logging.Level;
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
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody User user) {
        // This condition only occurs if there is no existing friendship for the request
        log.info("User create request received: " + user.toString());

        // This operation is a reactive application-level dual-write, which requires a 2-phase workflow
        return userRepository.save(user).doOnError((ex) -> {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }).doOnNext((userResult) -> {
            // If the database operation fails, an event should not be sent to the message broker
            log.info(String.format("Database operation for user create request is pending a dual-write to Kafka: %s",
                    userResult.toString()));

            try {
                // Attempt to perform a reactive dual-write to Kafka by sending a domain event
                source.output().send(MessageBuilder
                        .withPayload(new UserEvent(userResult, EventType.USER_CREATED)).build());
                // The application dual-write was a success and the database transaction can commit
            } catch (Exception ex) {
                log.log(Level.FINER, String.format("User create request dual-write to Kafka failed: %s",
                        userResult.toString()));

                // Rollback the transaction
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
    }
}
