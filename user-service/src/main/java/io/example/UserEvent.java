package io.example;

/**
 * An event that encapsulates a state transition for the {@link User}
 * domain object.
 *
 * @link Kenny Bastani
 */
public class UserEvent {

    private User subject;
    private EventType eventType;

    public UserEvent() {
    }

    public UserEvent(User subject, EventType eventType) {
        this.subject = subject;
        this.eventType = eventType;
    }

    public User getSubject() {
        return subject;
    }

    public void setSubject(User subject) {
        this.subject = subject;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
