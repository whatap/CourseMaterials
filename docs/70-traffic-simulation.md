# 70 – Traffic Simulation

> **Language:** English | [한국어](70-traffic-simulation.ko.md)

**You'll learn:**
- Why k6 is preferred over shell-based load scripts
- How k6 is deployed inside the Kubernetes cluster
- How to adjust traffic volume and observe results

---

## Why k6?

The original `stress-test.sh` uses background shell processes — it has no metrics, no ramp control, and no thresholds. k6 gives you:

- **Scenario control** — define VUs (virtual users) and duration declaratively
- **Built-in metrics** — p95 latency, RPS, error rate shown in the terminal
- **Grafana integration** — push metrics to Prometheus / Grafana (already used in this project)
- **Readable scripts** — plain JavaScript, easy to extend with new endpoints

---

## k6 in Kubernetes (current setup)

k6 runs as a **Deployment** inside the `coursematerials` namespace. It calls `http://gateway:8080` directly over the cluster's internal DNS — no Ingress required.

**Start / stop:**
```bash
make k6-start   # apply ConfigMap + Deployment
make k6-stop    # scale Deployment to 0 replicas
make k6-logs    # tail live output
```

**What the script does** ([deploy/k8s/base/load-test/configmap.yaml](../deploy/k8s/base/load-test/configmap.yaml)):

```js
scenarios: {
  steady_traffic: {
    executor: 'constant-vus',
    vus: 3,           // 3 virtual users running concurrently
    duration: '999h', // runs until you scale to 0
  },
},
// Each VU: GET /product-composite/<random 1-1000>, sleep 1s
// Result: ~3 requests/second constant traffic
```

---

## Adjusting Traffic Volume

Edit `deploy/k8s/base/load-test/configmap.yaml` and change `vus`:

```yaml
vus: 3     # change to e.g. 10 for higher load
```

Re-apply:
```bash
make k6-start
# or: kubectl -n coursematerials rollout restart deployment/k6-traffic
```

---

## Running k6 Locally (Docker)

To run against a locally running docker-compose stack:

```bash
# extract the JS from the ConfigMap and run it
docker run --rm -i grafana/k6:0.55.0 run - <<'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 3,
  duration: '2m',
};

export default function () {
  const id = Math.floor(Math.random() * 1000) + 1;
  const res = http.get(`http://host.docker.internal:8080/product-composite/${id}`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
EOF
```

> `host.docker.internal` resolves to the Docker host on macOS/Windows. On Linux, use `--network=host` and `localhost` instead.

---

## Legacy Alternatives

| Script | Usage | When to use |
|--------|-------|-------------|
| `stress-test.sh` | `./stress-test.sh <concurrency> <delay>` | Quick local curl-based load without k8s |
| `test-em-all.bash` | `./test-em-all.bash` | Functional integration test — checks HTTP codes and JSON fields |

---

**Next:** [80 – Exercises](80-exercises.md)
