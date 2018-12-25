package io.example.util;

import io.example.DomainEvent;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.function.Consumer;

/**
 * This class is an abstract implementation of a reactive application-level dual-write transaction that can use
 * any Spring Data reactive driver and any Spring Cloud Stream binder. A reactive app-level dual-write is a
 * high-performance implementation of an atomic transaction between two systems partitioned by the network.
 * <p>
 * The message broker acts as the final commit log for a database transaction, allowing two systems of record,
 * since there is an atomic cascading fault that will cause a transactional rollback to occur if either
 * distributed system fails during a reactive stream to request.
 *
 * @param <T> is the Spring Data reactive entity class that will be saved in a non-blocking atomic commit.
 * @author Kenny Bastani
 * @author Stephane Maldini
 */
public abstract class AbstractDualWriter<T> implements DualWriteFunction<T> {

    private final Logger logger = Loggers.getLogger(AbstractDualWriter.class);

    public Mono<T> dualWriteFunction(Mono<T> entityExistsResult, Consumer<T> throwConflictError, Mono<T> databaseResult,
                                     Consumer<? super Throwable> databaseFailure, Consumer<T> writeToBroker) {
        return entityExistsResult.doOnNext(throwConflictError)
                .switchIfEmpty(databaseResult)
                .doOnError(databaseFailure)
                .doOnNext(writeToBroker);
    }

    @Transactional
    public Mono<T> dualWrite(Source broker, Mono<T> existsQuery, Mono<T> saveQuery, DomainEvent<T> event, Long timeout) {
        return dualWriteFunction(existsQuery, (entity) -> {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "Entity already exists");
        }, saveQuery, (ex) -> {
            logger.error("There was an error attempting to save an entity", saveQuery, ex);
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }, (entity) -> {
            // If the database operation fails, a domain event should not be sent to the message broker
            logger.info(String.format("Database request is pending transaction commit to broker: %s",
                    saveQuery.toString()));
            try {
                // Set the entity payload after it has been updated in the database, but before being committed
                event.setSubject(entity);
                // Attempt to perform a reactive dual-write to message broker by sending a domain event
                broker.output().send(MessageBuilder.withPayload(event).build(), timeout);
                // The application dual-write was a success and the database transaction can commit
            } catch (Exception ex) {
                logger.error(String.format("A dual-write transaction to the message broker has failed: %s",
                        saveQuery.toString()), ex);
                // This error will cause the database transaction to be rolled back
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "A transactional error occurred");
            }
        });
    }
}
