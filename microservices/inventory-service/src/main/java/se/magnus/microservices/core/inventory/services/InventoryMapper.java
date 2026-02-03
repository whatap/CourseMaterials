package se.magnus.microservices.core.inventory.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.inventory.Inventory;
import se.magnus.microservices.core.inventory.persistence.InventoryEntity;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Inventory entityToApi(InventoryEntity entity);

  @Mappings({
    @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true)
  })
  InventoryEntity apiToEntity(Inventory api);
}
