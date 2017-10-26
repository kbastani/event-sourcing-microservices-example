# Introduction to Event Sourcing in Microservices

This repository is a microservice reference example that is intended to teach the basics of event sourcing in Spring Boot applications.

## System Architecture

For this reference, I chose to create a simple example domain with a high degree of relationships between data stored on separate microservices. In the architecture diagram below, you'll see an abstract component diagram that describes an event-driven microservice architecture containing two domain services and one aggregate processor.

![Event sourcing architecture diagram](https://i.imgur.com/oL2sR9i.png)

## Conventions

One of the main problems I see today when describing components of a microservice architecture is a general ambiguity in the roles of separate services. For this reason, this example will describe a set of conventions for the roles of separate services.

### Domain Services

**Domain services** are microservices that own the _system of record_ for a portion of the application's domain.

![Domain service](https://imgur.com/Lgy55OJ.png)

Domain services:

- Manage the storage of domain data that it owns.
- Produce the API contract for the domain data that it owns.
- Produce events when the state of any domain data changes.
- Maintain relationship integrity to domain data owned by other services.

### Aggregate Services

**Aggregate services** are microservices that replicate eventually consistent views of domain data owned by separate _domain services_.

![Aggregate service](https://imgur.com/1jx6rTn.png)

Aggregate services:

- Subscribe to domain events emitted by separate domain services.
- Maintain an ordered immutable event log for events it receives.
- Create connected query projections of distributed domain data.
- Provide performant read-access to complex views of domain data.

# License

This project is licensed under Apache License 2.0.
