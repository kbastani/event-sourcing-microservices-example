package io.example;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The repository for managing {@link User} data.
 *
 * @author Kenny Bastani
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
}
