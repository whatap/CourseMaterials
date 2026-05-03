# 90 – 트러블슈팅

> **언어:** [English](90-troubleshooting.md) | 한국어

docker-compose와 Kubernetes 두 런타임을 위한 통합 빠른 참조 가이드입니다.

---

## 애플리케이션 문제

| 증상 | 진단 명령 | 조치 |
|------|----------|------|
| `curl localhost:8080/...`에서 503 반환 | `docker-compose logs gateway` | Gateway가 아직 Eureka에 등록되지 않음 — 30초 기다렸다가 재시도 |
| Eureka에 6개 미만의 서비스 표시 | `docker-compose logs <서비스명>` | `SPRING_PROFILES_ACTIVE`에 `docker` 포함 여부 확인 — 서비스가 컨테이너 DNS 대신 `localhost`에 연결하려 할 수 있음 |
| `product-composite`이 빈 recommendations 또는 reviews 반환 | Kafka 소비자 로그 확인 | Kafka 토픽이 아직 생성되지 않음 — Kafka 정상화 후 소비자 서비스 재시작 |
| 응답이 항상 3초 이상 걸림 | 예상된 동작 — `inventory`에 최대 3000ms 랜덤 sleep이 있음 | [실습 4](80-exercises.ko.md#실습-4-inventory-서비스--트레이스-깊이) 참조 |
| `/product-composite/1`에서 `404` 반환 | `curl localhost:8080/actuator/health` | 서비스가 아직 시작 중일 수 있음 — 기다렸다가 재시도 |

---

## Docker Compose 문제

| 증상 | 진단 명령 | 조치 |
|------|----------|------|
| 포트 8080 이미 사용 중 | `lsof -i :8080` 또는 `netstat -tlnp \| grep 8080` | docker-compose 시작 전 충돌하는 프로세스 중지 |
| 컨테이너가 즉시 종료됨 | `docker-compose logs <서비스>` | 로그에서 누락된 환경 변수 또는 설정 오류 확인 |
| MongoDB 또는 MySQL CrashLoopBackOff | `docker-compose logs mongodb` | 볼륨 충돌 — `docker-compose down -v`로 오래된 볼륨 제거 |
| Kafka `LEADER_NOT_AVAILABLE` | `docker-compose logs kafka` | Zookeeper 준비 안 됨 — Zookeeper 정상화 후 `docker-compose restart kafka` |
| WhaTap 에이전트가 나타나지 않음 | `./whatap/ping.sh` | `whatap.conf` 확인 — `license`와 `whatap.server.host`가 올바른지 확인; 포트 6600으로의 아웃바운드 TCP 방화벽 확인 |

---

## Kubernetes 문제

| 증상 | 진단 명령 | 조치 |
|------|----------|------|
| Pod이 `Pending` 상태에서 멈춤 | `kubectl describe pod <이름> -n coursematerials` | 사용 가능한 노드 없음 또는 리소스 부족 — 노드 용량 확인 |
| Pod이 `CrashLoopBackOff` 상태 | `kubectl logs <pod> -n coursematerials --previous` | init 컨테이너 로그 먼저 확인: `kubectl logs <pod> -c wait-deps` |
| Eureka 등록 수 < 6 | `kubectl logs deploy/product -n coursematerials` | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env 미설정 — ConfigMap 확인 |
| Kafka `LEADER_NOT_AVAILABLE` | `kubectl logs kafka-0 -n coursematerials` | `KAFKA_ADVERTISED_HOST_NAME`이 IP가 아닌 `kafka`(클러스터 DNS)여야 함 |
| MongoDB CrashLoopBackOff | `kubectl logs <mongodb-pod> -n coursematerials -c wait-deps` | init 컨테이너 `nc -z mongodb 27017` 실패 — StorageClass PVC 문제 |
| Ingress에서 503 반환 | `kubectl get endpoints gateway -n coursematerials` | 비어있으면: gateway pod이 Ready 아님 — readinessProbe와 로그 확인 |
| `ImagePullBackOff` | `kubectl describe pod <이름> -n coursematerials` | 이미지가 푸시되지 않았거나 잘못된 태그 — `make push-images` 실행 후 `overlays/local/kustomization.yaml`에서 태그 확인 |
| k6 pod이 시작되지 않음 | `kubectl describe pod -l app=k6-traffic -n coursematerials` | ConfigMap `k6-script`가 없을 수 있음 — `make k6-start` 실행 |

---

## WhaTap 연결 문제

| 증상 | 확인 | 조치 |
|------|------|------|
| 대시보드에서 에이전트 "Disconnected" 표시 | `./whatap/ping.sh` | `13.124.11.223:6600`으로의 아웃바운드 TCP 차단 — 방화벽 확인 또는 `whatap/proxy.sh` 사용 |
| 2분 후에도 대시보드에 메트릭 없음 | `./whatap/resmon.sh` | `whatap.conf`의 `license`와 `whatap.server.host`가 콘솔 값과 일치하는지 확인 |
| Log 메뉴에 로그가 표시되지 않음 | `whatap.conf`에서 `logsink_enabled=true` 확인 | conf 편집 후 서비스 재시작; `whatap/` 디렉터리에 WhaTap 로그 싱크 플러그인 JAR가 있는지 확인 |

---

## 유용한 명령어

```bash
# docker-compose: 전체 로그 실시간 확인
docker-compose -f docker-compose-whatap-1.yml logs -f

# docker-compose: 컨테이너 상태 확인
docker-compose -f docker-compose-whatap-1.yml ps

# k8s: 실패한 Pod 상세 정보
kubectl describe pod <pod-이름> -n coursematerials

# k8s: Deployment 로그 실시간 확인
kubectl logs -f deploy/<서비스명> -n coursematerials

# k8s: 디버깅을 위한 Pod 접속
kubectl exec -it <pod-이름> -n coursematerials -- sh

# WhaTap: 연결 테스트
./whatap/ping.sh
```
