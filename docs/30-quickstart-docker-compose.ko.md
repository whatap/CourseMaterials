# 30 – Docker Compose 빠른 시작

> **언어:** [English](30-quickstart-docker-compose.md) | 한국어

**이 챕터에서 배울 내용:**
- docker-compose로 전체 애플리케이션 스택을 빌드하고 실행하는 방법
- 모든 서비스가 정상 동작하는지 확인하는 방법
- 백그라운드 트래픽을 시작하고 중지하는 방법

> **마이크로서비스·컨테이너·APM이 처음이라면** 먼저 [10 – 입문 개념](10-primer.ko.md)을 5분만 훑고 오세요.

---

## 사전 조건

| 도구 | 버전 | 설치 |
|------|-----|------|
| Java (JDK) | 17+ | [adoptium.net](https://adoptium.net/) |
| Docker | 24+ | [docs.docker.com/get-docker](https://docs.docker.com/get-docker/) |
| Docker Compose | v2 | Docker Desktop에 포함; `docker compose version`으로 확인 |
| make | 모든 버전 | macOS/Linux에 기본 설치됨 |

> **Windows 사용자:** 아래 모든 명령은 WSL2 또는 Git Bash에서 실행하세요.

---

## 1단계 – 빌드

```bash
./gradlew clean build -x test
```

모든 모듈을 컴파일하고 각 서비스를 레이어드 JAR로 패키징합니다. 단위 테스트도 실행하려면 `-x test`를 제거하세요 (약 3분 추가 소요).

---

## 2단계 – 시작

```bash
docker-compose -f docker-compose-whatap-1.yml up -d
```

**gateway, eureka, product-composite, product, recommendation, review, inventory, kafka, zookeeper, mongodb, mysql** — 총 11개 컨테이너가 시작됩니다.

시작 진행 상황 확인:
```bash
docker-compose -f docker-compose-whatap-1.yml logs -f
```

각 서비스의 `Started ...Application` 로그가 나타날 때까지 기다리세요 (약 60–90초).

---

## 3단계 – 검증

**API 동작 확인:**
```bash
curl http://localhost:8080/product-composite/1
```
예상 결과: `productId`, `recommendations`, `reviews`, `inventory` 필드를 포함한 JSON 응답

**Eureka 대시보드:**  
[http://localhost:8761](http://localhost:8761) 접속 — **6개 서비스** 등록 확인 (gateway, product-composite, product, recommendation, review, inventory)

**Swagger UI:**  
[http://localhost:8080/openapi/swagger-ui.html](http://localhost:8080/openapi/swagger-ui.html)

---

## 4단계 – 트래픽 생성

WhaTap 대시보드에 항상 실시간 데이터가 표시되도록 상시 백그라운드 트래픽을 발생시킵니다.

**옵션 A – k6 (권장):**
```bash
make k6-start
```
> Kubernetes 배포가 실행 중이어야 합니다. 자세한 내용은 [70 – 트래픽 시뮬레이션](70-traffic-simulation.ko.md)을 참고하세요.

**옵션 B – 레거시 쉘 스크립트:**
```bash
chmod +x stress-test.sh
./stress-test.sh 5 0.5
# 사용법: ./stress-test.sh <동시 실행 수> <딜레이(초)>
```

---

## 스케일 조정

```bash
# product-composite를 3개 인스턴스로 확장
docker-compose -f docker-compose-whatap-1.yml scale product-composite=3

# 다시 1개로 축소
docker-compose -f docker-compose-whatap-1.yml scale product-composite=1
```

---

## 중지

```bash
docker-compose -f docker-compose-whatap-1.yml down
```

영구 볼륨(MongoDB, MySQL 데이터)도 삭제하려면 `-v`를 추가하세요.

---

**다음 단계:** [40 – WhaTap 모니터링](40-whatap-monitoring.ko.md)
