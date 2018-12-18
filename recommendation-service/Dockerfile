FROM openjdk:11.0.1-jre-slim-sid

CMD ["/usr/bin/java", "-jar", "/usr/share/myservice/myservice.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/share/myservice/myservice.jar