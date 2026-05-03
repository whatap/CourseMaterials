# 80 – 실습 단계

> **언어:** [English](80-exercises.md) | 한국어

**이 챕터에서 배울 내용:**
- 멀티 인스턴스 가시성을 위한 WhaTap 에이전트 이름 설정
- `weaving`으로 라이브러리 수준 추적 활성화
- 멀티 홉 호출 체인에서 분산 추적이 레이턴시를 어떻게 보여주는지

> **사전 조건:** [30 – Docker Compose 빠른 시작](30-quickstart-docker-compose.ko.md) 완료 및 서비스 실행 중

---

## 실습 1 – 에이전트 이름 설정

**목표:** 스케일 아웃 시 WhaTap 대시보드에서 개별 서비스 인스턴스를 식별할 수 있게 하기

### 단계

1. `docker-compose-whatap-1.yml`로 시작합니다 (이미 실행 중).  
   WhaTap 대시보드를 열어봅니다 — 에이전트 이름이 IP 기반의 알기 어려운 문자열임을 확인합니다.

2. 이름이 지정된 설정으로 전환합니다:
   ```bash
   docker-compose -f docker-compose-whatap-1.yml down
   docker-compose -f docker-compose-whatap-2.yml up -d
   ```

3. product-composite를 5개 인스턴스로 스케일 아웃합니다:
   ```bash
   docker-compose -f docker-compose-whatap-2.yml scale product-composite=5
   ```

4. WhaTap 대시보드를 새로 고칩니다.

### 예상 결과
`product-composite-<ip2>-<ip3>` 형태의 **5개 에이전트**가 각각 표시됩니다 (예: `product-composite-17-3`, `product-composite-17-5`).  
`{ip2}-{ip3}` 매크로는 시작 시 각 컨테이너 IP의 마지막 두 옥텟으로 확장됩니다.

