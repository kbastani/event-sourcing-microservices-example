package io.example.domain.friend;

/**
 * An event that encapsulates a state transition for the {@link Friend}
 * domain object.
 *
 * @link Kenny Bastani
 */
public class FriendEvent {

	private FriendMessage subject;
	private FriendEventType eventType;

	public FriendEvent() {
	}

	public FriendEvent(FriendMessage subject, FriendEventType eventType) {
		this.subject = subject;
		this.eventType = eventType;
	}

	public FriendMessage getSubject() {
		return subject;
	}

	public void setSubject(FriendMessage subject) {
		this.subject = subject;
	}

	public FriendEventType getEventType() {
		return eventType;
	}

	public void setEventType(FriendEventType eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return "FriendEvent{" +
				"subject=" + subject +
				", eventType=" + eventType +
				'}';
	}
}
