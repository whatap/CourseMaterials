package se.magnus.microservices.core.inventory.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveCrudRepository<InventoryEntity, String> {
  Mono<InventoryEntity> findByInventoryId(int inventoryId);
}
