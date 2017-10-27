# Introduction to Event Sourcing in Microservices

This repository is a microservice reference example that is intended to teach the basics of event sourcing in Spring Boot applications.

## System Architecture

For this reference, I chose to create a simple example domain with a high degree of relationships between data stored on separate microservices. In the architecture diagram below, you'll see an abstract component diagram that describes an event-driven microservice architecture containing two domain services and one aggregate processor.

<img src="https://imgur.com/DUEhtBH.png" width="480" alt="Event sourcing architecture diagram">

## Conventions

One of the main problems I see today when describing components of a microservice architecture is a general ambiguity in the roles of separate services. For this reason, this example will describe a set of conventions for the roles of separate services.

### Domain Services

**Domain services** are microservices that own the _system of record_ for a portion of the application's domain.

<img src="https://imgur.com/bAttimP.png" height="300" alt="Domain service">
<br/>

Domain services:

- Manage the storage of domain data that it owns.
- Produce the API contract for the domain data that it owns.
- Produce events when the state of any domain data changes.
- Maintain relationship integrity to domain data owned by other services.

### Aggregate Services

**Aggregate services** are microservices that replicate eventually consistent views of domain data owned by separate _domain services_.

<img src="https://imgur.com/6c8mJfC.png" height="305" alt="Aggregate service">
<br/>

Aggregate services:

- Subscribe to domain events emitted by separate domain services.
- Maintain an ordered immutable event log for events it receives.
- Create connected query projections of distributed domain data.
- Provide performant read-access to complex views of domain data.

## Example Domain

The example domain is a social network of users who can establish friend relationships with one another. I chose this domain because it has high join complexity when the domain data is split across separate services.

<img src="https://imgur.com/Uqd7SHE.png" width="400" alt="Domain graph of users and friends">
<br/>

The diagram above is a domain graph that shows `User` nodes and `Friend` relationships. In a microservice architecture we may decide to decompose this domain graph into two separate domain services, a `user-service` and a `friend-service`. For this reason, we'll have foreign-key relationships stored in the `friend-service` that reference the unique identity of a `User` stored on the `user-service`. 

# License

This project is licensed under Apache License 2.0.
