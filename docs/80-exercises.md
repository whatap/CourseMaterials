# 80 – Exercises

> **Language:** English | [한국어](80-exercises.ko.md)

**You'll learn:**
- How to configure WhaTap agent naming for multi-instance visibility
- How to enable library-level tracing with `weaving`
- How distributed tracing shows latency across a multi-hop call chain

> **Prerequisites:** [30 – Docker Compose Quickstart](30-quickstart-docker-compose.md) complete and services running.

---

## Exercise 1 – Agent Naming

**Goal:** Make individual service instances identifiable in the WhaTap dashboard when scaled.

### Steps

1. Start with `docker-compose-whatap-1.yml` (already running).  
   Open the WhaTap dashboard — note that agent names are cryptic IP-based strings.

2. Switch to the named configuration:
   ```bash
   docker-compose -f docker-compose-whatap-1.yml down
   docker-compose -f docker-compose-whatap-2.yml up -d
   ```

3. Scale product-composite to 5 instances:
   ```bash
   docker-compose -f docker-compose-whatap-2.yml scale product-composite=5
   ```

4. Reload the WhaTap dashboard.

### Expected Observation
You should see **5 separate agents** named `product-composite-<ip2>-<ip3>` (e.g. `product-composite-17-3`, `product-composite-17-5`).  
The `{ip2}-{ip3}` macro expands to the last two octets of each container's IP at startup.

→ Reference: [WhaTap agent naming docs](https://docs.whatap.io/en/java/agent-name)

**Try next:** Scale down to 1 and watch agents disappear from the dashboard.

---

## Exercise 2 – Gray Area (Library Tracing)

**Goal:** See Spring Boot internal library calls (WebClient, reactive scheduler) appear as distinct spans in the Hitmap trace.

### Steps

1. Open [whatap/whatap.conf](../whatap/whatap.conf) and confirm:
   ```
   weaving=spring-boot-2.5
   ```
   This line should already be present. If not, add it.

2. Restart the services to pick up the config change:
   ```bash
   docker-compose -f docker-compose-whatap-1.yml restart
   ```

3. Make a few requests:
   ```bash
   curl http://localhost:8080/product-composite/1
   ```

4. Open the WhaTap Hitmap, drag a rectangle over recent dots, and click into a trace.

### Expected Observation
You should see **library-level spans** inside each trace — for example, `spring-webflux`, `netty`, or `spring-reactive` appear as sub-steps rather than a black-box transaction.

→ Reference: [WhaTap weaving docs](https://docs.whatap.io/en/java/agent-weaving) | [Transaction profile options](https://docs.whatap.io/en/java/trs-profile#additional-options)

**Try next:** Comment out `weaving=spring-boot-2.5`, restart, and compare the trace depth.

---

## Exercise 3 – Log Integration

**Goal:** Correlate application logs with traces in the WhaTap Log menu.

### Steps

1. Open [whatap/whatap.conf](../whatap/whatap.conf) and confirm:
   ```
   logsink_enabled=true
   ```

2. Add the grok phaser pattern for gateway route logs. In the WhaTap console, go to **Log → Log Setting → Parser** and add:
   ```
   %{TIMESTAMP_ISO8601:date}\s+%{LOGLEVEL:log_level}\s+%{NUMBER:pid}\s+---\s+\[%{DATA:thread}\]\s+%{DATA:logger_class}\s+:\s+Mapping\s+\[Exchange:\s+%{WORD:http_method}\s+%{URI:url}\]\s+to\s+Route\{id='%{DATA:route_id}',\s+uri=%{DATA:route_uri},\s+order=%{NUMBER:order},\s+predicate=%{DATA:predicate},\s+match trailing slash:\s+%{WORD:match_trailing_slash},\s+gatewayFilters=\[%{DATA:gateway_filters}\],\s+metadata=\{%{DATA:metadata}\}\}
   ```

3. Make a request, then check:
   - **Log menu** — filter by service name to see structured log entries
   - **Profile menu** — click a trace to see the correlated log lines inline

### Expected Observation
Gateway route mapping logs like:
```
2025-03-21 08:31:51.001 DEBUG 1 --- [or-http-epoll-1] o.s.c.g.h.RoutePredicateHandlerMapping   :
Mapping [Exchange: GET http://localhost:8080/product-composite/1] to Route{id='product-composite', ...}
```
...appear parsed and structured in the WhaTap Log view.

→ Reference: [WhaTap log integration docs](https://docs.whatap.io/en/log/log-java)

**Try next:** Filter logs by `log_level=DEBUG` to isolate gateway routing activity.

---

## Exercise 4 – Inventory Service & Trace Depth

**Goal:** Observe the full 4-hop distributed trace and understand how deep-chain latency appears in WhaTap.

### Background

The call chain is:
```
gateway → product-composite → product → inventory
```

Both `product` and `inventory` inject intentional random sleep:
- **product** ([ProductServiceImpl.java](../microservices/product-service/src/main/java/se/magnus/microservices/core/product/services/ProductServiceImpl.java) line 76): `10–100 ms`
- **inventory** ([InventoryServiceImpl.java](../microservices/inventory-service/src/main/java/se/magnus/microservices/core/inventory/services/InventoryServiceImpl.java) line 71): `10–3000 ms`

The `product → inventory` call uses `WebClient` (async HTTP). If inventory is slow or unavailable, product continues gracefully — the trace still shows the failed/slow span.

### Steps

1. Ensure services are running and traffic is flowing (`make k6-start` or `./stress-test.sh 3 1`).

2. Open the WhaTap **Hitmap** — you should see dots spread across a latency range of ~10ms to ~3s.

3. Drag over the high-latency dots (top of the chart) and open the trace.

4. Expand the trace tree to see all 4 service hops.

### Expected Observation
- The **inventory** span shows the longest duration (up to 3 seconds)
- The **product** span wraps inventory — its total duration includes the inventory call
- The **product-composite** span wraps all three downstream services in parallel (`Mono.zip`)
- Total end-to-end latency is dominated by the `inventory` sleep

**Try next:** Stop the inventory service (`docker-compose stop inventory`) and observe how the trace changes — product completes faster, but inventory shows a timeout/error span.

---

**Next:** [90 – Troubleshooting](90-troubleshooting.md)
