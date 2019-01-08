package io.example.domain.friend;

/**
 * An event that encapsulates a state transition for the {@link Friend}
 * domain object.
 *
 * @link Kenny Bastani
 */
public class FriendEvent extends DomainEvent<Friend> {

    private Friend subject;
    private EventType eventType;
    private FriendMessage friendMessage;

    public FriendEvent(EventType friendRemoved) {
    }

    public FriendEvent(Friend subject, EventType eventType) {
        this.subject = subject;
        this.eventType = eventType;
    }

    public FriendEvent(Friend subject, EventType eventType, FriendMessage friendMessage) {
        this.subject = subject;
        this.eventType = eventType;
        this.friendMessage = friendMessage;
    }

    public FriendEvent(Friend subject, Friend subject1, EventType eventType, FriendMessage friendMessage) {
        super(subject);
        this.subject = subject1;
        this.eventType = eventType;
        this.friendMessage = friendMessage;
    }

    public FriendMessage getFriendMessage() {
        return friendMessage;
    }

    public void setFriendMessage(FriendMessage friendMessage) {
        this.friendMessage = friendMessage;
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
