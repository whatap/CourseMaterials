# Kubernetes Deployment Guide

## Overview

Kustomize-based manifests under `deploy/k8s/`. Uses the same Dockerfile,
image tag, and Spring `docker` profile as the existing docker-compose flow.

```
course.192-168-2-200.nip.io
          │
ingress-nginx (LB 192.168.2.200)
          │
       gateway:8080 ──► eureka:8761
          │
  product / recommendation / review / inventory / product-composite
          │
  kafka ─ mongodb ─ mysql ─ zookeeper
```

## Prerequisites

### 1. default StorageClass
```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.30/deploy/local-path-storage.yaml
kubectl patch storageclass local-path -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

### 2. MetalLB (LoadBalancer)
```bash
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.9/config/manifests/metallb-native.yaml
```
Then create `IPAddressPool` and `L2Advertisement` for your IP range.

### 3. ingress-nginx
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.1/deploy/static/provider/cloud/deploy.yaml
```

## Quick Start

```bash
# 1. Build and push images
make build-images push-images

# 2. Create namespace and secret
kubectl create namespace coursematerials
kubectl -n coursematerials create secret generic coursematerials-secret \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<MYSQL_ROOT_PW>'

# 3. Apply
kubectl apply -k deploy/k8s/overlays/local
kubectl -n coursematerials get pods -w
```

Access: http://course.192-168-2-200.nip.io/product-composite/1

## Secret

**Never commit secret.yaml.** Create it directly on the cluster:
```bash
kubectl -n coursematerials create secret generic coursematerials-secret \
  --from-literal=SPRING_DATASOURCE_PASSWORD='your-password'
```

## Verify

```bash
# 1. All pods Running
kubectl -n coursematerials get pods

# 2. Eureka registrations (expect 6)
kubectl -n coursematerials port-forward svc/eureka 8761:8761
curl -s http://localhost:8761/eureka/apps -H 'Accept: application/json' | \
  jq '.applications.application[].name'

# 3. API via Ingress
curl http://course.192-168-2-200.nip.io/product-composite/1
```

## Troubleshooting

| Symptom | Check |
|---|---|
| Eureka registrations < 6 | `kubectl -n coursematerials logs deploy/<svc>` — EUREKA_CLIENT env |
| Kafka `LEADER_NOT_AVAILABLE` | `KAFKA_ADVERTISED_HOST_NAME=kafka` env on kafka StatefulSet |
| MongoDB CrashLoopBackOff | initContainer logs — `nc -z mongodb 27017` pass? |
| Ingress 503 | `kubectl -n coursematerials get endpoints gateway` — not empty? |

## WhaTap APM (Optional)

Use `overlays/whatap` — add label `whatap.io/inject: "true"` to Deployments
and configure `WhatapAgent` CR selector in `whatap-monitoring` namespace.
