FROM amazoncorretto:25
EXPOSE 8080
ENV RUNNING_IN_DOCKER=true

# Copy in source and dependencies
RUN mkdir -p /app/src
COPY src /app/src
COPY soklet-2.0.0-SNAPSHOT.jar /app

# Build the app
WORKDIR /app
RUN javac -parameters -cp soklet-2.0.0-SNAPSHOT.jar -d build src/com/soklet/example/App.java

# Build the app
WORKDIR /app
RUN javac -parameters -processor com.soklet.SokletProcessor -cp soklet-2.0.0-SNAPSHOT.jar -d build src/com/soklet/example/App.java

# Unprivileged user for runtime
USER 1000

CMD ["/bin/sh", "-c", "java -cp soklet-2.0.0-SNAPSHOT.jar:build com/soklet/example/App"]