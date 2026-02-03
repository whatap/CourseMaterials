
#!/bin/bash
sed -i '/  product:/i \
  inventory:\
    build: microservices/inventory-service\
    mem_limit: 512m\
    environment:\
      - SPRING_PROFILES_ACTIVE=docker,streaming_partitioned,streaming_instance_0,kafka\
      - JAVA_TOOL_OPTIONS=-javaagent:/app/whatap/whatap.agent-2.2.54.jar --add-opens=java.base/java.lang=ALL-UNNAMED\
    volumes:\
      - type: bind\
        source: ./whatap\
        target: /app/whatap\
    depends_on:\
      mongodb:\
        condition: service_healthy\
      kafka:\
        condition: service_started' ~/workspace/CourseMaterials/docker-compose-whatap-1.yml
