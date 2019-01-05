package io.example.domain;

public abstract class DomainEvent<T> {

    private T subject;

    public DomainEvent() {
    }

    public DomainEvent(T subject) {
        this.subject = subject;
    }

    public T getSubject() {
        return subject;
    }

    public void setSubject(T subject) {
        this.subject = subject;
    }
}
