package io.example.domain.friend;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * The domain entity representing a friend relationship between two users.
 *
 * @author Kenny Bastani
 */
@Table("friend")
public class Friend {

	@Id
	private Long id;

	@Column(value = "user_id")
	private Long userId;

	@Column(value = "friend_id")
	private Long friendId;

	@Column(value = "created_at")
	private Timestamp createdAt;

	@Column(value = "updated_at")
	private Timestamp updatedAt;

	public Friend() {
	}

	public Friend(Long userId, Long friendId) {
		this.userId = userId;
		this.friendId = friendId;
	}

	public Friend(Long id, Long userId, Long friendId) {
		this.id = id;
		this.userId = userId;
		this.friendId = friendId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
		return "Friend{" +
				"id=" + id +
				", userId=" + userId +
				", friendId=" + friendId +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Friend friend = (Friend) o;
		return Objects.equals(id, friend.id) &&
				Objects.equals(userId, friend.userId) &&
				Objects.equals(friendId, friend.friendId) &&
				Objects.equals(createdAt, friend.createdAt) &&
				Objects.equals(updatedAt, friend.updatedAt);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id, userId, friendId, createdAt, updatedAt);
	}
}
