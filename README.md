# WorkHub SaaS Backend Phase 1

This is the initial development phase of the WorkHub SaaS backend, covering core Spring Boot structures, database models, Authentication with JWT, and Multi-tenant partitioning strategies.

## Requirements
- Java 17+
- Gradle

## How to Run Locally

You can launch the application directly from the root of the project using the Gradle wrapper:

### Windows
```cmd
gradlew bootRun
```

### Linux/macOS
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`. An H2 in-memory database will be initialized automatically, creating two initial tenants (`Acme Corp` and `Globex`) and two corresponding users.

### Default Accounts
Use these to log in through the `/auth/login` endpoint to receive a JWT:
- admin@acme.com / password
- user@globex.com / password

You can run `./test_flows.sh` on bash to verify the core functionally.
