# 20 – 아키텍처

> **언어:** [English](20-architecture.md) | 한국어

**이 챕터에서 배울 내용:**
- 어떤 서비스가 있고 서로 어떻게 연결되어 있는지
- 각 서비스가 사용하는 데이터베이스와 메시지 브로커
- Spring 프로파일이 런타임 동작을 제어하는 방법

---

## 서비스 목록

| 서비스 | 로컬 포트 | DB | Kafka | 주요 엔드포인트 |
|--------|----------|-----|-------|----------------|
| gateway | 8080 | – | – | 모든 트래픽 라우팅 |
| eureka | 8761 | – | – | 서비스 레지스트리 UI |
| product-composite | 7000 | – | 생산자 | `GET /product-composite/{productId}` |
| product | 7001 | MongoDB | 소비자 | `GET /product/{productId}` |
| recommendation | 7002 | MongoDB | 소비자 | `GET /recommendation?productId=` |
| review | 7003 | MySQL | 소비자 | `GET /review?productId=` |
| inventory | 7004 | MongoDB | 소비자 | `GET /inventory/{productId}` |

> Docker / Kubernetes 환경에서 모든 서비스는 내부적으로 **8080** 포트를 사용합니다. 위의 로컬 포트는 순수 `localhost` 실행 시에만 해당됩니다.

---

## 호출 체인

```
client
  └─ GET /product-composite/{id}
       └─ gateway  (Spring Cloud Gateway)
            └─ product-composite  (오케스트레이터, 리액티브 Mono.zip)
                 ├─ product          ──► inventory   (WebClient, 동기 HTTP)
                 ├─ recommendation
                 └─ review
```

**비동기 (Kafka):** `product-composite`이 CREATE / DELETE 이벤트를 발행하면, `product`, `recommendation`, `review`, `inventory`가 각자의 토픽 파티션에서 소비합니다.

**추적 관점에서의 의미:** `product → inventory` 구간이 호출 체인 깊숙이 추가적인 동기 HTTP 호출을 만듭니다. 두 서비스 모두 의도적으로 랜덤 지연을 주입합니다 — WhaTap에서 확인하게 될 분산 추적 깊이가 바로 이것입니다.

---

## Spring 프로파일 정리

| 프로파일 | 효과 |
|---------|------|
| `docker` | 호스트명을 컨테이너 DNS(`eureka`, `mongodb`, `kafka`, `mysql`)로 전환, 포트를 `8080`으로 통일 |
| `kafka` | 메시지 바인더를 RabbitMQ에서 Kafka로 전환; RabbitMQ 헬스체크 비활성화 |
| `streaming_partitioned` | 파티션 키 표현식을 사용한 Kafka 2-파티션 토픽 활성화 |
| `streaming_instance_0` | 이 소비자 인스턴스는 파티션 0을 읽음 |
| `streaming_instance_1` | 이 소비자 인스턴스는 파티션 1을 읽음 (스케일 아웃된 레플리카에서 사용) |

Kubernetes에서의 활성 조합 (`env-configmap.yaml`):
```
SPRING_PROFILES_ACTIVE=docker,streaming_partitioned,streaming_instance_0,kafka
```

---

## 용어 사전

| 용어 | 한 줄 정의 | 참고 |
|------|-----------|------|
| 마이크로서비스 | 단일 책임을 가진 독립적으로 배포 가능한 서비스 | [martinfowler.com](https://martinfowler.com/articles/microservices.html) |
| 서비스 디스커버리 | 서비스가 IP가 아닌 이름으로 서로를 찾는 메커니즘 | [Eureka 문서](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/) |
| API 게이트웨이 | 클라이언트 요청을 하위 서비스로 라우팅하는 단일 진입점 | [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) |
| Kafka | 비동기 메시징에 사용되는 분산 이벤트 스트리밍 플랫폼 | [kafka.apache.org](https://kafka.apache.org/intro) |
| APM | 애플리케이션 성능 모니터링 — 실행 중인 서비스의 트레이스, 메트릭, 로그 | [WhaTap APM](https://docs.whatap.io/ko/java/introduction) |
| 분산 추적 | 단일 요청이 여러 서비스를 거치는 흐름을 추적하는 것 | [OpenTelemetry](https://opentelemetry.io/docs/concepts/observability-primer/) |

---

**다음 단계:** [30 – Docker Compose 빠른 시작](30-quickstart-docker-compose.ko.md)
