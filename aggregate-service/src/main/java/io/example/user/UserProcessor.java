package io.example.user;

import org.apache.log4j.Logger;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

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

    private final Logger log = Logger.getLogger(UserProcessor.class);
    private final UserRepository userRepository;

    public UserProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @StreamListener(value = UserSink.INPUT)
    public void apply(Message<UserEvent> userEvent) {

        log.info("Event received: " + userEvent.toString());

        switch (userEvent.getPayload().getEventType()) {
            case USER_CREATED:

                // Saves a new user node
                userRepository.save(userEvent.getPayload().getSubject());
                break;
        }
    }
}
