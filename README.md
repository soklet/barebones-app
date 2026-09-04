<a href="https://www.soklet.com">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="https://cdn.soklet.com/soklet-gh-logo-dark-v2.png">
        <img alt="Soklet" src="https://cdn.soklet.com/soklet-gh-logo-light-v2.png" width="300" height="101">
    </picture>
</a>

## Soklet Barebones App

Here we demonstrate building and running a single-file "barebones" [Soklet](https://www.soklet.com) application with nothing but the [soklet-4.0.0.jar](https://repo1.maven.org/maven2/com/soklet/soklet/4.0.0/soklet-4.0.0.jar) and the JDK.  There are no other libraries or frameworks, no Servlet container, no Maven build process - no special setup is required.

While a real production system will have more moving parts, this demonstrates that you _can_ build server software without ceremony or dependencies.

If you'd like an example of a production-ready system, see the [Toy Store App](https://www.soklet.com/docs/toystore-app).

Two ways to build and run:

* [Directly from the command-line](#building-and-running-without-docker)
* [Inside of a Docker container](#building-and-running-with-docker)

### Source Code

The entire application is contained in [src/com/soklet/barebones/App.java](src/com/soklet/barebones/App.java), which is reproduced below.

```java
public class App {
  private static final int DEFAULT_HTTP_PORT = 8080;
  private static final String LOOPBACK_HTTP_HOST = "127.0.0.1";
  private static final String LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE =
    "SOKLET_BAREBONES_LOOPBACK_PORT";

  @GET("/")
  public String index() {
    return "Hello, world!";
  }

  @GET("/test-input")
  public Response testInput(@QueryParameter Integer input) {
    return Response.withStatusCode(200)
      .headers(Map.of("Content-Type", Set.of("application/json; charset=UTF-8")))
      // A real application would not construct JSON in this manner
      .body(String.format("{\"input\": %d}", input))
      .build();
  }

  private static int resolveHttpPort(String value) {
    if (value == null)
      return DEFAULT_HTTP_PORT;

    int port;

    try {
      port = Integer.parseInt(value);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(String.format(
        "%s must be an integer from 1 through 65535",
        LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE), exception);
    }

    if (port < 1 || port > 65535)
      throw new IllegalArgumentException(String.format(
        "%s must be an integer from 1 through 65535", LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE));

    return port;
  }

  public static void main(String[] args) throws Exception {
    String loopbackPortOverride = System.getenv(LOOPBACK_HTTP_PORT_ENVIRONMENT_VARIABLE);
    int port = resolveHttpPort(loopbackPortOverride);
    HttpServer.Builder httpServerBuilder = HttpServer.withPort(port);

    if (loopbackPortOverride != null)
      httpServerBuilder.host(LOOPBACK_HTTP_HOST);
    
    SokletConfig sokletConfig = SokletConfig.withHttpServer(
      httpServerBuilder.build()
    ).build();

    // In an interactive console environment, it makes sense to stop on `Enter` keypress.
    // In a Docker container, it makes sense to wait for JVM shutdown (e.g. SIGTERM)
    boolean stopOnEnterKey = !"true".equals(System.getenv("RUNNING_IN_DOCKER"));

    System.out.printf("Starting Soklet Barebones App on port %d\n", port);

    if (stopOnEnterKey) {
      System.out.println("Press [enter] to exit once ready");
      SokletApplication.run(sokletConfig, ShutdownTrigger.ENTER_KEY);
    } else {
      SokletApplication.run(sokletConfig);
    }
  }
}
```

### Building and Running Without Docker

Requires JDK 17+ to be installed on your machine.  If you need one, Amazon provides [Corretto](https://aws.amazon.com/corretto/) - a free-to-use-commercially, production-ready distribution of [OpenJDK](https://openjdk.org/) that includes long-term support.

#### Build

```shell
javac -parameters -processor com.soklet.SokletProcessor -cp soklet-4.0.0.jar -d build src/com/soklet/barebones/App.java
```

#### Run

```shell
java -cp soklet-4.0.0.jar:build com/soklet/barebones/App
```

The application listens on port `8080` with its normal all-interface binding by
default. Local noninteractive automation can instead select an IPv4 loopback
port without changing the source code:

```shell
RUNNING_IN_DOCKER=true SOKLET_BAREBONES_LOOPBACK_PORT=18080 \
  java -cp soklet-4.0.0.jar:build com/soklet/barebones/App
```

### Building and Running With Docker

Requires [Docker](https://www.docker.com/products/docker-desktop/) to be installed on your machine. The entire [Dockerfile](Dockerfile) is reproduced below.

```dockerfile
FROM amazoncorretto:25
EXPOSE 8080
ENV RUNNING_IN_DOCKER=true

# Copy in source and dependencies
RUN mkdir -p /app/src
COPY src /app/src
COPY soklet-4.0.0.jar /app

# Build the app
WORKDIR /app
RUN javac -parameters -processor com.soklet.SokletProcessor -cp soklet-4.0.0.jar -d build src/com/soklet/barebones/App.java

# Unprivileged user for runtime
USER 1000

CMD ["/bin/sh", "-c", "exec java -cp soklet-4.0.0.jar:build com/soklet/barebones/App"]
```

#### Build

```shell
docker build . --file Dockerfile --tag soklet/barebones-app
```

#### Run (use `Ctrl+C` to stop)

```shell
docker run -p 8080:8080 soklet/barebones-app
```

### Testing

#### Happy Path

##### Request

```shell
curl  "http://localhost:8080/"
```

##### Response

```text
Hello, world!
```

#### Query Parameters

##### Request

```shell
curl --verbose "http://localhost:8080/test-input?input=123"
```

##### Response

```text
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /test-input?input=123 HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: application/json; charset=UTF-8
< Date: Sun, 21 Mar 2024 16:19:01 GMT
< 
* Connection #0 to host localhost left intact
{"input": 123}
```

#### Bad Input

##### Request

```shell
curl --verbose "http://localhost:8080/test-input?input=abc"
```

##### Response

```text
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /test-input?input=abc HTTP/1.1
...
< HTTP/1.1 400 Bad Request
< Content-Length: 21
< Content-Type: text/plain; charset=UTF-8
< Date: Sun, 21 Mar 2024 16:19:01 GMT
< 
* Connection #0 to host localhost left intact
HTTP 400: Bad Request
```
