# 50 – Reading the WhaTap Dashboard

> **Language:** English | [한국어](50-whatap-dashboard.ko.md)

**What you'll learn in this chapter:**
- Which menus to open first (and which to ignore until later)
- How to read the **Hitmap** — the single most useful screen
- How to read a **trace** tree and find the slow span
- What Apdex and the response-time histogram are telling you

> **When to come here:** after chapter 40, once `make k6-start` (or `./stress-test.sh`) is producing traffic and dots are appearing on the dashboard.

---

## Main menus at a glance

| Menu | What it shows |
|------|---------------|
| Dashboard / Overview | Throughput (TPS), avg response time, agent connection status |
| Transactions / Hitmap | Every request as a dot — X = time, Y = response time |
| Profile / Trace | One request expanded: which methods/services were called, and for how long |
| Log | Application logs collected by the agent |
| Topology | Service-to-service call graph |

For this lab, **start with the Hitmap.** Everything else is reachable from it.

---

## Reading the Hitmap

The Hitmap is a scatter plot.

- **One dot = one completed request.**
- **X axis = time** (when the request finished).
- **Y axis = response time** (ms). Higher = slower.
- **Color = status**: normal vs error vs slow.
- **Drag a box** to select a region — the trace list below filters to those requests.

**Recipe for finding a slow request:** drag a box around the dots in the *upper-right* (recent + slow), then click on a single dot to open its trace.

→ Reference: [WhaTap — Hitmap](https://docs.whatap.io/java/hit-map)

---

## Reading a trace tree

A trace is a tree of **spans**. Each span is one unit of work (an HTTP call, a DB query, a method invocation).

- The **top span** is the entry point (here: `gateway`).
- **Indentation** = call depth.
- **Bar length** = total time spent in that step.
- **Bar minus children's bars** = time spent in the step itself, not in things it called.

In this lab, latency is dominated by the deepest leaf — usually `inventory`, which sleeps up to 3 seconds. Walk down the longest bar each level and you'll find it. See [Exercise 4](80-exercises.md#exercise-4-inventory-service--trace-depth) for the same idea as a guided exercise.

→ Reference: [WhaTap — Transaction Profile](https://docs.whatap.io/java/trs-profile)

---

## Apdex and the response-time histogram

**Apdex** is a single 0–1 score summarising user satisfaction, derived from a target response time `T` (set by `apdex_time` in `whatap.conf`):

| Bucket | Range | Counts as |
|--------|-------|-----------|
| Satisfied | `≤ T` | 1.0 |
| Tolerating | `T < x ≤ 4T` | 0.5 |
| Frustrated | `> 4T` | 0.0 |

In this lab `apdex_time=2000`, so anything ≤ 2 s is Satisfied and anything > 8 s is Frustrated. Because `inventory` injects up to 3 s of sleep, expect a meaningful **Tolerating** slice.

→ Reference: [Apdex — apdex.org](https://www.apdex.org/overview.html)

---

## Agent name view (compose-1 vs compose-2)

The same service can show up under two very different names depending on which compose file you used to start it:

| Started from | Agent name shown in the dashboard |
|--------------|-----------------------------------|
| `docker-compose-whatap-1.yml` | Auto-generated — hard to tell instances apart |
| `docker-compose-whatap-2.yml` | `product-composite-{ip2}-{ip3}` etc. — instances clearly distinct |

Scaling `product-composite` to 5 with **compose-2** gives you 5 cleanly named agents — that's the payoff of [Exercise 1](80-exercises.md#exercise-1-agent-naming).

---

## First-look checklist

Open the dashboard and verify:

- [ ] **Six or more agents Connected** (gateway, product-composite, product, recommendation, review, inventory — plus any scaled instances).
- [ ] **Hitmap shows a continuous horizontal band of dots** — traffic is flowing.
- [ ] **Click the highest dot** → the trace expands to a 4-hop tree (gateway → product-composite → product → inventory).

If any of those fails, jump to [90 – Troubleshooting](90-troubleshooting.md).

---

**Next:** [60 – Kubernetes Quickstart](60-quickstart-kubernetes.md) to redeploy the same system on k8s, or [80 – Exercises](80-exercises.md) to start the hands-on labs.
