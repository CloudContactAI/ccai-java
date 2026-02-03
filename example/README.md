# Java SDK Example

## Building the Main Library

First, build the main CCAI Java SDK library from the parent directory:

```bash
cd ..
./mvnw clean install
```

## Building the Application

To build the example app, run:

```bash
./mvnw clean package
```

This will compile the code and create a JAR file at:
```
target/javasdk-test-1.0.0.jar
```

## Running the Application

```bash
java -jar target/javasdk-test-1.0.0.jar
```