→ 참고: [WhaTap 에이전트 이름 설정 문서](https://docs.whatap.io/ko/java/agent-name)

**다음으로 시도해 보세요:** 인스턴스를 1개로 줄이고 대시보드에서 에이전트가 사라지는 것을 관찰하세요.

---

## 실습 2 – Gray Area (라이브러리 추적)

**목표:** Spring Boot 내부 라이브러리 호출(WebClient, 리액티브 스케줄러)이 Hitmap 트레이스에서 별도의 스팬으로 나타나는 것 확인

### 단계

1. [whatap/whatap.conf](../whatap/whatap.conf)를 열고 확인합니다:
   ```
   weaving=spring-boot-2.5
   ```
   이 줄이 이미 있어야 합니다. 없으면 추가하세요.

2. 설정 변경 사항을 반영하기 위해 서비스를 재시작합니다:
   ```bash
   docker-compose -f docker-compose-whatap-1.yml restart
   ```

3. 몇 번의 요청을 보냅니다:
   ```bash
   curl http://localhost:8080/product-composite/1
   ```

4. WhaTap Hitmap을 열고 최근 점들에 사각형을 드래그한 후 트레이스를 클릭합니다.

### 예상 결과
각 트레이스 안에 **라이브러리 수준 스팬**이 보여야 합니다 — 예를 들어 `spring-webflux`, `netty`, `spring-reactive`가 블랙박스 트랜잭션이 아닌 별도의 하위 단계로 나타납니다.

→ 참고: [WhaTap weaving 문서](https://docs.whatap.io/ko/java/agent-weaving) | [트랜잭션 프로파일 옵션](https://docs.whatap.io/ko/java/trs-profile#additional-options)

**다음으로 시도해 보세요:** `weaving=spring-boot-2.5`를 주석 처리하고 재시작해서 트레이스 깊이를 비교해 보세요.

---

## 실습 3 – 로그 연동

**목표:** WhaTap Log 메뉴에서 애플리케이션 로그와 트레이스를 상관관계로 확인

### 단계

1. [whatap/whatap.conf](../whatap/whatap.conf)를 열고 확인합니다:
   ```
   logsink_enabled=true
   ```

2. gateway 라우트 로그를 위한 grok 파서 패턴을 추가합니다. WhaTap 콘솔에서 **로그 → 로그 설정 → 파서**로 이동해 다음을 추가합니다:
   ```
   %{TIMESTAMP_ISO8601:date}\s+%{LOGLEVEL:log_level}\s+%{NUMBER:pid}\s+---\s+\[%{DATA:thread}\]\s+%{DATA:logger_class}\s+:\s+Mapping\s+\[Exchange:\s+%{WORD:http_method}\s+%{URI:url}\]\s+to\s+Route\{id='%{DATA:route_id}',\s+uri=%{DATA:route_uri},\s+order=%{NUMBER:order},\s+predicate=%{DATA:predicate},\s+match trailing slash:\s+%{WORD:match_trailing_slash},\s+gatewayFilters=\[%{DATA:gateway_filters}\],\s+metadata=\{%{DATA:metadata}\}\}
   ```

3. 요청을 보낸 후 다음을 확인합니다:
   - **로그 메뉴** — 서비스 이름으로 필터링해 구조화된 로그 항목 확인
   - **프로파일 메뉴** — 트레이스를 클릭해 연관된 로그 라인 인라인으로 확인

### 예상 결과
다음과 같은 gateway 라우트 매핑 로그가:
```
2025-03-21 08:31:51.001 DEBUG 1 --- [or-http-epoll-1] o.s.c.g.h.RoutePredicateHandlerMapping   :
Mapping [Exchange: GET http://localhost:8080/product-composite/1] to Route{id='product-composite', ...}
```
...WhaTap Log 뷰에서 파싱되어 구조화된 형태로 표시됩니다.

→ 참고: [WhaTap 로그 연동 문서](https://docs.whatap.io/ko/log/log-java)

**다음으로 시도해 보세요:** `log_level=DEBUG`로 필터링해 gateway 라우팅 활동을 격리해 보세요.

---

## 실습 4 – Inventory 서비스 & 트레이스 깊이

**목표:** 4단계 분산 추적 전체를 관찰하고, 깊은 체인의 레이턴시가 WhaTap에서 어떻게 나타나는지 이해하기

### 배경

호출 체인:
```
gateway → product-composite → product → inventory
```

`product`와 `inventory` 모두 의도적인 랜덤 sleep을 주입합니다:
- **product** ([ProductServiceImpl.java](../microservices/product-service/src/main/java/se/magnus/microservices/core/product/services/ProductServiceImpl.java) 76번째 줄): `10–100 ms`
- **inventory** ([InventoryServiceImpl.java](../microservices/inventory-service/src/main/java/se/magnus/microservices/core/inventory/services/InventoryServiceImpl.java) 71번째 줄): `10–3000 ms`

`product → inventory` 호출은 `WebClient`(비동기 HTTP)를 사용합니다. inventory가 느리거나 사용 불가능해도 product는 gracefully하게 계속 처리됩니다 — 트레이스에는 여전히 실패/느린 스팬이 표시됩니다.

### 단계

1. 서비스가 실행 중이고 트래픽이 흐르고 있는지 확인합니다 (`make k6-start` 또는 `./stress-test.sh 3 1`).

2. WhaTap **Hitmap**을 열면 약 10ms~3초 범위에 걸쳐 점들이 분산된 것을 볼 수 있습니다.

3. 차트 상단의 고레이턴시 점들에 드래그하고 트레이스를 엽니다.

4. 트레이스 트리를 펼쳐 4개 서비스 홉을 모두 확인합니다.

### 예상 결과
- **inventory** 스팬이 가장 긴 지속 시간(최대 3초)을 보임
- **product** 스팬은 inventory를 감싸며 — 총 지속 시간에 inventory 호출 시간이 포함됨
- **product-composite** 스팬은 3개 하위 서비스를 병렬로 감쌈 (`Mono.zip`)
- 전체 엔드투엔드 레이턴시는 `inventory` sleep에 의해 결정됨

**다음으로 시도해 보세요:** inventory 서비스를 중단하고 (`docker-compose stop inventory`) 트레이스가 어떻게 변하는지 관찰하세요 — product는 더 빠르게 완료되지만 inventory에 타임아웃/에러 스팬이 나타납니다.

---

**다음 단계:** [90 – 트러블슈팅](90-troubleshooting.ko.md)
