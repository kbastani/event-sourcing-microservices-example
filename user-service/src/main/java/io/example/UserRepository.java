package io.example;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * The repository for managing {@link User} data.
 *
 * @author Kenny Bastani
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
