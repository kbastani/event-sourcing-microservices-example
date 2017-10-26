package io.example;

/**
 * An event that encapsulates a state transition for the {@link Friend}
 * domain object.
 *
 * @link Kenny Bastani
 */
public class FriendEvent {

    private Friend subject;
    private EventType eventType;

    public FriendEvent() {
    }

    public FriendEvent(Friend subject, EventType eventType) {
        this.subject = subject;
        this.eventType = eventType;
    }

    public Friend getSubject() {
        return subject;
    }

    public void setSubject(Friend subject) {
        this.subject = subject;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
