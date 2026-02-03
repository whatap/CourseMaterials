package se.magnus.microservices.core.inventory.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.concurrent.ThreadLocalRandom;
import se.magnus.api.core.inventory.Inventory;
import se.magnus.api.core.inventory.InventoryService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.inventory.persistence.InventoryEntity;
import se.magnus.microservices.core.inventory.persistence.InventoryRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class InventoryServiceImpl implements InventoryService {

  private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final InventoryRepository repository;

  private final InventoryMapper mapper;

  @Autowired
  public InventoryServiceImpl(InventoryRepository repository, InventoryMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  public Mono<Inventory> createInventory(Inventory body) {

    if (body.getInventoryId() < 1) {
      throw new InvalidInputException("Invalid inventoryId: " + body.getInventoryId());
    }

    InventoryEntity entity = mapper.apiToEntity(body);
    Mono<Inventory> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Inventory Id: " + body.getInventoryId()))
      .map(e -> mapper.entityToApi(e));

    return newEntity;
  }

  @Override
  public Mono<Inventory> getInventory(int inventoryId) {

    if (inventoryId < 1) {
      throw new InvalidInputException("Invalid inventoryId: " + inventoryId);
    }

    LOG.info("Will get inventory info for id={}", inventoryId);

    return repository.findByInventoryId(inventoryId)
      .switchIfEmpty(Mono.error(new NotFoundException("No inventory found for inventoryId: " + inventoryId)))
      .log(LOG.getName(), FINE)
      .publishOn(Schedulers.boundedElastic())
      .map(entity -> {
           int sleepTime = ThreadLocalRandom.current().nextInt(10, 3001);
           try {
               Thread.sleep(sleepTime);
           } catch (InterruptedException ex) {
               Thread.currentThread().interrupt();
               LOG.error("Sleep was interrupted", ex);
           }
           LOG.info("Slept for {} ms", sleepTime);
           return entity;
      })
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
  }

  @Override
  public Mono<Void> deleteInventory(int inventoryId) {

    if (inventoryId < 1) {
      throw new InvalidInputException("Invalid inventoryId: " + inventoryId);
    }

    LOG.debug("deleteInventory: tries to delete an entity with inventoryId: {}", inventoryId);
    return repository.findByInventoryId(inventoryId).log(LOG.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
  }

  private Inventory setServiceAddress(Inventory e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
  }
}
