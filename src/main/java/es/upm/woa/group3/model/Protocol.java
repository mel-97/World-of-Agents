package es.upm.woa.group3.model;

public enum Protocol {
  CREATE_UNIT("CreateUnit"), // AgUnit <--> AgWorld
  CREATE_BUILDING("CreateBuilding"), // AgUnit <--> AgWorld
  NOTIFY_NEW_UNIT("NotifyNewUnit"), // AgWorld --> AgTribe
  NOTIFY_NEW_BUILDING("NotifyNewBuilding"), // AgWorld --> AgTribe
  MOVE_TO_CELL("MoveToCell"), // AgUnit --> AgWorld
  NOTIFY_UNIT_POSITION("NotifyUnitPosition"), // AgUnit --> AgTribe
  NOTIFY_NEW_CELL_DISCOVERY("NotifyNewCellDiscovery"), // AgWorld --> AgTribe
  REGISTER_TRIBE("RegisterTribe");


  private String name;

  Protocol(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
