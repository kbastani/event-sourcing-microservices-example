package io.example.domain.friend.entity;

import io.example.domain.user.entity.User;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

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

    @DateLong
    private Date createdAt;

    @DateLong
    private Date lastModified;

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", user=" + user +
                ", friend=" + friend +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend1 = (Friend) o;
        return Objects.equals(id, friend1.id) &&
                Objects.equals(user, friend1.user) &&
                Objects.equals(friend, friend1.friend);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, user, friend);
    }
}
