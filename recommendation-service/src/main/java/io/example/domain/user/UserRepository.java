package io.example.domain.user;

import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link User} data and friend connections.
 *
 * @author Kenny Bastani
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    User findUserByUserId(Long userId);

    @Query("MATCH (userA:User)-[r:FRIEND]->(userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "DELETE r")
    void removeFriend(Long fromId, Long toId);

    @Query("MATCH (userA:User), (userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "CREATE (userA)-[:FRIEND { createdAt: {2}, lastUpdated: {3} }]->(userB)")
    void addFriend(Long fromId, Long toId, Long createdAt, Long lastUpdated);

    @Query("MATCH (userA:User), (userB:User)\n" +
            "WHERE userA.userId={0} AND userB.userId={1}\n" +
            "MATCH (userA)-[:FRIEND]-(fof:User)-[:FRIEND]-(userB)\n" +
            "RETURN DISTINCT fof")
    Streamable<User> mutualFriends(Long fromId, Long toId);

    @Query("MATCH (me:User {userId: {0}})-[:FRIEND]-(friends),\n" +
            "\t(nonFriend:User)-[:FRIEND]-(friends)\n" +
            "WHERE NOT (me)-[:FRIEND]-(nonFriend)\n" +
            "WITH nonFriend, count(nonFriend) as mutualFriends\n" +
            "RETURN nonFriend as user, mutualFriends as weight\n" +
            "ORDER BY weight DESC")
    <T> Streamable<T> recommendedFriends(Long userId, Class<T> clazz);
}
