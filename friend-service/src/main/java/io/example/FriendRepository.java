package io.example;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository for managing {@link Friend} entities.
 *
 * @author Kenny Bastani
 */
public interface FriendRepository extends JpaRepository<Friend, Long> {
}
