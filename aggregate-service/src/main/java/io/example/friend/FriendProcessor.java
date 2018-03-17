package io.example.friend;

import io.example.AggregateRepository;
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
    private final AggregateRepository aggregateRepository;

    public FriendProcessor(AggregateRepository aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    @StreamListener(value = FriendSink.INPUT)
    public void apply(Message<FriendEvent> friendEvent) {

        log.info("Event received: " + friendEvent.toString());

        switch (friendEvent.getPayload().getEventType()) {
            case FRIEND_ADDED:
                aggregateRepository.addFriend(
                        friendEvent.getPayload().getSubject().getUserId(),
                        friendEvent.getPayload().getSubject().getFriendId());
                break;
            case FRIEND_REMOVED:
                aggregateRepository.removeFriend(
                        friendEvent.getPayload().getSubject().getUserId(),
                        friendEvent.getPayload().getSubject().getFriendId());
                break;
        }
    }
}
