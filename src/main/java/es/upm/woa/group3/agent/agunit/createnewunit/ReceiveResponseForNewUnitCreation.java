package es.upm.woa.group3.agent.agunit.createnewunit;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateUnit;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ReceiveResponseForNewUnitCreation extends CyclicBehaviour {

  private BaseAgent unitAgent;
  private Logger logger;

  public ReceiveResponseForNewUnitCreation(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      ACLMessage message = unitAgent.receiveMessage(Protocol.CREATE_UNIT);
      if (isNull(message)) {
        block();
        return;
      }

      Optional.of(unitAgent.extractContent(message))
          .filter(contentElement -> contentElement instanceof Action)
          .map(Action.class::cast)
          .ifPresent(a -> handleResponse(message, a));

    } catch (AgentException ex) {
      logger.log("UnitCreation: Cannot process response from World " + ex.getMessage());
    }
  }

  private void handleResponse(ACLMessage message, Action action) {
    Optional.of(action.getAction())
        .filter(concept -> concept instanceof CreateUnit)
        .ifPresent(
            concept -> {
              int performative = message.getPerformative();
              switch (performative) {
                case ACLMessage.NOT_UNDERSTOOD:
                  logger.log(
                      "Received NOT_UNDERSTOOD for NewUnitCreationAction from"
                          + message.getSender().getLocalName());
                  break;
                case ACLMessage.REFUSE:
                  logger.log(
                      "Received REFUSE for NewUnitCreationAction from"
                          + message.getSender().getLocalName());
                  break;
                case ACLMessage.AGREE:
                  logger.log(
                      "Received AGREE for NewUnitCreationAction from... Waiting for final response"
                          + message.getSender().getLocalName());
                  break;
              }
            });
  }
}
