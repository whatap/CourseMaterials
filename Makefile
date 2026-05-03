IMAGE_PREFIX ?= jinronara/coursematerials-
IMAGE_TAG    ?= 0.1
# Docker Hub: https://hub.docker.com/repositories/jinronara
PLATFORM     ?= linux/amd64
APPS         := product recommendation review inventory product-composite
SPRING_CLOUD := eureka gateway

.PHONY: help build-jar build-images push-images compose-up compose-down k8s-apply k8s-delete k8s-status k6-start k6-stop k6-logs

help:
	@echo "build-jar       Gradle build (no tests)"
	@echo "build-images    Build all Docker images via buildx (linux/amd64)"
	@echo "push-images     Push all images to registry"
	@echo "compose-up      docker-compose 기존 흐름"
	@echo "k8s-apply       Apply Kustomize overlay (local)"

build-jar:
	./gradlew clean build -x test

build-images: build-jar
	@for s in $(APPS); do \
	  docker buildx build --platform $(PLATFORM) \
	    -t $(IMAGE_PREFIX)$$s:$(IMAGE_TAG) --load \
	    microservices/$$s-service ; \
	done
	docker buildx build --platform $(PLATFORM) \
	  -t $(IMAGE_PREFIX)eureka:$(IMAGE_TAG) --load spring-cloud/eureka-server
	docker buildx build --platform $(PLATFORM) \
	  -t $(IMAGE_PREFIX)gateway:$(IMAGE_TAG) --load spring-cloud/gateway

push-images:
	@for s in $(APPS) $(SPRING_CLOUD); do docker push $(IMAGE_PREFIX)$$s:$(IMAGE_TAG); done

compose-up:
	docker-compose -f docker-compose-whatap-1.yml up -d
compose-down:
	docker-compose -f docker-compose-whatap-1.yml down

k8s-apply:
	kubectl apply -k deploy/k8s/overlays/local
k8s-delete:
	kubectl delete -k deploy/k8s/overlays/local --ignore-not-found
k8s-status:
	kubectl -n coursematerials get pods,svc,ingress

k6-start:
	kubectl -n coursematerials apply -f deploy/k8s/base/load-test/configmap.yaml
	kubectl -n coursematerials apply -f deploy/k8s/base/load-test/deployment.yaml

k6-stop:
	kubectl -n coursematerials scale deployment k6-traffic --replicas=0

k6-logs:
	kubectl -n coursematerials logs -f -l app=k6-traffic --tail=50
