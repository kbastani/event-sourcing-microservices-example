package io.example.domain.friend;

import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A repository for managing {@link Friend} entities.
 *
 * @author Kenny Bastani
 */
public interface FriendRepository extends ReactiveCrudRepository<Friend, Long> {

    @Query("SELECT f.id, f.user_id, f.friend_id FROM Friend f WHERE user_id = $1 AND friend_id = $2")
    Mono<Friend> getFriend(Long userId, Long friendId);

    @Query("SELECT f.id, f.user_id, f.friend_id FROM Friend f WHERE f.user_id = $1")
    Flux<Friend> getFriends(Long userId);
}
