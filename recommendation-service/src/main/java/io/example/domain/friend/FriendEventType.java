package io.example.domain.friend;

import io.example.domain.friend.entity.Friend;

/**
 * The type of events that affect the state of a {@link Friend}.
 *
 * @author Kenny Bastani
 */
public enum FriendEventType {
    FRIEND_ADDED,
    FRIEND_REMOVED
}
