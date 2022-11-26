FROM openjdk:17-alpine

CMD ["java", "-jar", "/usr/share/myservice/myservice.jar"]

# Add the service itself
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/share/myservice/myservice.jar