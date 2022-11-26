package io.example.domain.friend;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.SubscribableChannel;

/**
 * Custom Spring Cloud Stream {@link Sink} binding for processing
 * events from the Friend channel.
 *
 * @author Kenny Bastani
 */
public interface FriendSink {

    String INPUT = "friend";

    @Input(FriendSink.INPUT)
    SubscribableChannel friend();
}
