package se.magnus.microservices.core.inventory.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.api.core.inventory.Inventory;
import se.magnus.api.core.inventory.InventoryService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final InventoryService inventoryService;

  @Autowired
  public MessageProcessorConfig(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Bean
  public Consumer<Event<Integer, Inventory>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.getEventCreatedAt());

      switch (event.getEventType()) {

        case CREATE:
          Inventory inventory = event.getData();
          LOG.info("Create inventory with ID: {}", inventory.getInventoryId());
          inventoryService.createInventory(inventory).block();
          break;

        case DELETE:
          int inventoryId = event.getKey();
          LOG.info("Delete recommendations with InventoryID: {}", inventoryId);
          inventoryService.deleteInventory(inventoryId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");

    };
  }
}
