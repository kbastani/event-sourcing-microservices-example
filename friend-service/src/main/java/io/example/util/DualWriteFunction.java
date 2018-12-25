package io.example.util;

import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public interface DualWriteFunction<T> {

    Mono<T> dualWriteFunction(Mono<T> entityExistsResult, Consumer<T> throwConflictError,
                              Mono<T> writeToDatabase, Consumer<? super Throwable> databaseFailure,
                              Consumer<T> writeToKafka);

    Mono<Void> dualDeleteFunction(Mono<T> entityExistsResult, Consumer<T> throwConflictError,
                              Mono<Void> deleteFromDatabase, Consumer<? super Throwable> databaseFailure,
                              Consumer<T> writeToKafka);
}
