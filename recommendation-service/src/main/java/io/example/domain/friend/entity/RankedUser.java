package io.example.domain.friend.entity;

import io.example.domain.user.entity.User;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Objects;

/**
 * Get a list of {@link User} ranked by a weight. This model is used to get a ranked number of my friend's
 * mutual friends who I am not yet friends with. This is a typical recommendation query you would find on
 * most social networks.
 *
 * @author Kenny Bastani
 */
@QueryResult
public class RankedUser {

    private User user;
    private Integer weight;

    public RankedUser() {
    }

    public RankedUser(User user, Integer weight) {
        this.user = user;
        this.weight = weight;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "RankedUser{" +
                "user=" + user +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankedUser that = (RankedUser) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(weight, that.weight);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, weight);
    }
}
