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

### Source Code

The entire application is contained in [src/com/soklet/barebones/App.java](src/com/soklet/barebones/App.java), which is reproduced below.

```java
public class App {
  @GET("/")
  public String index() {
    return "Hello, world!";
  }

  @GET("/test-input")
  public Response testInput(@QueryParameter Integer input) {
    return Response.withStatusCode(200)
        .headers(Map.of("Content-Type", List.of("application/json; charset=UTF-8")))
        // A real application would not construct JSON in this manner
        .body(String.format("{\"input\": %d}", input))
        .build();
  }

  public static void main(String[] args) throws Exception {
    SokletConfig sokletConfig = SokletConfig.withHttpServer(
        HttpServer.withPort(8080).host("127.0.0.1").build()
    ).build();

    Soklet soklet = Soklet.run(sokletConfig);
    System.out.println("Soklet Barebones App started on port 8080");
    soklet.awaitShutdown();
  }
}
```

The app listens on `http://127.0.0.1:8080/`. Use Ctrl+C to stop it; Soklet shuts down automatically with the JVM. For scoped runs and embedded applications, see [Application Lifecycle](https://www.soklet.com/docs/application-lifecycle).

### Building and Running

Requires JDK 21+ to be installed on your machine.  If you need one, Amazon provides [Corretto](https://aws.amazon.com/corretto/) - a free-to-use-commercially, production-ready distribution of [OpenJDK](https://openjdk.org/) that includes long-term support.

#### Build

```shell
javac -parameters -processor com.soklet.SokletProcessor -cp soklet-4.0.0.jar -d build src/com/soklet/barebones/App.java
```

#### Run (use Ctrl+C to stop)

```shell
java -cp soklet-4.0.0.jar:build com/soklet/barebones/App
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
