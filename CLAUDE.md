# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

**Build and Run (Maven):**
`./mvnw clean install`
`./mvnw run` (or specific service run commands)

**Run Tests (JUnit/TestNG):**
To run all tests: `./mvnw test`

**Run Specific Service:**
To run a specific service (e.g., the main service): `./mvnw spring-boot:run` (or relevant execution command)

## Code Architecture

The repository is structured as a multi-module Maven project, containing several distinct services:

**Key Modules and Directories:**

*   `capture-service/` : Likely contains the service responsible for capturing data.
*   `processor-service/` : Likely contains the core business logic and processing logic.
*   `ui-service/` : Likely contains the service related to the user interface or API endpoints.
*   `common/` : Contains shared code, utilities, or configuration.
*   `pom.xml` : Defines the Maven project structure and dependencies.

**Core Architecture:**
The application is composed of several microservices or logical components (`capture`, `processor`, `ui`), coordinated through a common dependency structure defined in `pom.xml`. Future development should focus on understanding the flow of data and dependencies between these services.