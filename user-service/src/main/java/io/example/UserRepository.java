package io.example;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The repository for managing {@link User} data.
 *
 * @author Kenny Bastani
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
