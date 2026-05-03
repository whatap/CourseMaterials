# 40 – WhaTap Monitoring

> **Language:** English | [한국어](40-whatap-monitoring.ko.md)

**You'll learn:**
- How WhaTap APM is injected into Java services
- The difference between the two docker-compose configurations
- Key `whatap.conf` options you will edit during the exercises

---

## What is WhaTap APM?

WhaTap APM collects traces, metrics, and logs from running JVM processes via a Java agent. The agent attaches at startup — no code changes are required in the application.  
→ [WhaTap Java agent overview](https://docs.whatap.io/en/java/introduction)

---

## Step 1 – Configure License and Server

The agent credentials live in [whatap/whatap.conf](../whatap/whatap.conf).

```
license=x605c8888kr4d-z4in0c7sgi43aa-z554s2prm6qa8d
whatap.server.host=13.124.11.223/13.209.172.35
```

Replace these two values with your own project credentials from the WhaTap console before running.  
→ [How to find your license key](https://docs.whatap.io/en/project/project-manage)

---

## Step 2 – Agent Injection (docker-compose)

Every service in `docker-compose-whatap-1.yml` uses this pattern:

```yaml
environment:
  - JAVA_TOOL_OPTIONS=-javaagent:/app/whatap/whatap.agent-2.2.54.jar
      --add-opens=java.base/java.lang=ALL-UNNAMED
volumes:
  - type: bind
    source: ./whatap        # host directory with whatap.conf + agent JAR
    target: /app/whatap     # mounted into each container
```

The bind-mounted `whatap/` directory contains:
- `whatap.agent-2.2.54.jar` — the agent JAR
- `whatap.conf` — shared config for all services
- `whatap-logsink-lz4-1.7.0.jar` — log sink plugin

---

## Step 3 – Agent Naming: compose-1 vs compose-2

This is the most important teaching difference between the two compose files.

| | `docker-compose-whatap-1.yml` | `docker-compose-whatap-2.yml` |
|-|-------------------------------|-------------------------------|
| Agent name | Auto-generated from container IP | Explicitly set per service |
| `JAVA_TOOL_OPTIONS` extras | none | `-Dwhatap.onode=docker-compose-node`<br>`-Dwhatap.okind=<service-type>`<br>`-Dwhatap.name=<service>-{ip2}-{ip3}` |
| Dashboard appearance | Hard to tell instances apart | `product-composite-17-3`, `product-composite-17-4` — clearly distinct |

**The `{ip2}-{ip3}` macro** expands at runtime to the last two octets of the container IP address. When you scale `product-composite=5`, you see five separately named agents in the WhaTap dashboard.

To switch:
```bash
docker-compose -f docker-compose-whatap-1.yml down
docker-compose -f docker-compose-whatap-2.yml up -d
```

→ See [Exercise 1](80-exercises.md#exercise-1-agent-naming) for the full walkthrough.

---

## Key `whatap.conf` Options

| Option | Current value | What it does |
|--------|--------------|--------------|
| `weaving` | `spring-boot-2.5` | Instruments Spring Boot library internals (WebClient, reactive chains) |
| `logsink_enabled` | `true` | Sends application logs to the WhaTap log sink |
| `mtrace_rate` | `100` | Samples 100 % of transactions for distributed tracing |
| `trace_normalize_urls` | `/product-composite/{productId},...` | Groups dynamic URLs to reduce cardinality in the Hitmap |
| `trace_ignore_url_set` | `/actuator/health` | Excludes health-check endpoints from trace overhead |
| `apdex_time` | `2000` | SLA threshold in ms — requests faster than this count as "satisfied" |
| `profile_http_parameter_enabled` | `true` | Captures HTTP query parameters in traces |
| `profile_sql_param_enabled` | `true` | Captures SQL bind parameters in traces |

---

## Agent Injection on Kubernetes

The `deploy/k8s/overlays/whatap/` overlay is currently a **placeholder**. To wire up real WhaTap APM on Kubernetes you would need:

1. **WhatapAgent CRD** — installed by the WhaTap Kubernetes operator
2. **Pod annotation** — add `whatap.io/inject: "true"` to each Deployment's pod template  
3. **ConfigMap** — mount `whatap.conf` into pods  
4. **Secret** — store the license key  

→ [WhaTap Kubernetes operator docs](https://docs.whatap.io/en/kubernetes/install-master-node-agent)

---

## Utility Scripts

These scripts in `whatap/` help diagnose connectivity issues:

| Script | Purpose |
|--------|---------|
| `whatap/ping.sh` | Tests TCP connectivity from agent to WhaTap server |
| `whatap/resmon.sh` | Validates CPU/memory resource monitoring data collection |
| `whatap/proxy.sh` | Runs the standalone proxy when direct connection is blocked |

Run any script from the project root:
```bash
./whatap/ping.sh
```

---

> **First time looking at the WhaTap dashboard?** Read [50 – Reading the WhaTap Dashboard](50-whatap-dashboard.md) before moving on.

---

**Next:** [60 – Kubernetes Quickstart](60-quickstart-kubernetes.md)
