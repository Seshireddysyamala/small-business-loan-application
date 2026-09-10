# Project Structure and Learning Guide

This repository uses a simple layered structure. Folder names describe the job performed by the classes inside them, and test packages mirror production packages.

## Top level

```text
small-business-loan-application/
|-- backend/                 Loan Application API and browser UI
|-- underwriting-service/   Independent Kafka underwriting worker
|-- docs/                    Architecture and learning notes
`-- README.md                Setup, run, and test instructions
```

## Backend service

Java root: `backend/src/main/java/com/seshi/loanapplication`

```text
loanapplication/
|-- config/                  Spring and Kafka configuration
|-- controller/              HTTP endpoint definitions
|-- dto/                     API request and response objects
|-- entity/                  JPA database entities
|-- exception/               API exceptions and global error handling
|-- mapper/                  Conversion between DTOs and entities
|-- messaging/
|   |-- event/               Kafka message contracts and enums
|   |-- publisher/           Outbound application-event producers
|   `-- subscriber/          Inbound underwriting-decision consumers
|-- repository/              Spring Data database access
|-- service/                 Application use cases and transaction logic
|-- validation/              Custom form/business validation
`-- SmallBusinessLoanBackendApplication.java
```

Other backend folders:

```text
backend/
|-- database/                Optional manual MySQL Workbench scripts
|-- postman/                 Importable API test collection
|-- src/main/resources/
|   |-- db/migration/        Flyway migrations used by the application
|   `-- static/              Browser UI: HTML, JavaScript, and CSS
|-- compose.yaml             Local MySQL
`-- compose.kafka.yaml       Local Kafka
```

## Underwriting service

Java root: `underwriting-service/src/main/java/com/seshi/underwriting`

```text
underwriting/
|-- config/                  Kafka topic and property configuration
|-- domain/                  Demo decision rules and their result
|-- entity/                  JPA underwriting-decision entity
|-- messaging/
|   |-- event/               This service's Kafka contract types
|   |-- publisher/           Outbound decision-event producer
|   `-- subscriber/          Inbound application-event consumer
|-- repository/              Underwriting database access
|-- service/                 Idempotent decision workflow
`-- UnderwritingServiceApplication.java
```

## Best order to read the request flow

Follow these files in order to see one HTTP request travel through the whole system:

1. `backend/.../controller/CompanyController.java` receives the request.
2. `backend/.../service/CompanyServiceImpl.java` runs the use case and transaction.
3. `backend/.../mapper/CompanyMapper.java` maps DTOs and the entity.
4. `backend/.../repository/CompanyRepository.java` saves the application.
5. `backend/.../messaging/publisher/KafkaLoanApplicationEventPublisher.java` publishes after commit.
6. `underwriting-service/.../messaging/subscriber/LoanApplicationEventSubscriber.java` receives it.
7. `underwriting-service/.../service/UnderwritingDecisionService.java` handles idempotency.
8. `underwriting-service/.../domain/UnderwritingPolicy.java` calculates the demo result.
9. `underwriting-service/.../messaging/publisher/KafkaLoanDecisionEventPublisher.java` returns the result.
10. `backend/.../messaging/subscriber/LoanDecisionEventSubscriber.java` receives the decision.
11. `backend/.../messaging/subscriber/LoanDecisionEventHandler.java` updates the application status.

## Where to make common changes

| If you want to change... | Look here first |
| --- | --- |
| REST URLs or HTTP status codes | `backend/.../controller` |
| Request fields or validation annotations | `backend/.../dto/CompanyRequest.java` |
| Custom validation rules | `backend/.../validation` |
| Database columns | Entity plus the next `db/migration` Flyway script |
| Create/update workflow | `backend/.../service` |
| Kafka payload fields | Both services' `messaging/event` packages |
| Underwriting rules | `underwriting-service/.../domain/UnderwritingPolicy.java` |
| Kafka broker/topic settings | Each service's `application.properties` and `config` package |
| Browser form or styling | `backend/src/main/resources/static` |
| API examples | `backend/postman` |

Never edit generated files under `target/`. Maven recreates that directory on every build.
