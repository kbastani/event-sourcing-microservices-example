## CQRS and Event Sourcing with Spring Boot, Docker, and Kubernetes

This project is a practical microservices reference example for demonstrating the basics of CQRS and Event Sourcing with Spring Boot and Spring Cloud. This tutorial walks you through getting this example up and running on Kubernetes using **Docker Stacks**. If you're unfamiliar with Kubernetes–no worries!–everything you need to get started is contained in this tutorial.

## Microservices for Social Networks

For this example, I've chosen to build a social network using microservices. A social network's domain graph provides a simple model that has a high-degree of complexity as friendships are established between users. The complexity of a graph can force microservice teams to become confused about the ownership of complicated features, such as generating friend recommendations for a user. Without the right architectural best practices, teams may resort to sophisticated caching techniques or ETLs—or worse: generating recommendations using HTTP calls that exponentially decrease performance.

<img src="https://imgur.com/Uqd7SHE.png" width="400" alt="Domain graph of users and friends">
<br/>

## Architecture

In the architecture diagram below, you'll see a component diagram that describes an event-driven microservices architecture that contains two domain services and one aggregate service (a read-only projection of replicated domain data provided as a service).

<img src="https://imgur.com/DUEhtBH.png" width="480" alt="Event sourcing architecture diagram">

### Microservice Specifications

The reference example has two microservices and one read-only replica of domain data that is stitched together from events streamed into Apache Kafka. This means that we can hydrate a different database technology with each event from multiple different microservices. By running these events in order, we can create one eventually consistent read-only projection of domain data stored in separate systems of record!

With this approach we can get the best of both worlds—the large shared database that was easier to query from a monolith—without sacrificing the many benefits of building microservices.

 ***Domain Services***
  - *User Service*
    - Framework: Spring Boot 2.0.7
    - Database: H2/MySQL
    - Messaging: Apache Kafka
    - Broker: Apache Kafka
    - Messaging: Producer
    - Practices: CQRS
  - *Friend Service*
    - Framework: Spring Boot 2.0.7
    - Database: H2/MySQL
    - Broker: Apache Kafka
    - Messaging: Producer
    - Practices: CQRS

***Aggregate Services***
  - *Recommendation Service*
    - Framework: Spring Boot 2.0.7
    - Database: Neo4j 3.5.0
    - Broker: Apache Kafka
    - Messaging: Consumer
    - Practices: Event Sourcing

## Usage

This is the first reference example that I've put together that uses *Docker Compose* to deploy and operate containers on *Kubernetes*.

**Docker Desktop Community v2.0** recently released an experimental feature that allows you to use *Docker Compose* files to deploy and operate distributed systems on any Kubernetes cluster (locally or remote). I think that this is a major advancement for developers looking to get up and running with Kubernetes and microservices as quickly as possible. Prior to this feature, (*over the last 5 years*), developers with Windows-based development environments found it difficult to run my examples using Docker. I'm proud to say that those days are now over.

### Docker Stacks on Kubernetes

**Docker Stacks** is a feature that now allows you to deploy realistically complex microservice examples to any remote or local Kubernetes clusters.

## Installation

First, if you haven'y already, download *Docker Desktop Community Edition* for your operating system of choice. You can choose between *Windows or Mac* from Docker's download page.

 - https://www.docker.com/products/docker-desktop

*Make sure that you are using version 2.0+ of Docker Desktop.*

### Pre-requisites

You'll need to do the following pre-requisites to enable Docker Stacks to perform Kubernetes deployments.

Pre-requisites:

