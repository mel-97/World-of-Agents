package es.upm.woa.group3.util;

import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.ontology.Building;

public class BuildOntologyHelper {

  public static BuildingType mapToBuildingType(Building building) {
    String buildingType = building.getType();

    for (BuildingType bt : BuildingType.values()) {
      if (bt.getName().equals(buildingType)) {
        return bt;
      }
    }

    return BuildingType.NOT_FOUND;
  }
}
