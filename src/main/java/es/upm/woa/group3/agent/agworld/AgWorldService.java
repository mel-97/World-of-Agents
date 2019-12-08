package es.upm.woa.group3.agent.agworld;

import es.upm.woa.group3.agent.AgTribe;
import es.upm.woa.group3.agent.AgUnit;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.GameBoard;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Tribe;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.NotifyNewUnit;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.upm.woa.group3.util.GameSettings.DURATION_OF_THE_GAME;
import static es.upm.woa.group3.util.GameSettings.FIRST_TRIBE_NAME;
import static es.upm.woa.group3.util.GameSettings.INIT_NUMBER_OF_TRIBES;
import static es.upm.woa.group3.util.GameSettings.INIT_NUMBER_OF_UNITS;
import static java.util.Objects.requireNonNull;

public class AgWorldService {
  private Logger logger;

  private BaseAgent worldAgent;
  private final List<Tribe> tribes = new ArrayList<>();
  private final GameBoard gameBoard = GameBoard.getInstance();
  private final List<Unit> unitsInMove = new ArrayList<>();

  private final LocalTime registerTime = LocalTime.now();

  public AgWorldService(BaseAgent worldAgent) {
    this.worldAgent = requireNonNull(worldAgent);
    this.logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  public void startAgentTribe() {
    for (int i = 0; i < INIT_NUMBER_OF_TRIBES; i++) {
      AID aid =
          worldAgent
              .startNewAgent(
                  worldAgent.getContainerController(), FIRST_TRIBE_NAME + i, new AgTribe())
              .orElseThrow(() -> new RuntimeException("Error during launching new worldAgent"));
      Tribe tribe = new Tribe(aid, FIRST_TRIBE_NAME + i);
      tribes.add(tribe);
    }
  }

  /** Launches new unit and creates TOWN_HALL in that cell */
  public void startUnits() {
    for (Tribe tribe : tribes) {
      for (int i = 0; i < INIT_NUMBER_OF_UNITS; i++) {
        // todo: agree on new units names
        // Cell cell = gameBoard.putTribeAndTownHall(tribe); TODO: I commented this to test creation
        // of new townhall
        Cell cell = gameBoard.getRandomPosition(); // TODO: test creation of new townhall
        Unit unit = startAgentUnit(cell, tribe, tribe.getName() + "-" + "UNIT" + i);
        tribe.addDiscoveredCell(cell);
        informTribeAboutNewUnit(cell, tribe, unit);
      }
    }
  }

  Unit startAgentUnit(Cell cell, Tribe tribe, String unitName) {
    AgUnit agUnit = new AgUnit();
    AID newUnitAid =
        worldAgent
            .startNewAgent(worldAgent.getContainerController(), unitName, agUnit)
            .orElseThrow(() -> new RuntimeException("Cannot create new UNIT:" + unitName));

    return new Unit(newUnitAid, unitName, tribe, cell.getX(), cell.getY());
  }

  void informTribeAboutNewUnit(Cell cell, Tribe ownerTribe, Unit unit) {
    ownerTribe.addUnit(unit);
    NotifyNewUnit notifyNewUnit = new NotifyNewUnit();
    notifyNewUnit.setLocation(cell);
    cell.setContent(new Empty());
    notifyNewUnit.setNewUnit(unit.getAid());

    try {
      Action agAction = new Action(ownerTribe.getAid(), notifyNewUnit);
      ACLMessage message =
          worldAgent.newMessage(
              ownerTribe.getAid(), ACLMessage.INFORM, Protocol.NOTIFY_NEW_UNIT, agAction);

      worldAgent.send(message);
      logger.log("Informs a tribe about unit creation: " + ownerTribe.getAid().getName());
    } catch (AgentException ex) {
      logger.log("Cannot inform Tribe About New Unit " + ex.getMessage());
    }
  }

  public boolean isGameFinished() {
    // todo: test this ...
    // game finishes when time elapsed is greater then duration of game
    Duration timeElapsed = Duration.between(this.registerTime, LocalTime.now());
    return timeElapsed.compareTo(DURATION_OF_THE_GAME) > 0;
  }

  List<Tribe> getTribes() {
    return tribes;
  }

  List<Unit> getUnitsInMove() {
    return unitsInMove;
  }

  public Optional<Unit> getUnitByAid(AID unitAid) {
    for (Tribe t : tribes) {
      if (t.containsUnit(unitAid)) {
        return t.getUnitByAid(unitAid);
      }
    }
    return Optional.empty();
  }

  public void validateUnitMovement(Unit requesterUnit, int targetDirection) {
    if (!gameBoard.canUnitMove(requesterUnit, targetDirection)) {
      throw new AgentException("Unit cannot move - wrong target direction: " + targetDirection);
    }
  }

  public void validateUnitsInMovement(Unit sender, ACLMessage response) {
    if (unitsInMove.contains(sender)) {
      response.setPerformative(ACLMessage.REFUSE);
      throw new AgentException(
          String.format("Unit%s already in move - sending refuse", sender.getAid().getLocalName()));
    } else if (isGameFinished()) {
      response.setPerformative(ACLMessage.FAILURE);
      throw new AgentException("Game ended - sending failure" + sender.getAid().getLocalName());
    } else {
      logger.log("Move to cell accepted" + sender.getAid().getLocalName());
      response.setPerformative(ACLMessage.AGREE);
      unitsInMove.add(sender);
    }
  }
  // TODO: 19.05.19 Merge this 2 methods
  public void validateUnitsInMovementForBuildingCreation(Unit sender, ACLMessage response) {
    if (unitsInMove.contains(sender)) {
      response.setPerformative(ACLMessage.REFUSE);
      throw new AgentException(
          String.format("Unit%s already in move - sending refuse", sender.getAid().getLocalName()));
    } else if (isGameFinished()) {
      response.setPerformative(ACLMessage.FAILURE);
      throw new AgentException("Game ended - sending failure" + sender.getAid().getLocalName());
    } else {
      logger.log("Requester not in move - sending AGREE");
      response.setPerformative(ACLMessage.AGREE);
    }
  }

  public Optional<Tribe> getTribeByUnitAID(AID unitAid) {
    return tribes.stream().filter(tribe -> tribe.containsUnit(unitAid)).findAny();
  }
}
