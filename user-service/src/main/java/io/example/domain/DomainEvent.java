package io.example.domain;

import java.io.Serializable;

/**
 * A domain event is an abstract class that describes a behavior within a domain.
 *
 * @param <T>  is the type of domain object that this event applies to.
 * @param <ID> is the type of identity for the domain event.
 * @author Kenny Bastani
 */
public abstract class DomainEvent<T, ID> implements Serializable {

    private ID id;
    private Long createdAt;
    private Long lastModified;

    public DomainEvent() {
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public abstract T getSubject();

    public abstract void setSubject(T subject);

    public abstract EventType getEventType();

    public abstract void setEventType(EventType eventType);

    @Override
    public String toString() {
        return "DomainEvent{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                '}';
    }
}
