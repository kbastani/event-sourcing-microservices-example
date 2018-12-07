package io.example.domain.friend.entity;

import io.example.domain.user.entity.User;
import org.neo4j.ogm.annotation.*;

/**
 * A projection of the {@link Friend} domain object that is owned by the
 * friend service.
 *
 * @author Kenny Bastani
 */
@RelationshipEntity("FRIEND")
public class Friend {
    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private User user;

    @EndNode
    private User friend;

    public Friend() {
    }

    public Friend(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }
}
