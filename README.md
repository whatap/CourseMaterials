# CourseMaterials
3/27 WhaTap Academy course materials

## Prerequisites
* Java
* docker-compose

## Build
```bash
./gradlew build
docker-compose build
```

## Install WhaTap agent
1. Create whatap java application project
   * https://service.whatap.io  
2. Run Docker Compose with whatap agents
   * Before running, please review the contents of docker-compose-whatap-1.yml and the the configuration in whatap.conf.
    ```bash
    docker-compose -f ./docker-compose-whatap-1.yml up -d
    ```
3. Requests are generated using test-em-all.bash.
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
  * restart services
  * add options in whatap.conf
    ```
    profile_sql_param_enabled=true
    profile_http_parameter_enabled=true
    ```
### Apdex
  * View the dashboard, Metrics and Alert
  * add options in whatap.conf
    ```
    apdex_time=10
    ```
  * modify in whatap.conf
    ```
    apdex_time=200
    ```

### Data cleansing
  * https://docs.whatap.io/en/java/trs-view#java-agent-exception
  * https://docs.whatap.io/en/java/track-transactions-intro#normalizing-the-transaction-name
  * check with hitmap and statistics
  * add options in whatap.conf
    ```
    status_ignore=404,422,400
    httpc_status_ignore=404,422,400
    ```
  * add options in whatap.conf
    ```
    ignore_exceptions=org.springframework.web.server.ServerWebInputException
    ```
  * add options in whatap.conf
    ```
    trace_ignore_url_set=/actuator/health
    ```
  * add options in whatap.conf
    ```
    trace_normalize_urls=/product-composite/{productId}
    ```

### Distribution Tracing
  * https://docs.whatap.io/en/java/agent-transaction#multiple-transaction-trace
  * https://docs.whatap.io/en/java/analysis-msa
  * check with hitmap drag and MSA Analysis
  * add options in whatap.conf
    ```
    mtrace_auto_enabled=false
    mtrace_rate=100
    ```

### With the Log
  * https://docs.whatap.io/en/log/log-java
  * Check Log and profile menus
  * add options in whatap.conf
    ```
    logsink_enabled=true
    ```

