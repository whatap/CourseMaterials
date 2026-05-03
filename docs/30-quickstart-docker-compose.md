# 30 – Docker Compose Quickstart

> **Language:** English | [한국어](30-quickstart-docker-compose.ko.md)

**You'll learn:**
- How to build and run the full application stack with docker-compose
- How to verify every service is healthy
- How to start and stop background traffic

> **New to microservices, containers, or APM?** Skim [10 – Primer](10-primer.md) (~5 min) before continuing.

---

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java (JDK) | 17+ | [adoptium.net](https://adoptium.net/) |
| Docker | 24+ | [docs.docker.com/get-docker](https://docs.docker.com/get-docker/) |
| Docker Compose | v2 | bundled with Docker Desktop; `docker compose version` to check |
| make | any | pre-installed on macOS/Linux |

> **Windows users:** use WSL2 or Git Bash for all commands below.

---

## Step 1 – Build

```bash
./gradlew clean build -x test
```

This compiles all modules and packages each service as a layered JAR. Skip `-x test` if you want to run the unit tests too (takes ~3 min extra).

---

## Step 2 – Start

```bash
docker-compose -f docker-compose-whatap-1.yml up -d
```

This starts: **gateway, eureka, product-composite, product, recommendation, review, inventory, kafka, zookeeper, mongodb, mysql** — 11 containers total.

Watch startup progress:
```bash
docker-compose -f docker-compose-whatap-1.yml logs -f
```

Wait until you see `Started ...Application` lines for each service (~60–90 seconds).

---

## Step 3 – Verify

**API works:**
```bash
curl http://localhost:8080/product-composite/1
```
Expected: JSON with `productId`, `recommendations`, `reviews`, `inventory` fields.

**Eureka dashboard:**  
Open [http://localhost:8761](http://localhost:8761) — you should see **6 services** registered (gateway, product-composite, product, recommendation, review, inventory).

**Swagger UI:**  
Open [http://localhost:8080/openapi/swagger-ui.html](http://localhost:8080/openapi/swagger-ui.html)

---

## Step 4 – Generate Traffic

Generate constant background traffic so the WhaTap dashboard always shows live data.

**Option A – k6 (recommended):**
```bash
make k6-start
```
> Requires the Kubernetes deployment to be running. See [70 – Traffic Simulation](70-traffic-simulation.md) for details.

**Option B – legacy shell script:**
```bash
chmod +x stress-test.sh
./stress-test.sh 5 0.5
# usage: ./stress-test.sh <concurrency> <delay_seconds>
```

---

## Scaling

```bash
# scale product-composite to 3 instances
docker-compose -f docker-compose-whatap-1.yml scale product-composite=3

# scale back to 1
docker-compose -f docker-compose-whatap-1.yml scale product-composite=1
```

---

## Stop

```bash
docker-compose -f docker-compose-whatap-1.yml down
```

Add `-v` to also remove persistent volumes (MongoDB, MySQL data).

---

**Next:** [40 – WhaTap Monitoring](40-whatap-monitoring.md)
