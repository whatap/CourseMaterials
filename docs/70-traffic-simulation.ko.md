# 70 – 트래픽 시뮬레이션

> **언어:** [English](70-traffic-simulation.md) | 한국어

**이 챕터에서 배울 내용:**
- k6가 쉘 기반 부하 스크립트보다 선호되는 이유
- k6가 Kubernetes 클러스터 내부에 배포되는 방식
- 트래픽 볼륨을 조정하고 결과를 확인하는 방법

---

## k6가 더 나은 이유

기존 `stress-test.sh`는 백그라운드 쉘 프로세스를 사용하며 메트릭, 증감 제어, 임계값 기능이 없습니다. k6는 다음을 제공합니다:

- **시나리오 제어** — VU(가상 사용자) 수와 지속 시간을 선언적으로 정의
- **내장 메트릭** — 터미널에서 p95 레이턴시, RPS, 에러율 즉시 확인
- **Grafana 연동** — Prometheus / Grafana로 메트릭 전송 가능 (이 프로젝트에서 이미 사용 중)
- **읽기 쉬운 스크립트** — 순수 JavaScript, 새 엔드포인트 추가 용이

---

## Kubernetes 내 k6 (현재 설정)

k6는 `coursematerials` 네임스페이스 안에 **Deployment**로 실행됩니다. Ingress 없이 클러스터 내부 DNS를 통해 `http://gateway:8080`을 직접 호출합니다.

**시작 / 중지:**
```bash
make k6-start   # ConfigMap + Deployment 적용
make k6-stop    # Deployment를 0 레플리카로 스케일 다운
make k6-logs    # 실시간 출력 확인
```

**스크립트 동작** ([deploy/k8s/base/load-test/configmap.yaml](../deploy/k8s/base/load-test/configmap.yaml)):

```js
scenarios: {
  steady_traffic: {
    executor: 'constant-vus',
    vus: 3,           // 동시에 실행되는 가상 사용자 3명
    duration: '999h', // scale to 0으로 중단할 때까지 실행
  },
},
// 각 VU: GET /product-composite/<랜덤 1-1000>, sleep 1s
// 결과: 초당 약 3건의 상시 트래픽
```

---

## 트래픽 볼륨 조정

`deploy/k8s/base/load-test/configmap.yaml`을 편집하고 `vus`를 변경합니다:

```yaml
vus: 3     # 예: 높은 부하를 위해 10으로 변경
```

재적용:
```bash
make k6-start
# 또는: kubectl -n coursematerials rollout restart deployment/k6-traffic
```

---

## 로컬 Docker에서 k6 실행

로컬 docker-compose 스택에 대해 실행하려면:

```bash
# JS를 직접 작성해 실행
docker run --rm -i grafana/k6:0.55.0 run - <<'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 3,
  duration: '2m',
};

export default function () {
  const id = Math.floor(Math.random() * 1000) + 1;
  const res = http.get(`http://host.docker.internal:8080/product-composite/${id}`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
EOF
```

> `host.docker.internal`은 macOS/Windows에서 Docker 호스트로 해석됩니다. Linux에서는 `--network=host`와 `localhost`를 사용하세요.

---

## 레거시 대안

| 스크립트 | 사용법 | 언제 사용 |
|---------|--------|---------|
| `stress-test.sh` | `./stress-test.sh <동시 수> <딜레이>` | k8s 없이 로컬 curl 기반 빠른 부하 테스트 |
| `test-em-all.bash` | `./test-em-all.bash` | HTTP 코드와 JSON 필드를 확인하는 기능 통합 테스트 |

---

**다음 단계:** [80 – 실습 단계](80-exercises.ko.md)
