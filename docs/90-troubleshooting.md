# 90 – Troubleshooting

> **Language:** English | [한국어](90-troubleshooting.ko.md)

A combined quick-reference for both docker-compose and Kubernetes runtimes.

---

## Application Issues

| Symptom | Diagnostic command | Typical fix |
|---------|--------------------|-------------|
| `curl localhost:8080/...` returns 503 | `docker-compose logs gateway` | Gateway not yet registered in Eureka — wait 30s and retry |
| Eureka shows fewer than 6 services | `docker-compose logs <service-name>` | Check `SPRING_PROFILES_ACTIVE` includes `docker` — service may be trying to connect to `localhost` instead of container DNS |
| `product-composite` returns empty recommendations or reviews | Check Kafka consumer logs | Kafka topics not yet created — restart consumer services after Kafka is healthy |
| Response takes > 3 seconds consistently | Expected — `inventory` has up to 3000 ms random sleep | See [Exercise 4](80-exercises.md#exercise-4-inventory-service--trace-depth) |
| `404` on `/product-composite/1` | `curl localhost:8080/actuator/health` | Services may still be starting up — wait and retry |

---

## Docker Compose Issues

| Symptom | Diagnostic command | Typical fix |
|---------|--------------------|-------------|
| Port 8080 already in use | `lsof -i :8080` or `netstat -tlnp \| grep 8080` | Stop the conflicting process before starting docker-compose |
| Container exits immediately | `docker-compose logs <service>` | Check for missing env vars or config errors in the log |
| MongoDB or MySQL CrashLoopBackOff | `docker-compose logs mongodb` | Volume conflict — run `docker-compose down -v` to remove stale volumes |
| Kafka `LEADER_NOT_AVAILABLE` | `docker-compose logs kafka` | Zookeeper not yet ready — `docker-compose restart kafka` after zookeeper is healthy |
| WhaTap agent not appearing | `./whatap/ping.sh` | Check `whatap.conf` — verify `license` and `whatap.server.host` are correct; check firewall / outbound TCP to port 6600 |

---

## Kubernetes Issues

| Symptom | Diagnostic command | Typical fix |
|---------|--------------------|-------------|
| Pod stuck in `Pending` | `kubectl describe pod <name> -n coursematerials` | No available nodes or insufficient resources — check node capacity |
| Pod in `CrashLoopBackOff` | `kubectl logs <pod> -n coursematerials --previous` | Check init container logs first: `kubectl logs <pod> -c wait-deps` |
| Eureka registrations < 6 | `kubectl logs deploy/product -n coursematerials` | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env not set — check ConfigMap |
| Kafka `LEADER_NOT_AVAILABLE` | `kubectl logs kafka-0 -n coursematerials` | `KAFKA_ADVERTISED_HOST_NAME` must be `kafka` (cluster DNS), not an IP |
| MongoDB CrashLoopBackOff | `kubectl logs <mongodb-pod> -n coursematerials -c wait-deps` | Init container `nc -z mongodb 27017` failing — StorageClass PVC issue |
| Ingress returns 503 | `kubectl get endpoints gateway -n coursematerials` | If empty: gateway pod not Ready — check readinessProbe and logs |
| `ImagePullBackOff` | `kubectl describe pod <name> -n coursematerials` | Image not pushed or wrong tag — run `make push-images` and verify tag in `overlays/local/kustomization.yaml` |
| k6 pod not starting | `kubectl describe pod -l app=k6-traffic -n coursematerials` | ConfigMap `k6-script` may be missing — run `make k6-start` |

---

## WhaTap Connectivity

| Symptom | Check | Fix |
|---------|----|-----|
| Agent shows "Disconnected" in dashboard | `./whatap/ping.sh` | Outbound TCP to `13.124.11.223:6600` blocked — check firewall or use `whatap/proxy.sh` |
| No metrics in dashboard after 2+ min | `./whatap/resmon.sh` | Verify `license` and `whatap.server.host` in `whatap.conf` match console values |
| Logs not appearing in Log menu | Check `logsink_enabled=true` in `whatap.conf` | Restart services after editing conf; verify WhaTap log sink plugin JAR is present in `whatap/` |

---

## Useful Commands

```bash
# docker-compose: tail all logs
docker-compose -f docker-compose-whatap-1.yml logs -f

# docker-compose: check container health
docker-compose -f docker-compose-whatap-1.yml ps

# k8s: describe a failing pod
kubectl describe pod <pod-name> -n coursematerials

# k8s: tail logs from a deployment
kubectl logs -f deploy/<service-name> -n coursematerials

# k8s: exec into a pod for debugging
kubectl exec -it <pod-name> -n coursematerials -- sh

# WhaTap: test connectivity
./whatap/ping.sh
```
