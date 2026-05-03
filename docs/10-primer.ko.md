# 10 – 입문 개념

> **언어:** [English](10-primer.md) | 한국어

**이 챕터에서 배울 내용:**
- 이 실습이 풀려고 하는 큰 그림
- 나머지 챕터를 읽기 전에 알아야 할 최소 용어
- HTTP 요청 1개가 어떻게 "분산 추적"이 되는가

> 마이크로서비스·컨테이너·Kubernetes·APM에 익숙하다면 이 챕터는 건너뛰세요. 한 가지라도 처음이면 5분만 훑고 가세요.

---

## 이 실습이 풀려는 문제

여러 개의 마이크로서비스로 쪼개진 시스템에서는 "API가 느렸다"만으로는 부족합니다. 한 요청이 다섯 개 서비스와 세 개 데이터베이스를 거쳤을 수 있기 때문입니다. **APM(Application Performance Monitoring)**, 즉 WhaTap 같은 도구는 *"이번엔 어느 서비스의 어느 호출, 어느 라인이 느렸는가?"*를 코드에 `println`을 박지 않고도 답할 수 있게 해줍니다.

이 실습은 작지만 현실적인 시스템(Spring Boot 서비스 7개 + Kafka + DB 2종)을 띄우고, 거기에 상시 트래픽을 흘려서 실제 분산 추적이 APM 대시보드에 어떻게 보이는지 직접 보게 해줍니다.

---

## 마이크로서비스

**마이크로서비스**는 단일 책임을 가지고 독립적으로 배포되는 서비스입니다. 큰 애플리케이션 하나 대신 작은 서비스 여러 개가 네트워크로 통신합니다.

```
client → gateway → product-composite ─┬─ product ── inventory
                                       ├─ recommendation
                                       └─ review
```

→ 참고: [Martin Fowler — Microservices](https://martinfowler.com/articles/microservices.html)

---

## 컨테이너와 Docker

**컨테이너 이미지**는 실행 가능한 파일시스템 패키지입니다(앱 + JDK + 설정). **컨테이너**는 그 이미지의 실행 인스턴스입니다. Docker는 이를 빌드하고 실행하는 가장 흔한 도구입니다. 이 실습의 모든 서비스는 컨테이너 이미지로 배포됩니다.

→ 참고: [Docker — Get Started](https://docs.docker.com/get-started/)

---

## Kubernetes — 꼭 알아야 할 4가지 단어

Kubernetes(k8s)는 여러 머신에서 컨테이너를 실행해주는 시스템입니다. 60챕터를 읽으려면 이 4개만 알면 됩니다:

| 용어 | 한 줄 정의 |
|------|-----------|
| Pod | 같이 살고 같이 죽는 컨테이너 묶음 (보통 컨테이너 1개) |
| Deployment | "이 Pod를 N개 유지하라"는 선언 |
| Service | Pod 그룹 앞에 붙는 안정적인 클러스터 내부 DNS 이름 |
| Ingress | 클러스터 외부 → 내부 Service로 이어지는 HTTP 진입점 |

→ 참고: [Kubernetes — 튜토리얼](https://kubernetes.io/ko/docs/tutorials/kubernetes-basics/)

---

## 관측성(Observability) 3축

| 신호 | 정의 | 이 실습에서의 예 |
|------|------|------------------|
| 메트릭 | 시간에 따른 숫자 | `gateway`의 평균 응답시간 |
| 로그 | 시각이 찍힌 텍스트 이벤트 | `Mapping [Exchange: GET ...]` |
| 트레이스 | 한 요청이 서비스들을 거친 경로 | `gateway → product-composite → product → inventory` |

WhaTap은 셋 다 수집합니다. 이 과정은 그중 **트레이스**에 집중합니다. 마이크로서비스 환경에서 가장 유용한 신호이기 때문입니다.

→ 참고: [OpenTelemetry — 관측성 입문](https://opentelemetry.io/docs/concepts/observability-primer/)

---

## APM이란? (그리고 WhaTap이 실제로 하는 일)

WhaTap **Java 에이전트**는 JVM 기동 시 `-javaagent:` 옵션으로 붙는 JAR 파일입니다. 표준 라이브러리(Spring, JDBC, HTTP 클라이언트)를 바이트코드 weaving으로 계측해 **소스 코드 변경 없이** 트레이스·메트릭·로그를 WhaTap 서버로 전송합니다. 그것을 대시보드에서 봅니다.

→ 참고: [WhaTap Java 에이전트 소개](https://docs.whatap.io/ko/java/introduction)

---

## 분산 추적 한눈에 보기

`/product-composite/{id}` 요청 1개는 4개 서비스를 거칩니다. WhaTap은 각 단계를 **스팬(span)**으로 기록하고 트리 형태로 보여줍니다:

```
client
  └─ gateway ──────────────────────────────────────┐  ← 루트 스팬
       └─ product-composite ─────────────────┐    │
            ├─ product ──── inventory ─┐    │    │
            ├─ recommendation ─────────┘    │    │
            └─ review ─────────────────────┘    │
                                                 ▼
타임라인 (막대가 길수록 더 오래 걸림):
├──── gateway ─────────────────────────────────────┤
    ├──── product-composite ───────────────────┤
        ├── product ──────────────┤
                ├── inventory ─────┤   ← 보통 가장 긴 잎(leaf)
        ├── recommendation ──┤
        ├── review ─────┤
```

의도적인 랜덤 sleep을 주입했습니다: **product** 10–100 ms, **inventory** 10–3000 ms. 덕분에 트레이스가 시각적으로 흥미로워지고, "느린 잎"이 어떻게 보이는지를 직접 볼 수 있습니다.

---

## 이 과정에서 실제로 할 일

1. **docker-compose**로 시스템을 띄운다 (30챕터).
2. WhaTap 에이전트를 주입하고 에이전트 이름을 정리한다 (40챕터).
3. 같은 시스템을 **Kubernetes**에 배포한다 (60챕터).
4. **k6**로 상시 백그라운드 트래픽을 발생시킨다 (70챕터).
5. 대시보드를 읽고 느린 요청을 찾고 레이턴시를 분석한다 (50, 80챕터).

---

**다음 단계:** 서비스 목록부터 보려면 [20 – 아키텍처](20-architecture.ko.md), 바로 빌드하려면 [30 – Docker Compose 빠른 시작](30-quickstart-docker-compose.ko.md).
