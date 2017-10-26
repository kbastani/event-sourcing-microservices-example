package io.example;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

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

    public UserController(UserRepository userRepository, Source source) {
        this.userRepository = userRepository;
        this.source = source;
    }

    @RequestMapping(path = "/users", method = RequestMethod.POST)
    public User createUser(@RequestBody User user) {
        userRepository.save(user);

        source.output().send(MessageBuilder
                .withPayload(new UserEvent(user, EventType.USER_CREATED)).build());

        return user;
    }
}
