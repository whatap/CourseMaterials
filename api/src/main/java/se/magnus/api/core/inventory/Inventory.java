package se.magnus.api.core.inventory;

public class Inventory {
  private int inventoryId;
  private String name;
  private int weight;
  private String serviceAddress;

  public Inventory() {
    inventoryId = 0;
    name = null;
    weight = 0;
    serviceAddress = null;
  }

  public Inventory(int inventoryId, String name, int weight, String serviceAddress) {
    this.inventoryId = inventoryId;
    this.name = name;
    this.weight = weight;
    this.serviceAddress = serviceAddress;
  }

  public int getInventoryId() {
    return inventoryId;
  }

  public String getName() {
    return name;
  }

  public int getWeight() {
    return weight;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public void setInventoryId(int inventoryId) {
    this.inventoryId = inventoryId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }
}
