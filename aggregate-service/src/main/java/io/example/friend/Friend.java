package io.example.friend;

/**
 * A projection of the {@link Friend} domain object that is owned by the
 * friend service.
 *
 * @author Kenny Bastani
 */
public class Friend {
    private Long id;

    private Long userId;
    private Long friendId;

    public Friend() {
    }

    public Friend(Long userId, Long friendId) {
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
}
