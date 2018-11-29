package io.example;

import io.example.user.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

/**
 * Repository for managing {@link User} data and friend connections.
 *
 * @author Kenny Bastani
 */
public interface AggregateRepository extends Neo4jRepository<User, Long> {

    @Query("MATCH (userA:User)-[r:FRIENDS]->(userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "DELETE r")
    void removeFriend(Long fromId, Long toId);

    @Query("MATCH (userA:User), (userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "CREATE (userA)-[:FRIENDS]->(userB)")
    void addFriend(Long fromId, Long toId);

    @Query("MATCH (userA:User), (userB:User)\n" +
            "WHERE userA.userId={0} AND userB.userId={1}\n" +
            "MATCH (userA)-[:FRIENDS]->(fof:User)<-[:FRIENDS]-(userB)\n" +
            "RETURN fof")
    List<User> friendOfFriend(Long fromId, Long toId);
}
