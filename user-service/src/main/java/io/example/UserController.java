package io.example;

import io.example.util.AbstractDualWriter;
import io.example.util.KafkaDualWriter;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

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
    private final Logger log = Loggers.getLogger(AbstractDualWriter.class);

    public UserController(UserRepository userRepository, Source source, KafkaDualWriter kafkaDualWriter) {
        this.userRepository = userRepository;
        this.source = source;
        this.kafkaDualWriter = kafkaDualWriter;
    }

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody Mono<User> user) {
        return user.flatMap(u -> kafkaDualWriter.dualWrite(source, userRepository.getUser(u.getId()), userRepository.save(u),
                new UserEvent(u, EventType.USER_CREATED), 30000L));
    }
}
