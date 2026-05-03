# 10 – Primer

> **Language:** English | [한국어](10-primer.ko.md)

**What you'll learn in this chapter:**
- The high-level problem this lab is solving
- The minimum vocabulary you need before reading the rest of the docs
- How a single HTTP request becomes a "distributed trace"

> Skip this chapter if you already work with microservices, containers, Kubernetes, and APM. New to one or more of these? Spend ~5 minutes here first.

---

## The problem this lab demonstrates

In a system split into many independently deployed services, "the API was slow" is no longer enough information. The request may have hit five services and three databases on the way back. **Application Performance Monitoring (APM)** like WhaTap exists to answer questions of the form "*which service, which call, which line was slow this time?*" without adding `println` statements everywhere.

This lab gives you a small but realistic system (7 Spring Boot services + Kafka + 2 databases), runs continuous traffic against it, and lets you see what an APM dashboard looks like with real distributed traces.

---

## Microservices

A **microservice** is an independently deployable service that owns a single responsibility. Instead of one big application, you run many small ones that talk to each other over the network.

```
client → gateway → product-composite ─┬─ product ── inventory
                                       ├─ recommendation
                                       └─ review
```

→ Reference: [Martin Fowler — Microservices](https://martinfowler.com/articles/microservices.html)

---

## Containers and Docker

A **container image** is a packaged, runnable filesystem (your app + JDK + config). A **container** is a running instance of an image. Docker is the most common tool to build and run them. In this lab every service ships as a container image.

→ Reference: [Docker — Get Started](https://docs.docker.com/get-started/)

---

## Kubernetes — four words you must know

Kubernetes (k8s) is a system for running containers across many machines. You only need four concepts to read chapter 60:

| Term | One-line definition |
|------|---------------------|
| Pod | A group of containers that live and die together (usually 1 container) |
| Deployment | A declaration: "keep N copies of this Pod running" |
| Service | A stable in-cluster DNS name in front of a group of Pods |
| Ingress | The HTTP entry point from outside the cluster into Services |

→ Reference: [Kubernetes — Tutorials](https://kubernetes.io/docs/tutorials/kubernetes-basics/)

---

## Observability — the three signals

| Signal | What it is | Example in this lab |
|--------|------------|---------------------|
| Metric | A number over time | `gateway` average response time |
| Log | A timestamped text event | `Mapping [Exchange: GET ...]` |
| Trace | The path of a single request through services | `gateway → product-composite → product → inventory` |

WhaTap collects all three. Most of this course focuses on **traces**, because they are the most useful signal in microservice architectures.

→ Reference: [OpenTelemetry — Observability primer](https://opentelemetry.io/docs/concepts/observability-primer/)

---

## What is APM (and what does WhaTap actually do)?

The WhaTap **Java agent** is a JAR file attached to your JVM at startup via `-javaagent:`. It instruments standard libraries (Spring, JDBC, HTTP clients) using bytecode weaving — **no source-code changes required**. It then ships traces, metrics, and logs to the WhaTap server, where you view them in the dashboard.

→ Reference: [WhaTap Java Agent — Introduction](https://docs.whatap.io/java/introduction)

---

## Distributed tracing — at a glance

One request to `/product-composite/{id}` touches four services. WhaTap records each step as a **span**, and shows them as a tree:

```
client
  └─ gateway ──────────────────────────────────────┐  ← root span
       └─ product-composite ─────────────────┐    │
            ├─ product ──── inventory ─┐    │    │
            ├─ recommendation ─────────┘    │    │
            └─ review ─────────────────────┘    │
                                                 ▼
Timeline (longer bar = more time spent):
├──── gateway ─────────────────────────────────────┤
    ├──── product-composite ───────────────────┤
        ├── product ──────────────┤
                ├── inventory ─────┤   ← usually the longest leaf
        ├── recommendation ──┤
        ├── review ─────┤
```

Random sleeps were intentionally injected: **product** 10–100 ms, **inventory** 10–3000 ms. This makes the trace visually interesting and shows you what slow leaves look like in production.

---

## What you'll actually do

1. Run the system on **docker-compose** (chapter 30).
2. Inject the WhaTap agent and customize agent names (chapter 40).
3. Re-deploy the same system on **Kubernetes** (chapter 60).
4. Run constant background traffic with **k6** (chapter 70).
5. Read the dashboard, find slow requests, and reason about latency (chapters 50 and 80).

---

**Next:** [20 – Architecture](20-architecture.md) for the service inventory, or jump straight to [30 – Docker Compose Quickstart](30-quickstart-docker-compose.md) to start building.
