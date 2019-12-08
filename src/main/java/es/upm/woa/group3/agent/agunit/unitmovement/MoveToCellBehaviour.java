package es.upm.woa.group3.agent.agunit.unitmovement;

import es.upm.woa.group3.agent.AgentType;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.MoveToCell;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import java.util.Random;

import static java.util.Objects.requireNonNull;

public class MoveToCellBehaviour extends CyclicBehaviour {

  private BaseAgent unitAgent;
  private Logger logger;
  private final Random random = new Random();

  public MoveToCellBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      final int testMovementDirection = random.nextInt(6) + 1;
      DFAgentDescription worldAgent = unitAgent.discoverAgent(myAgent, AgentType.WORLD.name());
      MoveToCell moveToCell = new MoveToCell();
      moveToCell.setTargetDirection(testMovementDirection);

      Action moveToCellAction = new Action(unitAgent.getAID(), moveToCell);
      ACLMessage message =
          unitAgent.newMessage(
              worldAgent.getName(), ACLMessage.REQUEST, Protocol.MOVE_TO_CELL, moveToCellAction);
//      logger.log("Sending message to move to another cell");

      unitAgent.send(message);
    } catch (AgentException e) {
//      logger.log("Cannot send new request to move to another cell... " + e.getMessage());
    }
  }
}
