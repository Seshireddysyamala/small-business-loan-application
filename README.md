# Small Business Loan Application

This project now contains two independent Spring Boot services connected through Kafka:

```text
Postman / browser
      |
      v
Loan Application API (backend) -- loan-application-events --> Underwriting Service
      ^                                                        |
      |                                                        |
      +--------------- loan-decision-events -------------------+
```

- `backend` owns the customer-facing REST API, validation, MySQL application records, and UI.
- `underwriting-service` owns its decision policy and a separate H2 decision database.
- Kafka carries safe application events to underwriting and decision events back to the backend.

The services do not call each other's Java classes or databases. Each service has its own copy of the versioned event contract, which makes the service boundary explicit.

Start with [`docs/PROJECT_STRUCTURE.md`](docs/PROJECT_STRUCTURE.md) for a folder map and a recommended order for reading the code.

## API endpoints

The backend exposes the four existing endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/companies` | Submit a loan application |
| `GET` | `/api/companies/{id}` | Get one application and its current status |
| `PUT` | `/api/companies/{id}` | Update an application |
| `GET` | `/api/companies` | Get all applications |

A successful create or update is committed to MySQL before the backend publishes a `CREATED` or `UPDATED` event. The application starts as `SUBMITTED`; after underwriting returns a decision, it becomes `APPROVED`, `DECLINED`, or `MANUAL_REVIEW`.

## Run the complete flow

Docker is only needed here for MySQL and Kafka. Copy `backend/.env.example` to `backend/.env` and replace the development passwords first.

Open four PowerShell terminals:

1. Start MySQL:

   ```powershell
   cd backend
   docker compose up -d
   ```

2. Start Kafka:

   ```powershell
   cd backend
   docker compose -f compose.kafka.yaml up -d
   ```

3. Start the Loan Application API:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

4. Start the Underwriting Service:

   ```powershell
   cd underwriting-service
   ..\backend\mvnw.cmd -f pom.xml spring-boot:run
   ```

Import `backend/postman/Small-Business-Loan-API.postman_collection.json` into Postman. Run `1 - Create loan application`, then run `2 - Get application by id` with the saved `applicationId`. The returned `applicationStatus` should change from `SUBMITTED` to the underwriting result.

The service logs show the complete route without exposing personal data:

```text
Kafka event delivered ...                              # backend -> application topic
Loan application event received ...                    # underwriting received it
Loan decision published ...                            # underwriting -> decision topic
Loan decision received ... / Loan decision applied ... # backend updated MySQL
```

To inspect both topics directly:

```powershell
cd backend
docker compose -f compose.kafka.yaml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic loan-application-events --from-beginning --property print.key=true
```

In another terminal, replace the topic with `loan-decision-events` to inspect decisions.

Stop the infrastructure when finished:

```powershell
cd backend
docker compose -f compose.kafka.yaml down
docker compose down
```

## Test without Docker

Both projects use embedded Kafka and in-memory H2 during tests, so these commands do not require a locally running broker or MySQL:

```powershell
cd backend
.\mvnw.cmd test

cd ..\underwriting-service
..\backend\mvnw.cmd -f pom.xml test
```

The Kafka integration tests verify all important boundaries: the backend publishes a safe application event, underwriting consumes it and emits an idempotent decision, and the backend consumes that decision and updates the stored status.

## Demo underwriting policy

The current policy is deliberately simple and is for learning/testing only—not real credit decisioning:

- It uses business revenue, assets, time in business, requested amount, and a coarse credit-risk band.
- An amount within the calculated demo capacity can be approved.
- Zero revenue is declined.
- Incomplete, very young, high-risk, unsupported-schema, or over-capacity applications go to manual review.
- Re-delivery of the same source event reuses the stored decision ID rather than creating a second decision row.

The application event excludes SSN, contact name, email, phone, and raw credit score. Logs also use IDs and decision metadata only.

## Configuration

The main Kafka settings are shared by environment-variable name so both services connect to the same broker and topics:

- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_LOAN_APPLICATION_TOPIC`
- `KAFKA_LOAN_DECISION_TOPIC`
- `KAFKA_TOPIC_PARTITIONS`
- `KAFKA_TOPIC_REPLICAS`

The independent consumer groups are `KAFKA_UNDERWRITING_CONSUMER_GROUP` and `KAFKA_DECISION_CONSUMER_GROUP`. Set `KAFKA_ENABLED=false` to run either service without Kafka. Set `KAFKA_DECISION_CONSUMER_ENABLED=false` to disable only the backend decision subscriber.

The backend uses Flyway-managed MySQL tables. Underwriting uses its own Flyway-managed H2 file at `underwriting-service/data` by default. The local database files and `.env` files are ignored by Git.

Database commit and Kafka publication are not atomic yet. A transactional outbox is the appropriate next reliability step if guaranteed publication becomes a requirement.
