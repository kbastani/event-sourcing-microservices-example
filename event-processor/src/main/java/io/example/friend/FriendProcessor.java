package io.example.friend;

import io.example.user.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * Message stream listener for {@link Friend} events. Maps types of events
 * to a graph operation that replicates a connected view of domain data
 * across microservices.
 *
 * @author Kenny Bastani
 */
@Configuration
@EnableBinding(FriendSink.class)
public class FriendProcessor {

    private final Logger log = Logger.getLogger(FriendProcessor.class);
    private final UserRepository userRepository;

    public FriendProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @StreamListener(value = FriendSink.INPUT)
    public void apply(Message<FriendEvent> friendEvent) {

        log.info("Event received: " + friendEvent.toString());

        switch (friendEvent.getPayload().getEventType()) {
            case FRIEND_ADDED:
                userRepository.addFriend(
                        friendEvent.getPayload().getSubject().getUserA(),
                        friendEvent.getPayload().getSubject().getUserB());
                break;
            case FRIEND_REMOVED:
                userRepository.removeFriend(
                        friendEvent.getPayload().getSubject().getUserA(),
                        friendEvent.getPayload().getSubject().getUserB());
                break;
        }
    }
}
