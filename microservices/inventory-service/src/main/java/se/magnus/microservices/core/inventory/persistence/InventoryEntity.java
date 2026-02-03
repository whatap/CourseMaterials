package se.magnus.microservices.core.inventory.persistence;

import static java.lang.String.format;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventorys")
public class InventoryEntity {

  @Id private String id;

  @Version private Integer version;

  @Indexed(unique = true)
  private int inventoryId;

  private String name;
  private int weight;

  public InventoryEntity() {}

  public InventoryEntity(int inventoryId, String name, int weight) {
    this.inventoryId = inventoryId;
    this.name = name;
    this.weight = weight;
  }

  @Override
  public String toString() {
    return format("InventoryEntity: %s", inventoryId);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public int getInventoryId() {
    return inventoryId;
  }

  public void setInventoryId(int inventoryId) {
    this.inventoryId = inventoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
}
