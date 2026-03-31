# Kafka Application

This repository contains a local Kafka and PostgreSQL based fraud-processing setup.

## Modules

- `src/main/java/com/kafka/application`
  Java Spring Boot fraud-processing application. It consumes `orders`, evaluates fraud signals, stores the result in PostgreSQL, and publishes a `fraud-results` event.
- `OrderProcessor`
  Kotlin Spring Boot module that consumes `orders`, stores the processed record in PostgreSQL, and publishes a fraud result event to `fraud-results`.

## Local Setup

### Prerequisites

- Java 17
- Docker with Compose support
- Gradle wrapper access

### Start PostgreSQL and Kafka

From the repository root:

```bash
docker compose down -v
docker compose up -d
```

This starts:

- PostgreSQL on `localhost:5432`
- ZooKeeper on `localhost:2181`
- Kafka on `localhost:9092`

It also creates:

- PostgreSQL databases: `frauddb`, `orderdb`
- Kafka topics: `orders`, `fraud-results`

### Useful Docker Commands

```bash
docker compose ps
docker compose logs postgres
docker compose logs zookeeper
docker compose logs kafka
docker compose logs kafka-init
```

## Kubernetes on Docker Desktop

You can also run this project on Docker Desktop Kubernetes for deployment-style local testing.

### Prerequisites

- Docker Desktop Kubernetes enabled
- `kubectl` connected to Docker Desktop

Check the current context:

```bash
kubectl config current-context
```

### Build the app image locally

From the repository root:

```bash
docker build -f OrderProcessor/Dockerfile -t order-processor:local .
```

### Deploy the Kubernetes resources

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/order-processor.yaml
```

### Check deployed resources

```bash
kubectl get all -n kafka-app
kubectl get jobs -n kafka-app
kubectl logs job/kafka-topic-init -n kafka-app
kubectl logs deployment/order-processor -n kafka-app
```

### Produce a message to Kafka in Kubernetes

Kafka is exposed on `localhost:30092` through a NodePort service, so you can publish locally with:

```bash
./bin/kafka-console-producer.sh --bootstrap-server localhost:30092 --topic orders
```

Sample message:

```json
{"order_id":"ord-1001","user_id":"user-42","amount":12000.0,"currency":"INR","location":"Bengaluru"}
```

To read the output topic:

```bash
./bin/kafka-console-consumer.sh --bootstrap-server localhost:30092 --topic fraud-results --from-beginning
```

## Application Configuration

### Root Java app

Config file:
- [application.yml](/Users/samskrathishetty/prep/Kafka-Application/src/main/resources/application.yml)

Defaults:

- datasource URL: `jdbc:postgresql://localhost:5432/frauddb`
- Kafka bootstrap server: `localhost:9092`
- input topic: `orders`
- output topic: `fraud-results`

### Kotlin OrderProcessor

Config file:
- [application.yml](/Users/samskrathishetty/prep/Kafka-Application/OrderProcessor/src/main/resources/application.yml)

Defaults:

- datasource URL: `jdbc:postgresql://localhost:5432/orderdb`
- Kafka bootstrap server: `localhost:9092`
- input topic: `orders`
- output topic: `fraud-results`

## Running the Kotlin Module

```bash
./gradlew :OrderProcessor:bootRun
```

If Gradle dependencies changed, run:

```bash
./gradlew :OrderProcessor:clean :OrderProcessor:bootRun
```

## Local Kafka Testing

### Produce a sample input event

Open a Kafka producer:

```bash
docker compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic orders
```

If you are running Kafka locally outside Docker, you can also use:

```bash
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders
```

Then paste this sample message:

```json
{"order_id":"ord-1001","user_id":"user-42","amount":12000.0,"currency":"INR","location":"Bengaluru"}
```

### View produced fraud result events

```bash
docker compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fraud-results --from-beginning
```

### List Kafka topics

```bash
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## Message Flow

1. A producer publishes an `OrderEvent` to the `orders` topic.
2. The Kafka consumer reads the event.
3. Fraud logic evaluates the order.
4. The processed record is stored in PostgreSQL.
5. A `FraudResultEvent` is published to `fraud-results`.

## Logic Summary

The Kotlin `OrderProcessor` currently uses simple fraud rules:

- amount greater than `10000.0` is marked as fraud
- fraud reason becomes `HIGH_VALUE_ORDER`
- otherwise the order is marked clean with reason `CLEARED`

The Java app contains a richer rule set in [FraudDetectionService.java](/Users/samskrathishetty/prep/Kafka-Application/src/main/java/com/kafka/application/service/FraudDetectionService.java):

- high-value transactions increase risk
- country mismatches increase risk
- crypto payments increase risk
- unusually large item counts increase risk

Based on total risk score, the order is classified as:

- `CLEAN`
- `REVIEW`
- `FRAUD`

## Important Files

- [docker-compose.yml](/Users/samskrathishetty/prep/Kafka-Application/docker-compose.yml)
- [init-multiple-dbs.sql](/Users/samskrathishetty/prep/Kafka-Application/docker/postgres/init-multiple-dbs.sql)
- [OrderConsumer.kt](/Users/samskrathishetty/prep/Kafka-Application/OrderProcessor/src/main/kotlin/com/retail/fraudservice/messaging/OrderConsumer.kt)
- [OrderService.kt](/Users/samskrathishetty/prep/Kafka-Application/OrderProcessor/src/main/kotlin/com/retail/fraudservice/service/OrderService.kt)
- [FraudResultEvent.kt](/Users/samskrathishetty/prep/Kafka-Application/OrderProcessor/src/main/kotlin/com/retail/fraudservice/model/FraudResultEvent.kt)
- [FraudProcessingService.java](/Users/samskrathishetty/prep/Kafka-Application/src/main/java/com/kafka/application/service/FraudProcessingService.java)

## Troubleshooting

- If Kafka or Postgres startup looks stale, run `docker compose down -v` and bring the stack back up.
- If PostgreSQL errors mention dialect or datasource setup, confirm the containers are running before starting Spring Boot.
- If Kafka topic creation fails, check `docker compose logs kafka-init`.
