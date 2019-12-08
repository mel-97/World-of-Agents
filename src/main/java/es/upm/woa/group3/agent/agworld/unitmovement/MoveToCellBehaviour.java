package es.upm.woa.group3.agent.agworld.unitmovement;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agworld.AgWorldService;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.GameBoard;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.MoveToCell;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class MoveToCellBehaviour extends CyclicBehaviour {
  private Logger logger;
  private BaseAgent worldAgent;
  private GameBoard gameBoard;
  private AgWorldService agWorldService;

  public MoveToCellBehaviour(
      BaseAgent worldAgent, GameBoard gameBoard, AgWorldService agWorldService) {
    this.worldAgent = requireNonNull(worldAgent);
    this.gameBoard = requireNonNull(gameBoard);
    this.agWorldService = requireNonNull(agWorldService);
    logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = worldAgent.receiveMessage(Protocol.MOVE_TO_CELL);

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
              "Received MoveToCell request from: [%s]", message.getSender().getLocalName()));

      MoveToCell moveToCell = validateMessage(message);
      Unit requesterUnit = getRequesterUnit(message);
      logRequest(message, moveToCell, requesterUnit);
      ACLMessage responseMessage = getResponseMessage(message);

      agWorldService.validateUnitMovement(requesterUnit, moveToCell.getTargetDirection());
      agWorldService.validateUnitsInMovement(requesterUnit, responseMessage);
      worldAgent.send(responseMessage);
      // launch Delayed Behaviour
      worldAgent.addBehaviour(
          new MoveToCellSuccessDelayedBahaviour(worldAgent, requesterUnit, agWorldService, 6000L));

    } catch (AgentException ex) {
      logger.log(
          String.format(
              "MoveToCell: Sending REFUSE to [%s]: %s",
              message.getSender().getLocalName(), ex.getMessage()));

      worldAgent.sendRefuseResponseOnError(
          message, new Action(worldAgent.getAID(), new MoveToCell()), Protocol.MOVE_TO_CELL);
    }
  }

  private void logRequest(ACLMessage message, MoveToCell moveToCell, Unit requesterUnit) {
    logger.log(
        String.format(
            "MoveToCell: Unit [%s] currently in position: [%d x %d], wants to move to direction: [%d] ",
            message.getSender().getLocalName(),
            requesterUnit.getX(),
            requesterUnit.getY(),
            moveToCell.getTargetDirection()));
  }

  private Unit getRequesterUnit(ACLMessage message) {
    return agWorldService
        .getUnitByAid(message.getSender())
        .orElseThrow(() -> new AgentException("Cannot find requester unit"));
  }

  private MoveToCell validateMessage(ACLMessage message) {
    return worldAgent.validateMessage(
        message.getPerformative(),
        ACLMessage.REQUEST,
        worldAgent.extractContent(message),
        MoveToCell.class);
  }

  private ACLMessage getResponseMessage(ACLMessage message) {
    return worldAgent.responseFromMessage(
        message,
        ACLMessage.REFUSE,
        Protocol.MOVE_TO_CELL,
        new Action(worldAgent.getAID(), new MoveToCell()));
  }
}
