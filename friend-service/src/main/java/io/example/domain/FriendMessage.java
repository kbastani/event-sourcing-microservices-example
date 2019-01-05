package io.example.domain;

public class FriendMessage extends DomainEvent<FriendMessage> {

    private Long userId;
    private Long friendId;

    public FriendMessage() {
    }

    public FriendMessage(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
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
}
