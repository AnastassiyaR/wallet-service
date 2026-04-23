# Wallet Service

A small core banking service for managing accounts, balances, and transactions, with RabbitMQ event publishing.

---

## Instructions on how to build and run applications

The only requirement is Docker:

```bash
docker compose up --build
```

This starts PostgreSQL, RabbitMQ, and the application. The database schema is applied automatically via Flyway on startup.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (guest / guest)

---

## **Explanation of important choices in the solution**

**Atomic balance updates**
Deposits and withdrawals use a single SQL statement:

```
UPDATE balance
SET available_amount = available_amount ± amount
WHERE account_id = ? AND currency = ?
RETURNING *
```

This guarantees atomicity and removes race conditions.  
No separate read is required.

**No redundant `SELECTs`**
The service does not re‑read account or balance after updating them.  
This ensures deterministic responses under concurrency and improves throughput.

**RabbitMQ publishing after commit**
Events are published using `@TransactionalEventListener(phase = AFTER_COMMIT)`.  
If the database transaction rolls back, no message is sent.

**MyBatis with explicit SQL**
All SQL is written in XML mappers.  
This avoids ORM magic and makes performance predictable.

**Flyway for schema management**
The database schema is applied automatically on startup.  
The reviewer does not need to run SQL manually.

**Validation at multiple layers**
- Bean Validation for request structure
- Business rules in the service layer
- Database constraints (`CHECK`, `UNIQUE`) as a final safety net

---

## Estimate how many transactions your account application can handle per second on your development machine

Performance was measured on a Lenovo IdeaPad Slim 5 (AMD Ryzen, Linux) using ApacheBench with two runs:

**Run 1** (`amount: 1.00` - growing balance, variable response length):
```bash
ab -n 1000 -c 10 -p tx.json -T application/json http://localhost:8080/transactions
```
~189 TPS, median 40 ms, max 350 ms.  
Ab reported 993 "failed requests" - all were HTTP 200, but response body length varied as `balanceAfter`
grew (1.00 to 10.00 to 100.00). Ab flags length mismatches as failures, not actual errors.

**Run 2** (`amount: 0.01` - stable response length):
```bash
ab -n 1000 -c 10 -p tx_fixed.json -T application/json http://localhost:8080/transactions
```
~255 TPS, 0 failed requests, median 31 ms, max 328 ms.

The bottleneck is the round-trip to a single PostgreSQL instance running inside Docker.
On a real server (8-16 vCPU), the same architecture typically reaches 600–1200 TPS.

---

## Describe what you have to consider to be able to scale applications horizontally

The application is stateless, so multiple instances can run behind a load balancer without coordination. A few things
to keep in mind:

- Each instance opens its own database connection pool. When scaling out, the total number of connections grows and can
hit PostgreSQL's limit - this needs to be accounted for in configuration.
- The atomic `UPDATE` pattern is safe across instances - the database handles concurrent writes to the same row without 
distributed locks.
- RabbitMQ is already external, so multiple instances can publish independently.
- Flyway uses an internal lock, so parallel startups won't cause duplicate migrations.
- Read endpoints can be routed to a PostgreSQL read replica to reduce load on the primary.

---

## Explanation of the usage of AI

I tried to avoid AI as much as possible during this assignment. Even when I did use it, I always made sure to actually
understand and analyze what was generated before moving on.

The areas where I used it: MyBatis (first time working with it, so I used AI to get started but studied how it works
along the way), Dockerfile (I understand it well, but wanted a second opinion to make sure I wasn't missing
anything - AI is also useful for catching things you didn't know you were doing wrong), RabbitMQ configuration
(I am familiar with RabbitMQ, but never set it up from scratch in Spring), and tests (I have experience writing tests,
but used AI to double-check that all important cases were covered and nothing was missed). I also used it for this
README to help phrase things clearly.

In all cases the output was reviewed and adjusted manually.