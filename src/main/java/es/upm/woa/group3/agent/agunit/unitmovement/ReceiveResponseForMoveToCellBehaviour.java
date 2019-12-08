package es.upm.woa.group3.agent.agunit.unitmovement;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.MoveToCell;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ReceiveResponseForMoveToCellBehaviour extends CyclicBehaviour {
  private BaseAgent unitAgent;
  private Logger logger;

  public ReceiveResponseForMoveToCellBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      ACLMessage message = unitAgent.receiveMessage(Protocol.MOVE_TO_CELL);

      if (isNull(message)) {
        block();
        return;
      }

      Optional.of(unitAgent.extractContent(message))
          .filter(contentElement -> contentElement instanceof Action)
          .map(Action.class::cast)
          .ifPresent(a -> handleResponse(message, a));

    } catch (AgentException ex) {
      logger.log("ResponseForMoveToCell: Cannot process response from World " + ex.getMessage());
    }
  }

  private void handleResponse(ACLMessage msg, Action action) {

    Optional.of(action.getAction())
        .filter(concept -> concept instanceof MoveToCell)
        .ifPresent(
            concept -> {
              int msgType = msg.getPerformative();
              if (msgType == ACLMessage.NOT_UNDERSTOOD) {
                logger.log(
                    "Received NOT_UNDERSTOOD for MoveToCellAction from"
                        + msg.getSender().getLocalName());
              } else if (msgType == ACLMessage.REFUSE) {
                logger.log(
                    "Received REFUSE for MoveToCellAction from" + msg.getSender().getLocalName());
              } else if (msgType == ACLMessage.AGREE) {
//                logger.log(
//                    "Received AGREE for MoveToCellAction from... Waiting for final response "
//                        + msg.getSender().getLocalName());
              }
            });
  }
}
