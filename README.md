
# Sample Application for WhaTap Monitoring

This example creates a spring-boot application including Gateway, Eureka, and 4 microservices (product-composite, product, recommendation, review).

## Usage

1. Build
    ```bash
    ./gradlew clean build -x test
    ```
2. Run docker-compose
    ```bash
    docker-compose -f ./docker-compose-whatap-1.yml up -d
    ```
3. Requests are generated using stress-test.sh.
    ```bash
    chmod +x stress-test.sh
    ./stress-test.sh 5 0.5
    ```
    * Or use the original legacy script:
    ```bash
    nohup `while true; do ./test-em-all.bash; sleep 0.3; done` &
    ```

4. Monitor the WhaTap Dashboard
   ```bash
   docker-compose -f ./docker-compose-whatap-1.yml scale product-composite=3
   ```

## Practice Steps

### Agent Naming
  * https://docs.whatap.io/en/java/agent-name
  * View the dashboard
    ```bash
    docker-compose down
    docker-compose -f ./docker-compose-whatap-2.yml up -d
    ```
    ```bash
    docker-compose -f ./docker-compose-whatap-2.yml scale gateway=1
    docker-compose -f ./docker-compose-whatap-2.yml scale product-composite=5
    ```

### Gray Area
  * https://docs.whatap.io/en/java/agent-weaving
  * https://docs.whatap.io/en/java/trs-profile#additional-options
  * check with hitmap drag and library version
  * add options in whatap.conf
    ```
    weaving=spring-boot-2.5
    ```

### With the Log
  * https://docs.whatap.io/en/log/log-java
  * Check Log and profile menus
  * add options in whatap.conf
    ```
    logsink_enabled=true
    ```
  * add phaser
    ```
    %{TIMESTAMP_ISO8601:date}\s+%{LOGLEVEL:log_level}\s+%{NUMBER:pid}\s+---\s+\[%{DATA:thread}\]\s+%{DATA:logger_class}\s+:\s+Mapping\s+\[Exchange:\s+%{WORD:http_method}\s+%{URI:url}\]\s+to\s+Route\{id='%{DATA:route_id}',\s+uri=%{DATA:route_uri},\s+order=%{NUMBER:order},\s+predicate=%{DATA:predicate},\s+match trailing slash:\s+%{WORD:match_trailing_slash},\s+gatewayFilters=\[%{DATA:gateway_filters}\],\s+metadata=\{%{DATA:metadata}\}\}
    ```
  * Check with logs
    ```
    2025-03-21 08:31:51.001 DEBUG 1 --- [or-http-epoll-1] o.s.c.g.h.RoutePredicateHandlerMapping   : Mapping [Exchange: GET http://localhost:8080/product-composite/1] to Route{id='product-composite', uri=lb://product-composite, order=0, predicate=Paths: [/product-composite/**], match trailing slash: true, gatewayFilters=[], metadata={}}
    ```

### Inventory Service & Trace Depth
  * **New Service Added**: `inventory-service` (Port 7004)
  * **Call Chain**: `Product Composite` -> `Product Service` -> `Inventory Service`
  * **Goal**: Observe the distributed tracing and call depth in WhaTap Hitmap/Trace view.
  * **Latency**: Both `product-service` and `inventory-service` have random sleep logic to simulate production latency variation.

## Kubernetes Deployment (Optional)

Kustomize-based manifests are available under `deploy/k8s/`. Both docker-compose
and Kubernetes share the same Dockerfile, image tag, and Spring `docker` profile.

### Quick Start

Prerequisites: kubectl context pointing to your cluster, MetalLB, ingress-nginx,
and a default StorageClass. See [`deploy/k8s/README.md`](deploy/k8s/README.md) for setup.

```bash
make build-images push-images
kubectl create namespace coursematerials
kubectl -n coursematerials create secret generic coursematerials-secret \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<MYSQL_ROOT_PW>'
kubectl apply -k deploy/k8s/overlays/local
kubectl -n coursematerials get pods -w
```

Access: `http://course.192-168-2-200.nip.io/product-composite/1`
