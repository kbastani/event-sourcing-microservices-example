package io.example.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public abstract class AbstractEntity<ID> implements Serializable {

    public abstract ID getId();

    public abstract void setId(ID id);

    public abstract Timestamp getCreatedAt();

    public abstract void setCreatedAt(Timestamp createdAt);

    public abstract Timestamp getLastModified();

    public abstract void setLastModified(Timestamp lastModified);

}
