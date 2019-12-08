package es.upm.woa.group3.agent.agworld;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.group3.model.GameBoard;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Tribe;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateUnit;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static es.upm.woa.group3.util.GameSettings.DURATION_OF_UNIT_CREATION;
import static es.upm.woa.group3.util.GameSettings.UNIT_FOOD_PRICE;
import static es.upm.woa.group3.util.GameSettings.UNIT_GOLD_PRICE;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class CreateNewUnitBehaviour extends CyclicBehaviour {
  private Logger logger;
  private BaseAgent worldAgent;
  private GameBoard gameBoard;
  private AgWorldService agWorldService;

  public CreateNewUnitBehaviour(
      BaseAgent worldAgent, GameBoard gameBoard, AgWorldService agWorldService) {
    this.worldAgent = requireNonNull(worldAgent);
    this.gameBoard = requireNonNull(gameBoard);
    this.agWorldService = requireNonNull(agWorldService);
    logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = worldAgent.receiveMessage(Protocol.CREATE_UNIT);
    if (isNull(msg)) {
      block();
      return;
    }
    handleMessage(msg);
  }

  private void handleMessage(ACLMessage message) {
    try {
      logger.log(
          String.format(
              "CreateNewUnit: Received CreateUnit request from: [%s]",
              message.getSender().getLocalName()));
      ContentElement ce = worldAgent.extractContent(message);
      // ok
      worldAgent.validateMessage(
          message.getPerformative(), ACLMessage.REQUEST, ce, CreateUnit.class);

      ACLMessage response =
          worldAgent.responseFromMessage(
              message,
              ACLMessage.REFUSE,
              Protocol.CREATE_UNIT,
              new Action(worldAgent.getAID(), new CreateUnit()));

      AID sender = message.getSender();
      // get tribe that sender belongs to
      Tribe tribe = getTribe(sender);
      Unit unit = getUnit(sender, tribe);

      validateUnitsInMove(unit);

      Cell cellByPosition = gameBoard.getCellByPosition(unit.getX(), unit.getY());
//      Building building = worldAgent.getContentFromCell(cellByPosition, Building.class);
      Building building = new Building();
      building.setType(BuildingType.EMPTY.getName());
      validateUnitInTownHall(building, sender, response);
      validateSameTribe(response, tribe, building);
      handleWhenUnitInTownHall(message, response, tribe, cellByPosition);
    } catch (AgentException e) {
      logger.log(
          String.format(
              "CreateNewUnit: Sending REFUSE to [%s]: %s",
              message.getSender().getLocalName(), e.getMessage()));

      worldAgent.sendRefuseResponseOnError(
          message, new Action(worldAgent.getAID(), new CreateUnit()), Protocol.CREATE_UNIT);
    }
  }

  private Unit getUnit(AID sender, Tribe tribe) {
    return tribe
        .getUnitByAid(sender)
        .orElseThrow(() -> new RuntimeException("Unit doesn't belong to tribe...."));
  }

  private Tribe getTribe(AID sender) {
    return getTribeByUnitAID(sender)
        .orElseThrow(() -> new RuntimeException("Sender Unit doesn't belong to any tribe!!"));
  }

  private void validateSameTribe(ACLMessage response, Tribe tribe, Building building) {
    Boolean isTheSameTribe =
        Optional.of(building)
            .map(Building::getOwner)
            .map(aid -> aid.equals(tribe.getAid()))
            .orElse(false);

    if (isTheSameTribe) {
      logger.log("Trying to create new UNIT in other tribe town hall");
      response.setPerformative(ACLMessage.REFUSE);
      worldAgent.send(response);
    }
  }

  private void validateUnitInTownHall(Building building, AID sender, ACLMessage response) {

    Boolean isInTownHall =
        Optional.of(building)
            .map(Building::getType)
            .map(s -> BuildingType.TOWN_HALL.getName().equals(s))
            .orElse(false);

    if (!isInTownHall) {
      throw new AgentException(
          String.format("Unit not in a town hall: [%s]", sender.getLocalName()));
    }
  }

  private void validateUnitsInMove(Unit unit) {
    if (agWorldService.getUnitsInMove().contains(unit)) {
      throw new AgentException("Cannot create new unit - Unit in move");
    }
  }

  private void handleWhenUnitInTownHall(
      ACLMessage message, ACLMessage response, Tribe tribe, Cell cellByAid) {

    if (!tryToCreateNewUnit(tribe)) {
      throw new AgentException("Cannot create new agent - not enough resources...");
    }
    response.setPerformative(ACLMessage.AGREE);
    worldAgent.send(response);
    worldAgent.doWait(DURATION_OF_UNIT_CREATION);
    if (agWorldService.isGameFinished()) {
      refundUnitPay(tribe);
      throw new AgentException("New agent haven't created, game is over, resources were returned");
    }
    Unit unit =
        agWorldService.startAgentUnit(cellByAid, tribe, tribe.getName() + "--new-unit-name");

    logger.log("Launched new agent");
    sendFinalResponseToUnit(message);
    agWorldService.informTribeAboutNewUnit(cellByAid, tribe, unit);
  }

  private Optional<Tribe> getTribeByUnitAID(AID unitAid) {
    return agWorldService.getTribes().stream()
        .filter(tribe -> tribe.containsUnit(unitAid))
        .findAny();
  }

  private void sendFinalResponseToUnit(ACLMessage msg) {
    ACLMessage message =
        worldAgent.responseFromMessage(msg, ACLMessage.INFORM, Protocol.CREATE_UNIT);
    worldAgent.send(message);
  }

  private boolean tryToCreateNewUnit(Tribe tribe) {
    logger.log("Found unit and unit in town hall");
    if (tribe.getGold() > UNIT_GOLD_PRICE && tribe.getFood() > UNIT_FOOD_PRICE) {
      logger.log("Creating new Unit successful - tribe has enough resources...");
      tribe.setGold(tribe.getGold() - UNIT_GOLD_PRICE);
      tribe.setFood(tribe.getFood() - UNIT_FOOD_PRICE);
      return true;
    } else {
      logger.log("Not enough resources to create unit...");
      return false;
    }
  }

  private void refundUnitPay(Tribe tribe) {
    tribe.setGold(tribe.getGold() + UNIT_GOLD_PRICE);
    tribe.setFood(tribe.getFood() + UNIT_FOOD_PRICE);
  }

  private void validate(ACLMessage msg) throws Codec.CodecException, OntologyException {}
}
