package io.example.domain.user;

import io.example.domain.user.entity.User;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.SubscribableChannel;

/**
 * Custom Spring Cloud Stream {@link Sink} binding for processing
 * events from the {@link User} channel.
 *
 * @author Kenny Bastani
 */
public interface UserSink {
    String INPUT = "user";

    @Input(UserSink.INPUT)
    SubscribableChannel user();
}
