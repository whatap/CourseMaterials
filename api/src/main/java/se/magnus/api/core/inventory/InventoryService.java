package se.magnus.api.core.inventory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface InventoryService {

  Mono<Inventory> createInventory(Inventory body);

  /**
   * Sample usage: "curl $HOST:$PORT/inventory/1".
   *
   * @param inventoryId Id of the inventory
   * @return the inventory, if found, else null
   */
  @GetMapping(
    value = "/inventory/{inventoryId}",
    produces = "application/json")
  Mono<Inventory> getInventory(@PathVariable int inventoryId);

  Mono<Void> deleteInventory(int inventoryId);
}
