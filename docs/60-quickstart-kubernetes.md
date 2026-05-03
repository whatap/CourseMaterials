# 60 – Kubernetes Quickstart

> **Language:** English | [한국어](60-quickstart-kubernetes.ko.md)

**You'll learn:**
- What cluster-level prerequisites the manifests require
- How to deploy the full stack with a single `kubectl` command
- How to verify pods are healthy and the API is reachable

---

## Architecture on Kubernetes

```
course.192-168-2-200.nip.io
          │
ingress-nginx  (LoadBalancer 192.168.2.200)
          │
       gateway:8080 ──► eureka:8761
          │
  product / recommendation / review / inventory / product-composite
          │
  kafka ─ mongodb ─ mysql ─ zookeeper

  k6-traffic (Deployment) ──► gateway:8080   # constant ~3 RPS
```

---

## Prerequisites

Install these once per cluster. Skip if already present.

### 1 – Default StorageClass
```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.30/deploy/local-path-storage.yaml
kubectl patch storageclass local-path \
  -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```
→ [local-path-provisioner](https://github.com/rancher/local-path-provisioner)

### 2 – MetalLB (LoadBalancer)
```bash
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.9/config/manifests/metallb-native.yaml
```
Then create an `IPAddressPool` and `L2Advertisement` for your LAN IP range.  
→ [MetalLB configuration guide](https://metallb.universe.tf/configuration/)

### 3 – ingress-nginx
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.1/deploy/static/provider/cloud/deploy.yaml
```
→ [ingress-nginx docs](https://kubernetes.github.io/ingress-nginx/)

---

## Quickstart

### Step 1 – Build and push images
```bash
make build-images push-images
```
Images are tagged `jinronara/coursematerials-<service>:0.1` and pushed to Docker Hub.  
Override registry/tag: `make build-images IMAGE_PREFIX=myrepo/ IMAGE_TAG=dev`.

### Step 2 – Create namespace and secret
```bash
kubectl create namespace coursematerials

kubectl -n coursematerials create secret generic coursematerials-secret \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<your-mysql-password>'
```
> **Never commit `secret.yaml` to git.** Always create secrets directly on the cluster.

### Step 3 – Deploy
```bash
kubectl apply -k deploy/k8s/overlays/local
```
Wait for all pods to reach `Running`:
```bash
kubectl -n coursematerials get pods -w
```
Expected: **12 pods Running** (11 services + k6-traffic).

### Step 4 – Verify

**All pods running:**
```bash
make k8s-status
# or: kubectl -n coursematerials get pods,svc,ingress
```

**Eureka registrations (expect 6 services):**
```bash
kubectl -n coursematerials port-forward svc/eureka 8761:8761 &
curl -s http://localhost:8761/eureka/apps -H 'Accept: application/json' | \
  jq '.applications.application[].name'
```

**API via Ingress:**
```bash
curl http://course.192-168-2-200.nip.io/product-composite/1
```

---

## Makefile Shortcuts

| Command | Action |
|---------|--------|
| `make k8s-apply` | `kubectl apply -k deploy/k8s/overlays/local` |
| `make k8s-delete` | Delete all resources (preserves namespace) |
| `make k8s-status` | Show pods, services, and ingress |
| `make k6-start` | Deploy k6 traffic generator |
| `make k6-stop` | Scale k6 to 0 replicas |
| `make k6-logs` | Tail k6 output |

---

## Overlay Structure

```
deploy/k8s/
  base/                    # All resources (namespace, infra, services, load-test)
  overlays/
    local/                 # Image tags + namespace binding
    whatap/                # Placeholder for WhaTap APM injection (see ch 03)
```

To use a different image tag or registry, edit `overlays/local/kustomization.yaml`.

---

**Next:** [70 – Traffic Simulation](70-traffic-simulation.md)
