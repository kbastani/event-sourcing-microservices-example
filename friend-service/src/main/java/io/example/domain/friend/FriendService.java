package io.example.domain.friend;

import io.example.domain.user.UserClient;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * The {@link FriendService} contains methods for managing the transactional state of {@link Friend} entities. Each
 * method that alters the state of a {@link Friend} entity will allow you to specify a callback {@link Consumer<Friend>}.
 * The callback function provides you with a reference to the pre-committed {@link Friend} entity, allowing you to
 * perform a dual-write to a separate application before finalizing the transaction and committing the result to the
 * attached database.
 *
 * @author Kenny Bastani
 */
@Service
public class FriendService {

    private final R2dbcEntityTemplate template;
    private final UserClient userClient;

    public FriendService(R2dbcEntityTemplate template, UserClient userClient) {
        this.template = template;
        this.userClient = userClient;
    }

    /**
     * Create a new {@link Friend} with a supplied callback {@link Consumer<Friend>} that will allow you to submit a domain
     * event to a third-party system, such as Apache Kafka, before finalizing the commit.
     *
     * @param friend   is the {@link Friend} entity to create.
     * @param callback is a {@code Function<Friend, Publisher<Void>>} that will allow you to throw an exception to rollback the TX.
     * @return a {@link Mono<Friend>} that emits the result of the transaction in the form of the committed {@link Friend}.
     */
    public Mono<Friend> create(Friend friend, Function<Friend, Publisher<Void>> callback) {

        // The userId and friendId must not be the same, as users cannot befriend themselves
        return Mono.just(friend).doOnNext((f) -> {
            if (f.getFriendId().equals(f.getUserId()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "A user cannot befriend oneself");
        }).then(Mono.sequenceEqual(userClient.getUser(friend.getUserId()).hasElement(),
                userClient.getUser(friend.getFriendId()).hasElement(), (a, b) -> a && b).doOnNext(valid -> {
            if (!valid) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "The supplied friends do not exist by the friendId or userId");
            }
        }).then(template.insert(Friend.class)
                .using(friend)
                .map(Friend::getId)
                .flatMap(id -> template.select(Friend.class)
                        .matching(Query.query(where("id").is(id)))
                        .first()
                        .single()).delayUntil(u -> Mono.fromRunnable(() -> callback.apply(u)))
                .delayUntil(callback).single()));
    }

    /**
     * Uses a non-transactional database client to all the friends of a user.
     *
     * @param userId is the ID of user to find friends for.
     * @return a {@link Flux<Friend>} that emits the result of the database lookup.
     */
    public Flux<Friend> findUserFriends(Long userId) {
        return template.select(Friend.class)
                .matching(Query.query(where("user_id").is(userId)))
                .all();
    }

    /**
     * Uses a non-transactional database client to find a {@link Friend} by ID.
     *
     * @param id is the ID of the {@link Friend} that should be found.
     * @return a {@link Mono<Friend>} that emits the result of the database lookup.
     */
    public Mono<Friend> find(Long id) {
        return template.select(Friend.class)
                .matching(Query.query(where("id").is(id)))
                .first();
    }

    /**
     * Updates an existing {@link Friend} entity by its unique ID.
     *
     * @param friend   is {@link Friend} model that contains the updates to be applied to the existing entity.
     * @param callback is a {@link Consumer<Friend>} that allows you to either commit or rollback the transaction.
     * @return a {@link Mono<Friend>} that emits the result of the update transaction.
     */
    public Mono<Friend> update(Friend friend, Consumer<Friend> callback) {
        Assert.notNull(friend.getId(), "Friend ID must not be null");

        AtomicReference<Long> friendId = new AtomicReference<>();
        friendId.set(friend.getId());

        return template.update(Friend.class)
                .matching(Query.query(where("id").is(friend.getId())))
                .apply(Update.update("friend_id", friend.getId())
                        .set("user_id", friend.getUserId()))
                .then(template.selectOne(Query.query(where("id").is(friend.getId())), Friend.class).single())
                .delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))
                .single();
    }

    /**
     * Deletes an existing {@link Friend} entity by its unique ID.
     *
     * @param friend   is {@link Friend} model that contains the updates to be applied to the existing entity.
     * @param callback is a {@link Consumer<Friend>} that allows you to either commit or rollback the transaction.
     * @return a {@link Mono<Friend>} that emits the result of the update transaction.
     */
    public Mono<Friend> delete(Friend friend, Consumer<Friend> callback) {
        return template.select(Friend.class)
                .matching(Query.query(where("user_id").is(friend.getUserId())
                        .and(where("friend_id").is(friend.getFriendId()))))
                .first()
                .flatMap(f -> {
                    friend.setId(f.getId());
                    return template.delete(f);
                }).single().then(Mono.just(friend))
                .delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u))).single();
    }

    public Mono<Boolean> exists(Long userId, Long friendId) {
        return template.exists(Query.query(where("user_id")
                .is(userId).and("friend_id").is(friendId)), Friend.class).single();
    }
}
