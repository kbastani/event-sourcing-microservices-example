package io.example.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.query.Query;
import reactor.core.publisher.Mono;

/**
 * The repository for managing {@link User} data.
 *
 * @author Kenny Bastani
 */
public interface UserRepository extends R2dbcRepository<User, Long> {

    @Query("SELECT * FROM users u WHERE u.id = $1 LIMIT 1")
    Mono<User> getUser(Long id);
}
