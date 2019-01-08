package io.example.domain.friend;

import java.sql.Timestamp;

public class FriendMessage extends DomainEvent<FriendMessage> {

	private Long userId;
	private Long friendId;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public FriendMessage() {
	}

	public FriendMessage(Long userId, Long friendId) {
		this.userId = userId;
		this.friendId = friendId;
	}

	public FriendMessage(Long userId, Long friendId, Timestamp createdAt, Timestamp updatedAt) {
		this.userId = userId;
		this.friendId = friendId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getFriendId() {
		return friendId;
	}

	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "FriendMessage{" +
				"userId=" + userId +
				", friendId=" + friendId +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				"} " + super.toString();
	}
}
