# Validation App (CSV → JSON) — Learning Project

This repository is a small Spring Boot learning project that converts a CSV file into JSON and demonstrates a simple REST endpoint and Cucumber functional tests.

It is intended for learning only.

What the project does

- At application startup a CommandLineRunner converts `sample.csv` (from the classpath: `src/main/resources/sample.csv`) into a JSON file called `output.json` (written to the application's working directory).
- A small REST endpoint (`GET /api/persons`) returns the JSON content as a list of `Person` objects (the controller uses `PersonService` which reads `output.json`).
- Cucumber feature(s) are provided under `src/test/resources/features` to exercise the CSV → JSON behavior as functional tests.

Files of interest

- `src/main/java/com/prats/validation/ValidationAppApplication.java` — Spring Boot main class; runs the CSV → JSON conversion on startup.
- `src/main/java/com/prats/validation/service/FileValidationService.java` — Converts CSV to JSON using OpenCSV + Jackson.
- `src/main/java/com/prats/validation/service/PersonService.java` — Reads `output.json` and maps to `Person` objects.
- `src/main/java/com/prats/validation/controller/PersonController.java` — Exposes `GET /api/persons`.
- `src/main/resources/sample.csv` — Sample CSV used by the app.
- `output.json` — The JSON output file produced when the app runs (written to the working directory where the jar is executed).
- `src/test/resources/features` — Cucumber feature files used for functional tests.

Quick notes / caveats

- The app writes `output.json` to the application working directory when it runs. `PersonService` is implemented to load `output.json` via `ClassPathResource` — depending on how you run the app this may or may not find the newly-created file on the classpath. If you hit a 500 or a file-not-found when calling the REST endpoint after running the jar, you can copy `output.json` into `src/main/resources` (or modify `PersonService` to load from the filesystem) — this project is intentionally simple for learning.

Prerequisites

- Java 17+
- Maven (or use the included Maven wrapper `mvnw.cmd` on Windows)
- Docker (optional, only if you want to run the app inside a container)

How to build

From the project root (Windows cmd.exe):

```
:: use the Maven wrapper on Windows
mvnw.cmd clean package
```

This will produce `target/validation-app-0.0.1-SNAPSHOT.jar`.

How to run the application (local jar)

From the project root (the app writes `output.json` into the current working directory):

```
java -jar target\validation-app-0.0.1-SNAPSHOT.jar
```

You should see "CSV to JSON conversion done!" in the logs and a new `output.json` file in the directory where you ran the jar.

Access the REST endpoint (after the app starts):

```
curl http://localhost:8080/api/persons
```

How to run tests (unit + Cucumber functional tests)

Run all tests (including Cucumber features) with Maven/wrapper:

```
mvnw.cmd test
```

If you want to run only the Cucumber tests, you can run the surefire/failsafe configuration you have in the project (or run the feature's test runner class from your IDE).

Docker: build & run

Build the image:

```
docker build -t validation-app .
```

Run the container and map an `out` directory on your host to the container working dir so you can inspect `output.json` after the container runs:

```
:: create an output directory first
mkdir out
:: run and expose port 8080; map host "out" to container "/app"
docker run --rm -p 8080:8080 -v "%cd%/out:/app" validation-app
```

After the container runs, check the `out` folder on your host for `output.json`.

Project purpose

This repository is a learning exercise to demonstrate:

- Reading classpath resources (`sample.csv`).
- Converting CSV to Java objects with OpenCSV.
- Serializing objects to JSON with Jackson.
- Running code at startup with a Spring Boot CommandLineRunner.
- Basic Cucumber feature-driven functional tests.