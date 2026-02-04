# Java SDK Example

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Configuration

Create a `.env` file in the example directory with your CCAI credentials:

```bash
CCAI_CLIENT_ID=your-client-id
CCAI_API_KEY=your-api-key
```

Load the environment variables before running:

```bash
export $(cat .env | xargs)
```

**Important:** Before running the application, open `src/main/java/com/example/javasdk/JavaSdkTestApplication.java` and update the Account information in both `runSMSTest()` and `runMMSTest()` methods from the default `"John", "Doe", "+14158906431"` to your own contact details for testing.

## Building the Java Test Main Application

To build the example app, run:

```bash
mvn clean install
```

This will compile the code and create a Spring Boot executable JAR file at:
```
target/javasdk-test-1.0.0.jar
```

## Running the Application

Run the Spring Boot application with a test type argument:

**To send an SMS:**
```bash
java -jar target/javasdk-test-1.0.0.jar sms
```

**To send an MMS:**
```bash
java -jar target/javasdk-test-1.0.0.jar mms
```

Or use the Spring Boot Maven plugin:

**For SMS:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=sms
```

**For MMS:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=mms
```

## Expected Output

**SMS Test:**
```
Testing CCAI Java SDK...
SMS Test Result: PASS | Name: John Doe | Phone: +14158906431 | Response ID: 141292
```

**MMS Test:**
```
Testing CCAI Java SDK MMS...
MMS Test Result: PASS | Name: John Doe | Phone: +14158906431 | Response ID: 141292
```

## Notes

- The MMS test uses the `CloudContactAI.png` image from `src/main/resources/`
- Both tests use the test environment by default (`useTestEnvironment = true`)
- Test results show PASS/FAIL status with contact information and response ID
