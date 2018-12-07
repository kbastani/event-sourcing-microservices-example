package io.example.domain.user;

import io.example.domain.user.entity.User;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

/**
 * Message stream listener for {@link User} events. Maps types of events
 * to a graph operation that replicates a connected view of domain data
 * across microservices.
 *
 * @author Kenny Bastani
 */
@Configuration
@EnableBinding(UserSink.class)
public class UserProcessor {

    private final Logger log = Logger.getLogger(UserProcessor.class.getName());
    private final UserRepository userRepository;

    public UserProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @StreamListener(value = UserSink.INPUT)
    @Transactional
    public void apply(Message<UserEvent> userEvent) {

        log.info("Event received: " + userEvent.getPayload().getSubject());

        switch (userEvent.getPayload().getEventType()) {
            case USER_CREATED:

                // Saves a new user node
                User user = new User(userEvent.getPayload().getSubject().getId(),
                        userEvent.getPayload().getSubject().getFirstName(),
                        userEvent.getPayload().getSubject().getLastName());

                user = userRepository.save(user);
                user = userRepository.findById(user.getId(), 1).orElse(null);

                log.info("Created user: " + user);
                break;
        }
    }
}
