package io.example.friend;

/**
 * An event that encapsulates a state transition for the {@link Friend}
 * domain object.
 *
 * @link Kenny Bastani
 */
public class FriendEvent {

    private Friend subject;
    private FriendEventType eventType;

    public FriendEvent() {
    }

    public FriendEvent(Friend subject, FriendEventType eventType) {
        this.subject = subject;
        this.eventType = eventType;
    }

    public Friend getSubject() {
        return subject;
    }

    public void setSubject(Friend subject) {
        this.subject = subject;
    }

    public FriendEventType getEventType() {
        return eventType;
    }

    public void setEventType(FriendEventType eventType) {
        this.eventType = eventType;
    }
}
