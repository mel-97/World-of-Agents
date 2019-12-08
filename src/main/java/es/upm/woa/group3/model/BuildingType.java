package es.upm.woa.group3.model;

public enum BuildingType {
  NOT_FOUND("NotFound"),
  EMPTY("Empty"),
  TOWN_HALL("Town Hall"),
  STORE("Store"),
  FARM("Farm");

  BuildingType(String name) {
    this.name = name;
  }

  private String name;

  public String getName() {
    return name;
  }
}
