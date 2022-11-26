package io.example.domain.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.DateLong;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

import java.util.Date;
import java.util.Random;

/**
 * A projection of the {@link User} domain object that is owned by the
 * user service.
 *
 * @author Kenny Bastani
 */
@Node
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;

    @DateLong
    private Date createdAt;

    @DateLong
    private Date lastModified;

    public User() {
        userId = Math.abs(new Random().nextLong());
    }

    public User(String firstName, String lastName) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(Long id, String firstName, String lastName) {
        this.userId = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(Long userId, String firstName, String lastName, Date createdAt, Date lastModified) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
    }

    public Long getId() {
        return userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        try {
            return new Jackson2JsonObjectMapper().toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "User{" +
                "id=" + id +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + lastModified +
                '}';
    }
}