- Install `kubectl` (https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- Install `minikube` (https://kubernetes.io/docs/tasks/tools/install-minikube/)
- Enable `Kubernetes` (From Docker Desktop)
- Turn on `Experimental Features` (From Docker Desktop)

You can easily tackle the last two pre-requisites by configuring the Docker preferences pane—which can be found from the menu in the Docker Desktop system tray.

#### How does it work?

Docker Desktop will use your `kubectl` configuration to provide you a list of Kubernetes clusters that you can target for a stack deployment. By default, Docker Desktop provides you a ready-to-go Kubernetes node called `docker-for-desktop-cluster` that runs locally. Or you can setup your own local cluster using `mini-kube`.

Once you have finished the pre-requisites, you should adjust your Docker system memory to roughly 8GB. You can find these settings, again, in the Docker Desktop system tray.

### Docker Compose Classic (Local)

The `docker-compose.yml` file that is in the root directory of this project will provide you with a `v3.3` Docker Compose manifest that you can use to run this application locally or to deploy to Kubernetes/Docker Swarm. To run the example locally without using a container orchestrator, simply run the following commands.

```bash
docker-compose up -d
docker-compose logs -f
```

You'll see a flurry of system logs flash before your eyes as multiple containers in the distributed system begin to spin up. It's recommended that you wait until the logging comes to a slow halt. In another tab, ensure that all of the containers are running.

```bash
docker-compose ps
```

If all of the services have successfully started, that means you're ready to start playing with the application. Skip forward

### Docker Stacks (Kubernetes or Swarm)

Running Docker Compose locally is an ephemeral way to quickly spin up a distributed system on your local machine. This has been a successful feature for the last 4 years and has allowed developers to quickly run distributed systems without configuration.

The problem posed by running Docker Compose locally is that most developers often do not have the system memory available to performantly run some of my more complex examples. **Docker Stacks** allows you to use **Docker Compose** to deploy a multi-container applications, such as the social network in this repository—targeting an orchestrator that is either Kubernetes or Docker Swarm. This choice is up to you, but for this example, I will demonstrate how to easily deploy to a Kubernetes cluster using **Docker Stacks**.

### Deploying to Kubernetes

Make sure that you've completed the pre-requisites listed in an earlier section of this README. Once you've done that, select the Kubernetes cluster that you would like to deploy to using the *Docker Desktop System Tray Menu*. You should find this icon in either the top right of your MacOS or at the bottom right of your Windows OS. By default, `docker-for-desktop` should be selected, which is a Kubernetes cluster running on your local machine. To see where Docker discovers these Kubernetes clusters, you can run the following formatted command using `kubectl config view`.


```bash
kubectl config view -o \
  jsonpath='{"\n\033[1mCLUSTER NAME\033[0m\n"}{range .clusters[*]}{.name}{"\n"}{end}'
```

If you have any Kubernetes clusters added to your `kubectl config`, you'll see something similar to the following output.

```bash
CLUSTER NAME
docker-for-desktop-cluster
gke_kubernetes-engine-205004_us-central1-a_cluster-1
gke_kubernetes-engine-205004_us-east1-b_istio-cluster
kubernetes-the-hard-way
minikube
```

It doesn't matter which cluster you decide to use–whether it is running locally–or if you have a remote cluster setup and managed by a cloud provider. With **Docker Stacks**, you'll be able to deploy this example on any Kubernetes cluster you have configured as a target in `kubectl config`.

#### Ready to Deploy

Now that you've selected a target Kubernetes cluster, it's time to deploy the example contained inside this repository. Using one simple command, this example will be deployed using the meta data contained inside the `docker-compose.yml` file.


```bash
docker stack up event-sourcing --compose-file $(pwd)/docker-compose.yml
```

After running this command, the services contained in the `docker-compose.yml` file will begin to be deployed to pods in your Kubernetes cluster. You should see the following output when the applications are up and running.


```bash
Waiting for the stack to be stable and running...

neo4j: Ready                    [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
discovery-service: Ready        [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
kafka: Ready                    [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
recommendation-service: Ready   [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
friend-service: Ready           [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
edge-service: Ready             [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
zookeeper: Ready                [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
user-service: Ready             [pod status: 1/1 ready, 0/1 pending, 0/1 failed]
```

As you can see, each one of our applications and backing services (such as Kafka, Neo4j, and Zookeeper), were successfully deployed and started on your Kubernetes cluster. To verify that the pods are successfully running, you can use the `kubectl get pod` command for more info.

You should see something similar to the following output.

```shell
NAME                                      READY     STATUS    RESTARTS   AGE
discovery-service-9c44459b8-c5qln         1/1       Running   0          1m
edge-service-6cb5d5dc8c-5glzq             1/1       Running   0          1m
friend-service-684d4d758c-r2vfg           1/1       Running   0          1m
kafka-7ff94cf4b9-lcs6p                    1/1       Running   0          1m
neo4j-5fcf84c4d5-x2llp                    1/1       Running   0          1m
recommendation-service-7c84655985-lfpzd   1/1       Running   0          1m
user-service-6d7fb74ffc-p8fg4             1/1       Running   0          1m
zookeeper-5fd96b9b9f-8dfw8                1/1       Running   0          1m
```

## Building a Social Network

That's it! All you need to do now is to explore the reference example. For extra credit, you can add in a service mesh, such as Istio, and add sidecars to each of these applications. Istio will provide you with distributed tracing and metrics. Since we are using **Spring Cloud Eureka** in this example, there won't be much of a need to use Istio for discovery.

You'll now be able to navigate to Eureka at the following URL:

 - http://localhost:8761

You should see that each of the microservices have registered with Eureka. You won't be able to directly navigate to the URIs contained in instance registry. That's because these URIs are a part of the network overlay that is being used by your Kubernetes cluster. We can think of these IPs as private, which are not directly mapped to a gateway. Thankfully, we have a Spring Cloud gateway that is accessible and mapped to your `localhost` (*if you've deployed these services to a local Kubernetes cluster*).

#### API Gateway

The *Edge Service* application is an API gateway that simplifies, combines, and secures access to the potentially many different REST APIs exposed by different microservices. For our simple social networking backend, we have a few simple APIs exposed by the gateway.

- **Create User**
  - POST http://localhost:9000/user/v1/users
- **Get User**
  - GET http://localhost:9000/user/v1/users/{0}
- **Update User**
  - PUT http://localhost:9000/user/v1/users/{0}
- **Add Friend**
  - POST http://localhost:9000/friend/v1/users/{0}/commands/addFriend?friendId={1}
- **Remove Friend**
  - POST http://localhost:9000/friend/v1/users/{0}/commands/removeFriend?friendId={1}
- **Get Friends**
  - GET http://localhost:9000/friend/v1/users/{0}/friends
- **Mutual Friends**
  - GET http://localhost:9000/recommendation/v1/friends/{0}/commands/findMutualFriends?friendId={1}
- **Friend Recommendation**
  - GET http://localhost:9000/recommendation/v1/friends/{0}/commands/recommendFriends

## Commands and Queries

CQRS is simply a way to structure your REST APIs so that stateful business logic can be captured as a sequence of events. Simple CRUD operations have dominated web services for the better part of the last two decades. With microservices, we should make sure we structure our REST APIs in the same way that we would build a command-line application. Can you imagine trying to build a CLI application using only REST APIs that implemented CRUD?

For each aggregate that we have in our microservice applications, we must only use *command* APIs to mutate state as a result of how business logic applies to domain data.

### Event Sourcing and CQRS

The *User Service* is responsible for storing, exposing, and managing the data of a social network's users. The query model for a user's profile is created by applying commands in the form of business logic triggered by an API. Each command API are applied to a `User` object and will generate a domain event that describes what happened.

A stream of endless domain events are piped into a `User` topic that is stored in Apache Kafka. Think of Apache Kafka as our DNA store for a distributed system—allowing us to exactly replicate and create projections of domain data that are distributed across many different applications and databases.

<img src="https://imgur.com/DUEhtBH.png" width="480" alt="Event sourcing architecture diagram">

The same idea applies to the *Friend Service*. Every time a user adds a friend, a command is triggered that generates an event that describes exactly what happened. All of these events can be sequenced in the exact order they are received from the front-end users.

There is a metaphor I often use for event sourcing in microservice. It helps to think that each domain microservice is a musical instrument in an orchestra. As each instrument among many. Each instrument is playing a stream of notes that form a composition. When a musician plays a single note, an event occurs, projecting a harmonic wave into the ears of an audience. Each of these different instruments are playing in parallel and are combined together to form a single symphony of sound.

Now, our aggregate store—the *Recommendation Service*—could be thought of as a kind of recording studio that combines the different channel sources into one single musical track. The newly formed single track is then recorded to a disk or tape, making it immutable—as an exact replica of the symphony that cannot be edited. We can create as many copies as we want, and distribute them all over the world without worrying about the original symphony being corrupted or accidentally modified. That's the beauty of read-only aggregate services. They are like our recording studios that can mix or remix the original multi-channel tracks and combine them into one immutable projection of past behavior.

<img src="https://imgur.com/Uqd7SHE.png" width="400" alt="Domain graph of users and friends">
<br/>

The *Recommendation Service* can use a single projected view of the many silos of domain data and provide new powerful querying capabilities that do not require HTTP traversals. These aggregate services can be used for machine learning, predictive analytics, or any use case that requires combining streams of domain data together into a single eventually consistent data structure.

## Friend of a Friend Recommendations

Let's take a look at a seemingly simple query that can quickly become a monster that wrecks havoc as computational complexity rears its ugly head. A simple friend of a friend query should be simple, right? Well let's rethink that idea. Since our friend relationships are stored separate from our users, we will need to make a single HTTP request for each friend, and their friends. To generate a single friend recommendation, we would need to make 101 HTTP requests if everyone in our social network had only 100 friends. This is obviously not an option if you ever wanted to sleep again as an on-call developer or operator with a pager.

What would be better is if we used the best tool for the job to generate eventually consistent friend recommendations. The best tool in this case would be Neo4j, a graph database that eats these kinds of queries for breakfast and then asks for seconds and thirds. Let's take a look at a friend of a friend query in Neo4j.

```java
MATCH (user:User {id: 1}), (friend:User {id: 2}),
  (user)-[:FRIEND]-(mutualFriends)-[:FRIEND]-(friend)
return mutualFriends
```

This is called a Cypher query, and it's designed as ASCII art to resemble the connections we are querying in the graph database. The SQL-like syntax above is a basic friend-of-a-friend query.

## Friend Recommendations

But what if I wanted to know who I should be friends with? That's a bit more complicated. We need to rank the number of mutual connections that a user's friends have. We then need to eliminate the mutual friends from that list, resulting in a ranked list of people who a user is not friends with yet. This is a *non-mutual friend of a friend query*. Is your head spinning yet? Well it's actually not hard at all if you think about it visually.

The question we are asking is similar to the last one, except we want to find the friends of my friends who have the most mutual friends with me, that I am not yet friends with yet.

```java
// Match all the friends of my friends
MATCH (me:User {userId: 1})-[:FRIEND]-(friends),
	(nonFriend:User)-[:FRIEND]-(friends)

// Now remove anyone I'm friends with
WHERE NOT (me)-[:FRIEND]-(nonFriend)

// Count the number of mutual friends
WITH nonFriend, count(nonFriend) as mutualFriends

// Return all non-friends
RETURN nonFriend, mutualFriends

// Rank by the most mutual friends
ORDER BY mutualFriends DESC
```

Using Spring Data Neo4j, each of these queries can be mapped to repository methods that make it easy to something that would otherwise be very difficult and costly to do. It may not make sense to have everyone use Neo4j across a microservice architecture. There are domains that are just not that complex, which really benefit from a relational database model. Also, a majority of developers understand and already have experience with RDBMS. Where a database like Neo4j really shines is when you have a small team of data scientists who are able to work with service developers to performantly operationalize some of the more complex queries that would be extremely costly if implemented on top of a more traditional data store.

## Conventional Best Practices

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


# License

This project is licensed under Apache License 2.0.
