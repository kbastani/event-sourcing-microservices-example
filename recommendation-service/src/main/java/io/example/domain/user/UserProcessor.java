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

        log.info("Event received: " + userEvent.getPayload().toString());

        switch (userEvent.getPayload().getEventType()) {
            case USER_CREATED:
                User newUser = userRepository.save(userEvent.getPayload().getSubject());
                log.info(String.format("Created user: %s", newUser));
                break;
            case USER_UPDATED:
                User updateUser = userEvent.getPayload().getSubject();
                User findUser = userRepository.findUserByUserId(updateUser.getId());
                if(findUser != null) {
                    findUser.setCreatedAt(updateUser.getCreatedAt());
                    findUser.setLastModified(updateUser.getLastModified());
                    findUser.setFirstName(updateUser.getFirstName());
                    findUser.setLastName(updateUser.getLastName());
                    findUser = userRepository.save(findUser);
                    log.info(String.format("Updated user: %s", findUser.toString()));
                }
                break;
            default:
                break;
        }
    }
}
