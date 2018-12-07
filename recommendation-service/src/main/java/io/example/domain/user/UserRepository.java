package io.example.domain.user;

import io.example.domain.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link User} data and friend connections.
 *
 * @author Kenny Bastani
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    User findUserByUserId(Long userId);
}
