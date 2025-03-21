# CourseMaterials
3/27 WhaTap academy course materials

## Pre-requierments 
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
     
2. run docker-compose with whatap agents
   * Before running, please check the contents of docker-compose-whatap.yml and the configuration in whatap.conf.
  ```bash
  ./docker-compose -f ./docker-compose-whatap.yml up
  ```

3. Requests are generated using test-em-all.bash.
  ```bash
  watch -n 1 ./test-em-all.bash
  ```

## Practice Steps
### Agent naming
  * https://docs.whatap.io/en/java/agent-name
### Error defining
  * https://docs.whatap.io/en/java/agent-transaction#exception
### Gray Area
  * https://docs.whatap.io/en/java/agent-weaving
### With the Log
  * https://docs.whatap.io/en/log/log-java
