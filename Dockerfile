# To build:
# docker build . --file Dockerfile --tag soklet/barebones-app

# To run (use Ctrl+C to stop):
# docker run -p 8080:8080 soklet/barebones-app

FROM amazoncorretto:25
EXPOSE 8080
ENV RUNNING_IN_DOCKER=true

# Copy in source and dependencies
RUN mkdir -p /app/src
COPY src /app/src
COPY soklet-3.1.0.jar /app

# Build the app
WORKDIR /app
RUN javac -parameters -processor com.soklet.SokletProcessor -cp soklet-3.1.0.jar -d build src/com/soklet/barebones/App.java

# Unprivileged user for runtime
USER 1000

CMD ["/bin/sh", "-c", "exec java -cp soklet-3.1.0.jar:build com/soklet/barebones/App"]
