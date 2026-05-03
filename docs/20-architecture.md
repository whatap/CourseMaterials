# 20 – Architecture

> **Language:** English | [한국어](20-architecture.ko.md)

**You'll learn:**
- What services exist and how they connect
- Which databases and message broker each service uses
- How Spring profiles control runtime behaviour

---

## Service Map

| Service | Local Port | DB | Kafka | Main Endpoint |
|---------|-----------|-----|-------|---------------|
| gateway | 8080 | – | – | routes all traffic |
| eureka | 8761 | – | – | service registry UI |
| product-composite | 7000 | – | producer | `GET /product-composite/{productId}` |
| product | 7001 | MongoDB | consumer | `GET /product/{productId}` |
| recommendation | 7002 | MongoDB | consumer | `GET /recommendation?productId=` |
| review | 7003 | MySQL | consumer | `GET /review?productId=` |
| inventory | 7004 | MongoDB | consumer | `GET /inventory/{productId}` |

> In Docker / Kubernetes all services listen on port **8080** internally. The local ports above apply to bare-`localhost` runs only.

---

## Call Chain

```
client
  └─ GET /product-composite/{id}
       └─ gateway  (Spring Cloud Gateway)
            └─ product-composite  (orchestrator, reactive Mono.zip)
                 ├─ product          ──► inventory   (WebClient, sync HTTP)
                 ├─ recommendation
                 └─ review
```

**Async (Kafka):** `product-composite` publishes CREATE / DELETE events; `product`, `recommendation`, `review`, and `inventory` each consume from their own topic partition.

**Why this matters for tracing:** The `product → inventory` hop adds a second synchronous call deep in the chain. Both services inject random latency on purpose — this is the distributed tracing depth you will observe in WhaTap.

---

## Spring Profiles Cheatsheet

| Profile | Effect |
|---------|--------|
| `docker` | Switches hostnames to container DNS (`eureka`, `mongodb`, `kafka`, `mysql`) and all ports to `8080` |
| `kafka` | Switches message binder from RabbitMQ to Kafka; disables RabbitMQ health check |
| `streaming_partitioned` | Enables 2-partition Kafka topics with partition-key-expression |
| `streaming_instance_0` | This consumer reads partition 0 |
| `streaming_instance_1` | This consumer reads partition 1 (used for scaled-out replicas) |

Active combination in Kubernetes (`env-configmap.yaml`):
```
SPRING_PROFILES_ACTIVE=docker,streaming_partitioned,streaming_instance_0,kafka
```

---

## Glossary

| Term | One-line definition | Reference |
|------|---------------------|-----------|
| Microservice | An independently deployable service with a single responsibility | [martinfowler.com](https://martinfowler.com/articles/microservices.html) |
| Service Discovery | Mechanism for services to find each other by name, not by IP | [Eureka docs](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/) |
| API Gateway | Single entry point that routes client requests to downstream services | [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) |
| Kafka | Distributed event streaming platform used for async messaging | [kafka.apache.org](https://kafka.apache.org/intro) |
| APM | Application Performance Monitoring — traces, metrics, and logs from running services | [WhaTap APM](https://docs.whatap.io/en/java/introduction) |
| Distributed Tracing | Tracking a single request as it flows through multiple services | [OpenTelemetry](https://opentelemetry.io/docs/concepts/observability-primer/) |

---

**Next:** [30 – Docker Compose Quickstart](30-quickstart-docker-compose.md)
