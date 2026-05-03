# 60 – Kubernetes 빠른 시작

> **언어:** [English](60-quickstart-kubernetes.md) | 한국어

**이 챕터에서 배울 내용:**
- 매니페스트가 요구하는 클러스터 수준 사전 조건
- 단일 `kubectl` 명령으로 전체 스택을 배포하는 방법
- Pod가 정상 상태인지, API에 접근 가능한지 확인하는 방법

---

## Kubernetes 아키텍처

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

  k6-traffic (Deployment) ──► gateway:8080   # 상시 ~3 RPS
```

---

## 사전 조건

클러스터당 한 번만 설치하면 됩니다. 이미 설치되어 있으면 건너뛰세요.

### 1 – 기본 StorageClass
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
그 다음 LAN IP 대역에 맞게 `IPAddressPool`과 `L2Advertisement`를 생성하세요.  
→ [MetalLB 설정 가이드](https://metallb.universe.tf/configuration/)

### 3 – ingress-nginx
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.12.1/deploy/static/provider/cloud/deploy.yaml
```
→ [ingress-nginx 문서](https://kubernetes.github.io/ingress-nginx/)

---

## 빠른 시작

### 1단계 – 이미지 빌드 및 푸시
```bash
make build-images push-images
```
이미지는 `jinronara/coursematerials-<service>:0.1`로 태그되어 Docker Hub에 푸시됩니다.  
레지스트리/태그 변경: `make build-images IMAGE_PREFIX=myrepo/ IMAGE_TAG=dev`

### 2단계 – 네임스페이스 및 시크릿 생성
```bash
kubectl create namespace coursematerials

kubectl -n coursematerials create secret generic coursematerials-secret \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<MySQL 비밀번호>'
```
> **`secret.yaml`을 git에 커밋하지 마세요.** 항상 클러스터에 직접 시크릿을 생성하세요.

### 3단계 – 배포
```bash
kubectl apply -k deploy/k8s/overlays/local
```
모든 Pod가 `Running` 상태가 될 때까지 대기:
```bash
kubectl -n coursematerials get pods -w
```
예상 결과: **12개 Pod Running** (서비스 11개 + k6-traffic 1개)

### 4단계 – 검증

**전체 Pod 상태 확인:**
```bash
make k8s-status
# 또는: kubectl -n coursematerials get pods,svc,ingress
```

**Eureka 등록 확인 (6개 서비스 예상):**
```bash
kubectl -n coursematerials port-forward svc/eureka 8761:8761 &
curl -s http://localhost:8761/eureka/apps -H 'Accept: application/json' | \
  jq '.applications.application[].name'
```

**Ingress를 통한 API 호출:**
```bash
curl http://course.192-168-2-200.nip.io/product-composite/1
```

---

## Makefile 단축 명령

| 명령 | 동작 |
|------|------|
| `make k8s-apply` | `kubectl apply -k deploy/k8s/overlays/local` |
| `make k8s-delete` | 모든 리소스 삭제 (네임스페이스 유지) |
| `make k8s-status` | Pod, 서비스, Ingress 표시 |
| `make k6-start` | k6 트래픽 생성기 배포 |
| `make k6-stop` | k6를 0 레플리카로 스케일 다운 |
| `make k6-logs` | k6 출력 실시간 확인 |

---

## 오버레이 구조

```
deploy/k8s/
  base/                    # 모든 리소스 (namespace, infra, services, load-test)
  overlays/
    local/                 # 이미지 태그 + 네임스페이스 바인딩
    whatap/                # WhaTap APM 주입 플레이스홀더 (3챕터 참조)
```

이미지 태그나 레지스트리를 변경하려면 `overlays/local/kustomization.yaml`을 편집하세요.

---

**다음 단계:** [70 – 트래픽 시뮬레이션](70-traffic-simulation.ko.md)
