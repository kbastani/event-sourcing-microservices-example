package io.example.domain.friend;

import io.example.domain.friend.entity.Friend;
import io.example.domain.friend.entity.RankedUser;
import io.example.domain.user.entity.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing {@link User} data and friend connections.
 *
 * @author Kenny Bastani
 */
@Repository
public interface FriendRepository extends Neo4jRepository<Friend, Long> {

    @Query("MATCH (userA:User)-[r:FRIEND]->(userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "DELETE r")
    void removeFriend(Long fromId, Long toId);

    @Query("MATCH (userA:User), (userB:User)" +
            "WHERE userA.userId={0} AND userB.userId={1} " +
            "CREATE (userA)-[:FRIEND]->(userB)")
    void addFriend(Long fromId, Long toId);

    @Query("MATCH (userA:User), (userB:User)\n" +
            "WHERE userA.userId={0} AND userB.userId={1}\n" +
            "MATCH (userA)-[:FRIEND]-(fof:User)-[:FRIEND]-(userB)\n" +
            "RETURN DISTINCT fof")
    List<User> mutualFriends(Long fromId, Long toId);

    @Query("MATCH (me:User {userId: {0}})-[:FRIEND]-(friends),\n" +
            "\t(nonFriend:User)-[:FRIEND]-(friends)\n" +
            "WHERE NOT (me)-[:FRIEND]-(nonFriend)\n" +
            "WITH nonFriend, count(nonFriend) as mutualFriends\n" +
            "RETURN nonFriend as User, mutualFriends as Weight\n" +
            "ORDER BY Weight DESC")
    ArrayList<RankedUser> recommendedFriends(Long userId);
}
