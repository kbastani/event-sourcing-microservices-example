package io.example.domain;

import java.util.UUID;

/**
 * An event that encapsulates a state transition for the {@link User} domain object.
 *
 * @link Kenny Bastani
 */
public class UserEvent extends DomainEvent<User, Integer> {

    private User subject;
    private EventType eventType;

    public UserEvent() {
        this.setId(UUID.randomUUID().hashCode());
    }

    public UserEvent(User subject, EventType eventType) {
        this();
        this.subject = subject;
        this.eventType = eventType;
    }

    public UserEvent(EventType userCreated) {
        this.eventType = userCreated;
    }

    @Override
    public User getSubject() {
        return subject;
    }

    @Override
    public void setSubject(User subject) {
        this.subject = subject;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "subject=" + subject +
                ", eventType=" + eventType +
                '}';
    }
}
