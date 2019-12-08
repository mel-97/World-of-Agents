package es.upm.woa.group3.agent;

import es.upm.woa.group3.agent.agworld.AgWorldService;
import es.upm.woa.group3.agent.agworld.CreateNewUnitBehaviour;
import es.upm.woa.group3.agent.agworld.buildingcreation.CreateNewBuildingBehaviour;
import es.upm.woa.group3.agent.agworld.unitmovement.MoveToCellBehaviour;
import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.group3.model.GameBoard;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Tribe;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.group3.util.MessageUtils;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.NotifyCellDetail;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.upm.woa.group3.util.GameSettings.DURATION_OF_THE_GAME;
import static es.upm.woa.group3.util.GameSettings.DURATION_OF_TOWN_HALL_CREATION;
import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_GOLD_PRICE;
import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_STONE_PRICE;
import static es.upm.woa.group3.util.GameSettings.TOWN_HALL_WOOD_PRICE;
import static java.util.Objects.nonNull;

public class AgWorld extends BaseAgent {
  private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

  private final GameBoard gameBoard = GameBoard.getInstance();
  private final List<Tribe> tribes = new ArrayList<>();
  private final List<AID> unitsInMove = new ArrayList<>();
  private final LocalTime registerTime = LocalTime.now();

  private AgWorldService agWorldService = new AgWorldService(this);

  @Override
  protected void setup() {
    super.setup(AgentType.WORLD);
    agWorldService.startAgentTribe();
    agWorldService.startUnits();
  }

  @Override
  protected void registerBehaviours() {
    this.addBehaviour(new CreateNewUnitBehaviour(this, GameBoard.getInstance(), agWorldService));
    this.addBehaviour(new MoveToCellBehaviour(this, GameBoard.getInstance(), agWorldService));
    this.addBehaviour(
        new CreateNewBuildingBehaviour(this, GameBoard.getInstance(), agWorldService));
  }

  // Listen to INFORM messages... When new AgUnit is created it sends this message to inform about
  // new UnitCreation...

  private boolean isGameFinished() {
    // todo: test this ...
    // game finishes when time elapsed is greater then duration of game
    Duration timeElapsed = Duration.between(this.registerTime, LocalTime.now());
    return timeElapsed.compareTo(DURATION_OF_THE_GAME) > 0;
  }

  /* *********************************** BEHAVIOURS RELATED TO CREATION OF THE BUILDING **********************/
  private class CreateNewBuildingBehaviourOld extends CyclicBehaviour {

    @Override
    public void action() {
      ACLMessage msg = receiveMessage(Protocol.CREATE_BUILDING);

      if (nonNull(msg)) {
        handleMessage(msg);
      } else {
        block();
      }
    }

    private void handleMessage(ACLMessage message) {
      try {
        checkPrerequistites(message);
        logger.log("Received CreateBuilding request from " + message.getSender().getLocalName());

        // extracting building from message
        ContentElement ce = getContentManager().extractContent(message);
        Action agAction = (Action) ce;
        Concept conc = agAction.getAction();
        CreateBuilding createBuilding = (CreateBuilding) conc;
        String buildingTypeCheck = createBuilding.getBuildingType();
        BuildingType buildingType;
        if (buildingTypeCheck
            == BuildingType.TOWN_HALL
                .getName()) { // TODO: check another types of buildings, not sure that it is right
          // realization
          buildingType = BuildingType.TOWN_HALL;
        } else {
          buildingType = BuildingType.EMPTY;
        }
        // end extracting building from message

        ACLMessage response =
            MessageUtils.responseFromMessage(message, ontology, codec, ACLMessage.NOT_UNDERSTOOD);
        getContentManager().fillContent(response, new Action(getAID(), createBuilding));

        AID sender = message.getSender();
        Tribe tribe =
            getTribeByUnitAID(sender)
                .orElseThrow(
                    () -> new RuntimeException("Sender Unit doesn't belong to any tribe!!"));

        Unit unit =
            tribe
                .getUnitByAid(sender)
                .orElseThrow(() -> new RuntimeException("Unit doesn't belong to tribe...."));

        if (unitsInMove.contains(unit.getAid())) {
          logger.log("UNIT IN MOVE - CAN'T Create new building.... ");
          response.setPerformative(ACLMessage.REFUSE);
          send(response);
          return;
        }

        Cell cellByPosition = gameBoard.getCellByPosition(unit.getX(), unit.getY());

        Building buildingOnCell = (Building) cellByPosition.getContent(); // get what's on cell

        if (buildingOnCell.getType() == null) {
          handleWhenUnitInFreeCell(message, response, tribe, cellByPosition, buildingType);
        } else {
          logger.log("Unit not in a free cell" + sender.getLocalName());
          response.setPerformative(ACLMessage.REFUSE);
          send(response);
        }
      } catch (Codec.CodecException | OntologyException | RuntimeException e) {

        logger.log("Error during receiving a message: " + e.getMessage());
        ACLMessage response =
            MessageUtils.responseFromMessage(message, ontology, codec, ACLMessage.NOT_UNDERSTOOD);
        try {
          getContentManager().fillContent(message, new Action(getAID(), new CreateUnit()));
          send(response);
        } catch (Codec.CodecException | OntologyException e1) {
          e1.printStackTrace();
        }
      }
    }

