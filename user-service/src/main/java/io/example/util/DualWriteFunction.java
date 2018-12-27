package io.example.util;

import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface DualWriteFunction<T> {

    Mono<T> dualWriteFunction(Supplier<Mono<T>> saveQuery, Supplier<Mono<T>> lookupQuery,
                              Consumer<T> existsConsumer,
                              Consumer<? super Throwable> databaseFailure,
                              Consumer<T> writeToBroker);
}
