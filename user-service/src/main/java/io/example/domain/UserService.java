package io.example.domain;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * The {@link UserService} contains methods for managing the transactional state of {@link User} entities. Each
 * method that alters the state of a {@link User} entity will allow you to specify a callback {@link Consumer<User>}.
 * The callback function provides you with a reference to the pre-committed {@link User} entity, allowing you to
 * perform a dual-write to a separate application before finalizing the transaction and committing the result to the
 * attached database.
 *
 * @author Kenny Bastani
 */
@Service
public class UserService {

    private final R2dbcEntityTemplate template;

    public UserService(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * Create a new {@link User} with a supplied callback {@link Consumer<User>} that will allow you to submit a domain
     * event to a third-party system, such as Apache Kafka, before finalizing the commit.
     *
     * @param user     is the {@link User} entity to create.
     * @param callback is a {@link Consumer<User>} that will allow you to throw an exception to rollback the TX.
     * @return a {@link Mono<User>} that emits the result of the transaction in the form of the committed {@link User}.
     */
    public Mono<User> create(User user, Consumer<User> callback) {

        return template.insert(user)
                .map(User::getId)
                .flatMap(id -> template.select(User.class)
                        .matching(Query.query(where("id").is(id)))
                        .first()
                        .single()).delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)));
    }

    /**
     * Uses a non-transactional database client to find a {@link User} by ID.
     *
     * @param id is the ID of the {@link User} that should be found.
     * @return a {@link Mono<User>} that emits the result of the database lookup.
     */
    public Mono<User> find(Long id) {
        return template.select(User.class)
                .matching(Query.query(where("id").is(id)))
                .one()
                .single();
    }

    /**
     * Updates an existing {@link User} entity by its unique ID.
     *
     * @param user     is {@link User} model that contains the updates to be applied to the existing entity.
     * @param callback is a {@link Consumer<User>} that allows you to either commit or rollback the transaction.
     * @return a {@link Mono<User>} that emits the result of the update transaction.
     */
    public Mono<User> update(User user, Consumer<User> callback) {
        Assert.notNull(user.getId(), "User ID must not be null");

        AtomicReference<Long> userId = new AtomicReference<>();
        userId.set(user.getId());

        return template.update(User.class)
                .matching(Query.query(where("id").is(user.getId())))
                .apply(Update.update("first_name", user.getFirstName())
                        .set("last_name", user.getLastName()))
                .then(template.selectOne(Query.query(where("id").is(user.getId())), User.class).single())
                .delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))
                .single();
    }
}