    private void handleWhenUnitInFreeCell(
        ACLMessage message,
        ACLMessage response,
        Tribe tribe,
        Cell cellByAid,
        BuildingType buildingType)
        throws Codec.CodecException, OntologyException {

      boolean success = tryToCreateNewBuilding(tribe);
      if (success) {
        response.setPerformative(ACLMessage.AGREE);
        send(response);
        doWait(DURATION_OF_TOWN_HALL_CREATION);
        if (isGameFinished()) {
          refundBuildingPay(tribe);
          logger.log("New TownHall haven't created, game is over, resources were returned");
        } else {
          Building building = new Building();
          building.setOwner(tribe.getAid());
          building.setType(buildingType.getName());
          cellByAid.setContent(building);
          AgWorld agWorld = (AgWorld) myAgent;
          logger.log(
              String.format(
                  "The new TownHall was built in cell: [%d x %d]",
                  cellByAid.getX(), cellByAid.getY()));
          sendFinalResponseToUnit(agWorld, message, buildingType);
          informTribeAboutNewBuilding(cellByAid, tribe);
        }
      } else {
        response.setPerformative(ACLMessage.REFUSE);
        send(response);
      }
    }

    private Optional<Tribe> getTribeByUnitAID(AID unitAid) {
      return tribes.stream().filter(tribe -> tribe.containsUnit(unitAid)).findAny();
    }

    private void sendFinalResponseToUnit(AgWorld agent, ACLMessage msg, BuildingType buildingType)
        throws Codec.CodecException, OntologyException {
      ACLMessage newmsg = new ACLMessage(ACLMessage.UNKNOWN);
      newmsg.setOntology(ontology.getName());
      newmsg.setLanguage(codec.getName());
      newmsg.setProtocol(Protocol.CREATE_BUILDING.getName());
      CreateBuilding createBuilding = new CreateBuilding();
      createBuilding.setBuildingType(buildingType.getName());
      getContentManager().fillContent(newmsg, new Action(getAID(), createBuilding));
      newmsg.addReceiver(msg.getSender());
      newmsg.setPerformative(ACLMessage.INFORM);
      agent.send(newmsg);
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

    private void refundBuildingPay(Tribe tribe) {
      tribe.setGold(tribe.getGold() + TOWN_HALL_GOLD_PRICE);
      tribe.setGold(tribe.getStone() + TOWN_HALL_STONE_PRICE);
      tribe.setGold(tribe.getWood() + TOWN_HALL_WOOD_PRICE);
    }

    private void checkPrerequistites(ACLMessage msg)
        throws Codec.CodecException, OntologyException {
      if (msg.getPerformative() != ACLMessage.REQUEST) {
        logger.log("Message is not of type ACLMessage.REQUEST");
        throw new RuntimeException("Message is not of type ACLMessage.REQUEST");
      }
      ContentElement ce = getContentManager().extractContent(msg);
      if (!(ce instanceof Action)) {
        logger.log("Content element is not of type Action");
        throw new RuntimeException("Content element is not of type Action");
      }

      Action agAction = (Action) ce;
      Concept conc = agAction.getAction();

      if (!(conc instanceof CreateBuilding)) {
        logger.log("Error during receiving a message: Concept is not a instance of CreateBuilding");
        throw new RuntimeException(
            "Error during receiving a message: Concept is not a instance of CreateBuilding");
      }
    }

    private void informTribeAboutNewBuilding(Cell cell, Tribe ownerTribe) {
      ACLMessage message =
          MessageUtils.newAclMessage(
              ownerTribe.getAid(),
              ACLMessage.INFORM,
              ontology,
              codec,
              Protocol.NOTIFY_NEW_BUILDING);
      for (Unit u : ownerTribe.getUnits()) {
        message.addReceiver(u.getAid());
      }
      NotifyCellDetail cellDiscovery = new NotifyCellDetail();
      cellDiscovery.setNewCell(cell);
      Action action = new Action(ownerTribe.getAid(), cellDiscovery);

      try {
        getContentManager().fillContent(message, action);
        send(message);
        logger.log("Informs a tribe about new building creation: " + ownerTribe.getAid().getName());
      } catch (Codec.CodecException | OntologyException ex) {
        logger.log("Error - cannot inform about new building creation..." + ex.getMessage());
      }
    }
  }

  /* *********************************** END OF BEHAVIOURS RELATED TO CREATION OF THE BUILDING **********************/
}
