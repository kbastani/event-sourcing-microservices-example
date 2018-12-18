FROM openjdk:11.0.1-jre-slim-sid
MAINTAINER Kenny

CMD ["/usr/bin/java", "-jar", "/usr/share/myservice/myservice.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
#ADD target/lib           /usr/share/myservice/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/myservice/myservice.jar