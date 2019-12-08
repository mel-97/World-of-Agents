package es.upm.woa.group3.agent.agunit.unitmovement;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.MoveToCell;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ReceiveFinalResponseAboutMoveToCellBehaviour extends CyclicBehaviour {
  private BaseAgent unitAgent;
  private Logger logger;

  public ReceiveFinalResponseAboutMoveToCellBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {

    ACLMessage message = unitAgent.receiveMessage(Protocol.MOVE_TO_CELL);
    if (Objects.isNull(message)) {
      block();
      return;
    }
    try {
      MoveToCell moveToCell =
          Optional.of(message)
              .map(m -> unitAgent.extractContent(m))
              .filter(Action.class::isInstance)
              .map(Action.class::cast)
              .map(Action::getAction)
              .filter(MoveToCell.class::isInstance)
              .map(MoveToCell.class::cast)
              .orElseThrow(() -> new AgentException("Cannot process response from AgWorld"));

      handleResponse(message, moveToCell);

    } catch (AgentException ex) {
      logger.log(String.format("Cannot process response from world %s", ex.getMessage()));
    }
  }

  private void handleResponse(ACLMessage msg, MoveToCell moveToCell) {
    int performative = msg.getPerformative();
    // TODO: 19.05.19 Display new unit location
    Cell newlyArrivedCell = moveToCell.getNewlyArrivedCell();
    if (performative == ACLMessage.FAILURE) {
      logger.log(
          String.format(
              "Received FAILURE about Move  from %s. AGENT NOT MOVED TO POSITION: [%d]",
              msg.getSender().getLocalName(), moveToCell.getTargetDirection()));
    } else if (performative == ACLMessage.INFORM) {
      logger.log(
          String.format(
              "Received INFORM about Move from %s. AGENT SUCCESSFULLY MOVED TO ANOTHER CELL: [%d]!",
              msg.getSender().getLocalName(), moveToCell.getTargetDirection()));
    }
  }
}
