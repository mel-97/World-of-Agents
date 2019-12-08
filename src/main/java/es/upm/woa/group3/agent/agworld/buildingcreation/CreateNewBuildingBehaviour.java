package es.upm.woa.group3.agent.agworld.buildingcreation;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agworld.AgWorldService;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.group3.model.GameBoard;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Tribe;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CellContent;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.Empty;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_GOLD_PRICE;
import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_STONE_PRICE;
import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_WOOD_PRICE;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class CreateNewBuildingBehaviour extends CyclicBehaviour {
  private Logger logger;
  private BaseAgent worldAgent;
  private GameBoard gameBoard;
  private AgWorldService agWorldService;

  public CreateNewBuildingBehaviour(
      BaseAgent worldAgent, GameBoard gameBoard, AgWorldService agWorldService) {
    this.worldAgent = requireNonNull(worldAgent);
    this.gameBoard = requireNonNull(gameBoard);
    this.agWorldService = requireNonNull(agWorldService);
    logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  public void action() {
    ACLMessage msg = worldAgent.receiveMessage(Protocol.CREATE_BUILDING);

    if (isNull(msg)) {
      block();
      return;
    }
    handleMessage(msg);
  }

  private void handleMessage(ACLMessage message) {
    try {
      logger.log("Received message for CreateNewBuilding");
      CreateBuilding createBuilding = validateMessage(message);
      AID sender = message.getSender();
      logger.log(
          String.format(
              "Unit [%s] wants to createBuilding: %s",
              sender.getLocalName(), createBuilding.getBuildingType()));

      ACLMessage response = getResponseMessage(message, createBuilding, sender);
      Tribe tribe =
          agWorldService
              .getTribeByUnitAID(sender)
              .orElseThrow(() -> new AgentException("Sender Unit doesn't belong to any tribe!!"));

      Unit unit =
          tribe
              .getUnitByAid(sender)
              .orElseThrow(() -> new AgentException("Unit doesn't belong to tribe...."));

      agWorldService.validateUnitsInMovementForBuildingCreation(unit, response);

      // todo: not finished but send OK for test
      Cell cellByPosition = gameBoard.getCellByPosition(unit.getX(), unit.getY());

      CellContent content = cellByPosition.getContent();
      System.out.println("content: " + content);

      // TODO: 19.05.19 cell content could be Empty Or Building

      CellContent cellContent = cellByPosition.getContent();

      if (cellContent instanceof Empty) {
        handleWhenUnitInFreeCell(message, response, tribe, cellByPosition);
      } else if (cellContent instanceof Building) {
        Building building = (Building) cellContent;
        if (building.getType() == null) {
          throw new AgentException(
              "Assertion error - cellContent should have type when type of building");
        }
      } else {
        throw new AgentException("Unit not in a free cell");
      }

    } catch (AgentException ex) {
      logger.log(ex.getMessage());
      // TODO: 19.05.19 check for other building types
      BuildingType buildingType = BuildingType.TOWN_HALL;
      CreateBuilding createBuilding = new CreateBuilding();
      createBuilding.setBuildingType(buildingType.getName());
      worldAgent.sendRefuseResponseOnError(
          message, new Action(message.getSender(), createBuilding), Protocol.CREATE_BUILDING);
    }
  }

  private void handleWhenUnitInFreeCell(
      ACLMessage message, ACLMessage response, Tribe tribe, Cell cellByPosition) {
    if (tryToCreateNewBuilding(tribe)) {
      response.setPerformative(ACLMessage.AGREE);
      worldAgent.send(response);
      worldAgent.addBehaviour(new CreateNewBuildingSuccessDelayedBehaviour(worldAgent, message.getSender(), agWorldService, 6000));
    } else {
      throw new AgentException("Cannot create new Building - not enough resources");
    }
  }

  private CreateBuilding validateMessage(ACLMessage message) {
    return worldAgent.validateMessage(
        message.getPerformative(),
        ACLMessage.REQUEST,
        worldAgent.extractContent(message),
        CreateBuilding.class);
  }

  private ACLMessage getResponseMessage(
      ACLMessage message, CreateBuilding createBuilding, AID sender) {
    return worldAgent.responseFromMessage(
        message, ACLMessage.REFUSE, Protocol.CREATE_BUILDING, new Action(sender, createBuilding));
  }

  private boolean tryToCreateNewBuilding(
      Tribe tribe) { // TODO: if future any building, now TownHall
    logger.log("Unit in appropriate cell to create building");
    if (tribe.getGold() > TOWN_HALL_GOLD_PRICE
        && tribe.getStone() > TOWN_HALL_STONE_PRICE
        && tribe.getWood() > TOWN_HALL_WOOD_PRICE) {
      logger.log("Creating new building successful - tribe has enough resources...");
      tribe.setGold(tribe.getGold() - TOWN_HALL_GOLD_PRICE);
      tribe.setGold(tribe.getStone() - TOWN_HALL_STONE_PRICE);
      tribe.setGold(tribe.getWood() - TOWN_HALL_WOOD_PRICE);
      return true;
    } else {
      logger.log("Not enough resources to create TownHall...");
      return false;
    }
  }
}
