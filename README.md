# WhaTap Monitoring Sample Application

> **Language:** English | [한국어](README.ko.md)

A Spring Boot microservices application used as hands-on lab material for [WhaTap APM](https://docs.whatap.io/en/java/introduction) training.

```
client
  └─ ingress / localhost:8080
       └─ gateway ──► eureka
            └─ product-composite
                 ├─ product ──► inventory   (sync HTTP, deep tracing demo)
                 ├─ recommendation
                 └─ review
            └─ kafka ─ mongodb ─ mysql ─ zookeeper

  k6-traffic (in-cluster) ──► gateway:8080  # ~3 RPS constant
```

## Learning Path

| Chapter | Topic |
|---------|-------|
| [10 – Primer](docs/10-primer.md) | Microservices, containers, k8s, observability — at a glance |
| [20 – Architecture](docs/20-architecture.md) | Service map, call chain, Spring profiles |
| [30 – Docker Compose Quickstart](docs/30-quickstart-docker-compose.md) | Build, run, verify locally |
| [40 – WhaTap Monitoring](docs/40-whatap-monitoring.md) | Agent injection, naming, weaving, log sink |
| [50 – Reading the WhaTap Dashboard](docs/50-whatap-dashboard.md) | Hitmap, traces, Apdex — how to read what you see |
| [60 – Kubernetes Quickstart](docs/60-quickstart-kubernetes.md) | Deploy to k8s with Kustomize |
| [70 – Traffic Simulation](docs/70-traffic-simulation.md) | k6 constant traffic, legacy scripts |
| [80 – Exercises](docs/80-exercises.md) | 4 hands-on practice steps |
| [90 – Troubleshooting](docs/90-troubleshooting.md) | Symptom → fix reference |

> **Recommended order:** 10 (if needed) → 30 → 40 → 50 → 60 → 70 → 80
