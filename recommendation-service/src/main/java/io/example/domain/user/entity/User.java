package io.example.domain.user.entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

import java.util.Random;

/**
 * A projection of the {@link User} domain object that is owned by the
 * user service.
 *
 * @author Kenny Bastani
 */
@NodeEntity
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;

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

    @Override
    public String toString() {
        try {
            return new Jackson2JsonObjectMapper().toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
