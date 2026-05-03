# WhaTap 모니터링 실습 애플리케이션

> **언어:** [English](README.md) | 한국어

[WhaTap APM](https://docs.whatap.io/ko/java/introduction) 교육을 위한 Spring Boot 마이크로서비스 실습 애플리케이션입니다.

```
client
  └─ ingress / localhost:8080
       └─ gateway ──► eureka
            └─ product-composite
                 ├─ product ──► inventory   (동기 HTTP, 분산 추적 깊이 데모)
                 ├─ recommendation
                 └─ review
            └─ kafka ─ mongodb ─ mysql ─ zookeeper

  k6-traffic (클러스터 내부) ──► gateway:8080  # ~3 RPS 상시 유입
```

## 학습 경로

| 챕터 | 주제 |
|------|------|
| [10 – 입문 개념](docs/10-primer.ko.md) | 마이크로서비스·컨테이너·k8s·관측성 한눈에 |
| [20 – 아키텍처](docs/20-architecture.ko.md) | 서비스 맵, 호출 체인, Spring 프로파일 |
| [30 – Docker Compose 빠른 시작](docs/30-quickstart-docker-compose.ko.md) | 빌드·실행·검증 |
| [40 – WhaTap 모니터링](docs/40-whatap-monitoring.ko.md) | 에이전트 주입, 이름 설정, weaving, 로그 연동 |
| [50 – WhaTap 대시보드 읽기](docs/50-whatap-dashboard.ko.md) | Hitmap·트레이스·Apdex 해석 |
| [60 – Kubernetes 빠른 시작](docs/60-quickstart-kubernetes.ko.md) | Kustomize로 k8s 배포 |
| [70 – 트래픽 시뮬레이션](docs/70-traffic-simulation.ko.md) | k6 상시 트래픽, 레거시 스크립트 |
| [80 – 실습 단계](docs/80-exercises.ko.md) | 4가지 핵심 실습 |
| [90 – 트러블슈팅](docs/90-troubleshooting.ko.md) | 증상별 진단·조치 참조표 |

> **권장 순서:** 10(필요 시) → 30 → 40 → 50 → 60 → 70 → 80
