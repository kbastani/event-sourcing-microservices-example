FROM openjdk:11.0.1-jre-slim-sid

CMD ["/usr/bin/java", "-jar", "/usr/share/myservice/myservice.jar"]

# Add the service itself
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/share/myservice/myservice.jar