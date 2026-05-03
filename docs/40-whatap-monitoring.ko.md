# 40 – WhaTap 모니터링

> **언어:** [English](40-whatap-monitoring.md) | 한국어

**이 챕터에서 배울 내용:**
- WhaTap APM이 Java 서비스에 주입되는 방식
- 두 docker-compose 설정의 차이점
- 실습 중 편집하게 될 핵심 `whatap.conf` 옵션

---

## WhaTap APM이란?

WhaTap APM은 Java 에이전트를 통해 실행 중인 JVM 프로세스에서 트레이스, 메트릭, 로그를 수집합니다. 에이전트는 애플리케이션 시작 시 첨부되므로 **소스 코드 변경이 필요 없습니다.**  
→ [WhaTap Java 에이전트 소개](https://docs.whatap.io/ko/java/introduction)

---

## 1단계 – 라이선스 및 서버 설정

에이전트 자격 증명은 [whatap/whatap.conf](../whatap/whatap.conf)에 있습니다.

```
license=x605c8888kr4d-z4in0c7sgi43aa-z554s2prm6qa8d
whatap.server.host=13.124.11.223/13.209.172.35
```

실행 전에 WhaTap 콘솔에서 발급받은 자신의 프로젝트 자격 증명으로 위 두 값을 교체하세요.  
→ [라이선스 키 확인 방법](https://docs.whatap.io/ko/project/project-manage)

---

## 2단계 – 에이전트 주입 (docker-compose)

`docker-compose-whatap-1.yml`의 모든 서비스는 다음 패턴을 사용합니다:

```yaml
environment:
  - JAVA_TOOL_OPTIONS=-javaagent:/app/whatap/whatap.agent-2.2.54.jar
      --add-opens=java.base/java.lang=ALL-UNNAMED
volumes:
  - type: bind
    source: ./whatap        # whatap.conf + 에이전트 JAR가 있는 호스트 디렉터리
    target: /app/whatap     # 각 컨테이너에 마운트됨
```

바인드 마운트된 `whatap/` 디렉터리에는 다음이 포함됩니다:
- `whatap.agent-2.2.54.jar` — 에이전트 JAR
- `whatap.conf` — 모든 서비스가 공유하는 설정 파일
- `whatap-logsink-lz4-1.7.0.jar` — 로그 싱크 플러그인

---

## 3단계 – 에이전트 이름 설정: compose-1 vs compose-2

이것이 두 compose 파일 간의 가장 중요한 교육적 차이점입니다.

| | `docker-compose-whatap-1.yml` | `docker-compose-whatap-2.yml` |
|-|-------------------------------|-------------------------------|
| 에이전트 이름 | 컨테이너 IP에서 자동 생성 | 서비스별 명시적 설정 |
| `JAVA_TOOL_OPTIONS` 추가 옵션 | 없음 | `-Dwhatap.onode=docker-compose-node`<br>`-Dwhatap.okind=<서비스 타입>`<br>`-Dwhatap.name=<서비스>-{ip2}-{ip3}` |
| 대시보드 표시 | 인스턴스 구분 어려움 | `product-composite-17-3`, `product-composite-17-4`처럼 명확히 구분됨 |

**`{ip2}-{ip3}` 매크로**는 런타임에 컨테이너 IP 주소의 마지막 두 옥텟으로 확장됩니다. `product-composite=5`로 스케일 아웃하면 WhaTap 대시보드에 5개의 에이전트가 각각 다른 이름으로 표시됩니다.

전환 방법:
```bash
docker-compose -f docker-compose-whatap-1.yml down
docker-compose -f docker-compose-whatap-2.yml up -d
```

→ 전체 실습 흐름은 [실습 1](80-exercises.ko.md#실습-1-에이전트-이름-설정)을 참고하세요.

---

## 핵심 `whatap.conf` 옵션

| 옵션 | 현재 값 | 기능 |
|------|--------|------|
| `weaving` | `spring-boot-2.5` | Spring Boot 라이브러리 내부(WebClient, 리액티브 체인) 계측 |
| `logsink_enabled` | `true` | 애플리케이션 로그를 WhaTap 로그 싱크로 전송 |
| `mtrace_rate` | `100` | 분산 추적을 위해 트랜잭션의 100%를 샘플링 |
| `trace_normalize_urls` | `/product-composite/{productId},...` | Hitmap의 카디널리티 감소를 위해 동적 URL 그룹화 |
| `trace_ignore_url_set` | `/actuator/health` | 헬스체크 엔드포인트를 추적 대상에서 제외 |
| `apdex_time` | `2000` | SLA 임계값(ms) — 이보다 빠른 요청은 "만족" 으로 분류 |
| `profile_http_parameter_enabled` | `true` | 트레이스에 HTTP 쿼리 파라미터 캡처 |
| `profile_sql_param_enabled` | `true` | 트레이스에 SQL 바인드 파라미터 캡처 |

---

## Kubernetes에서의 에이전트 주입

`deploy/k8s/overlays/whatap/` 오버레이는 현재 **플레이스홀더**입니다. Kubernetes에서 실제 WhaTap APM을 연동하려면 다음이 필요합니다:

1. **WhatapAgent CRD** — WhaTap Kubernetes 오퍼레이터 설치
2. **Pod 어노테이션** — 각 Deployment의 pod 템플릿에 `whatap.io/inject: "true"` 추가
3. **ConfigMap** — `whatap.conf`를 Pod에 마운트
4. **Secret** — 라이선스 키 저장

→ [WhaTap Kubernetes 오퍼레이터 문서](https://docs.whatap.io/ko/kubernetes/install-master-node-agent)

---

## 유틸리티 스크립트

`whatap/` 디렉터리의 스크립트는 연결 문제 진단에 사용합니다:

| 스크립트 | 용도 |
|---------|------|
| `whatap/ping.sh` | 에이전트에서 WhaTap 서버까지의 TCP 연결 테스트 |
| `whatap/resmon.sh` | CPU/메모리 리소스 모니터링 데이터 수집 유효성 검증 |
| `whatap/proxy.sh` | 직접 연결이 차단된 경우 독립형 프록시 실행 |

프로젝트 루트에서 실행:
```bash
./whatap/ping.sh
```

---

> **WhaTap 대시보드를 처음 본다면** 다음으로 가기 전에 [50 – WhaTap 대시보드 읽기](50-whatap-dashboard.ko.md)부터 보세요.

---

**다음 단계:** [60 – Kubernetes 빠른 시작](60-quickstart-kubernetes.ko.md)
