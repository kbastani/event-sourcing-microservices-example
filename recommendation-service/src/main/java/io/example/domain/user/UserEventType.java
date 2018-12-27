package io.example.domain.user;

import io.example.domain.user.entity.User;

/**
 * The type of events that affect the state of a {@link User}.
 *
 * @author Kenny Bastani
 */
public enum UserEventType {
    USER_CREATED,
    USER_UPDATED
}
