package io.example.domain.friend;

import io.example.domain.user.UserClient;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.data.r2dbc.function.convert.MappingR2dbcConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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

	private final TransactionalDatabaseClient transactionalDatabaseClient;
	private final DatabaseClient databaseClient;
	private final MappingR2dbcConverter converter;
	private final UserClient userClient;

	public FriendService(TransactionalDatabaseClient transactionalDatabaseClient, DatabaseClient databaseClient,
	                     MappingR2dbcConverter converter, UserClient userClient) {
		this.transactionalDatabaseClient = transactionalDatabaseClient;
		this.databaseClient = databaseClient;
		this.converter = converter;
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
		// Validate that both friends exist on the user-service
		return Mono.sequenceEqual(userClient.getUser(friend.getUserId()).hasElement(),
				userClient.getUser(friend.getFriendId()).hasElement(), (a, b) -> a && b)
				.doOnNext(valid -> {
					if (!valid) {
						throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
								"The supplied friends do not exist by the friendId or userId");
					}
				}).then(transactionalDatabaseClient.inTransaction(db -> db.insert().into(Friend.class)
						.using(friend)
						.map((o, u) -> converter.populateIdIfNecessary(friend).apply(o, u))
						.first().map(Friend::getId)
						.flatMap(id -> db.execute().sql("SELECT * FROM friend WHERE id=$1")
								.bind(0, id).as(Friend.class)
								.fetch()
								.first()))
						.delayUntil(callback).single());
	}

	/**
	 * Uses a non-transactional database client to all the friends of a user.
	 *
	 * @param userId is the ID of user to find friends for.
	 * @return a {@link Flux<Friend>} that emits the result of the database lookup.
	 */
	public Flux<Friend> findUserFriends(Long userId) {
		return databaseClient.execute().sql("SELECT * FROM friend WHERE user_id=$1 LIMIT 1")
				.bind(0, userId).as(Friend.class)
				.fetch()
				.all();
	}

	/**
	 * Uses a non-transactional database client to find a {@link Friend} by ID.
	 *
	 * @param id is the ID of the {@link Friend} that should be found.
	 * @return a {@link Mono<Friend>} that emits the result of the database lookup.
	 */
	public Mono<Friend> find(Long id) {
		return databaseClient.execute().sql("SELECT * FROM friend WHERE id=$1 LIMIT 1")
				.bind(0, id).as(Friend.class)
				.fetch()
				.one();
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

		return transactionalDatabaseClient.inTransaction(db ->
				db.execute().sql("UPDATE friend SET friend_id=$1, user_id=$2 WHERE id=$3 RETURNING *")
						.bind(0, friend.getFriendId())
						.bind(1, friend.getUserId())
						.bind(2, friend.getId()).as(Friend.class).fetch().rowsUpdated()
						.then(db.execute().sql("SELECT * FROM friend WHERE id=$1")
								.bind(0, friendId.get())
								.as(Friend.class)
								.fetch()
								.first()).delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))).single();
	}

	/**
	 * Deletes an existing {@link Friend} entity by its unique ID.
	 *
	 * @param friend   is {@link Friend} model that contains the updates to be applied to the existing entity.
	 * @param callback is a {@link Consumer<Friend>} that allows you to either commit or rollback the transaction.
	 * @return a {@link Mono<Friend>} that emits the result of the update transaction.
	 */
	public Mono<Friend> delete(Friend friend, Consumer<Friend> callback) {
		return transactionalDatabaseClient.inTransaction(db ->
				db.execute().sql("SELECT * FROM friend f WHERE f.user_id=$1 AND f.friend_id=$2 LIMIT 1")
						.bind(0, friend.getUserId())
						.bind(1, friend.getFriendId())
						.as(Friend.class).fetch().first()
						.flatMap(f -> {
							friend.setId(f.getId());
							return db.execute().sql("DELETE FROM friend f WHERE f.id=$1")
									.bind(0, f.getId())
									.fetch()
									.rowsUpdated();
						}).single().then(Mono.just(friend))
						.delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))).single();
	}

	public Mono<Boolean> exists(Long userId, Long friendId) {
		return databaseClient.execute().sql("SELECT * FROM friend WHERE user_id=$1 AND friend_id=$2")
				.bind(0, userId)
				.bind(1, friendId)
				.as(Friend.class).fetch().all().hasElements();
	}
}
