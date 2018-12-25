package io.example;

import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * The repository for managing {@link User} data.
 *
 * @author Kenny Bastani
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query("SELECT * FROM users u WHERE u.user_id = $1 LIMIT 1")
    Mono<User> getUser(Long id);
}
