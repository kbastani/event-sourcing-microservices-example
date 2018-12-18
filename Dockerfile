FROM openjdk:8-alpine

RUN wget -O /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.2/dumb-init_1.2.2_amd64 && \
chmod 755 /usr/local/bin/dumb-init

RUN mkdir -p /opt/app

WORKDIR /opt/app

EXPOSE 8080

CMD ["-jar", "event-sourcing-microservices-basics.jar"]

ENTRYPOINT ["/usr/local/bin/dumb-init", "java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Xmx256m", "-Djava.security.egd=file:/dev/urandom"]

COPY target/event-sourcing-microservices-basics.jar event-sourcing-microservices-basics.jar
