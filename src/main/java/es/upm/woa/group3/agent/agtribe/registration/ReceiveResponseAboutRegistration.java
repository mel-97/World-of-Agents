package es.upm.woa.group3.agent.agtribe.registration;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.RegisterTribe;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ReceiveResponseAboutRegistration extends CyclicBehaviour {
  private BaseAgent tribeAgent;
  private Logger logger;

  public ReceiveResponseAboutRegistration(BaseAgent unitAgent) {
    this.tribeAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      ACLMessage message = tribeAgent.receiveMessage(Protocol.REGISTER_TRIBE);
      if (isNull(message)) {
        block();
        return;
      }

      Optional.of(tribeAgent.extractContent(message))
          .filter(contentElement -> contentElement instanceof Action)
          .map(Action.class::cast)
          .ifPresent(a -> handleResponse(message, a));

    } catch (AgentException ex) {
      logger.log("TribeRegistration: Cannot process response from World " + ex.getMessage());
    }
  }

  private void handleResponse(ACLMessage message, Action action) {
    Optional.of(action.getAction())
        .filter(concept -> concept instanceof RegisterTribe)
        .ifPresent(
            concept -> {
              int performative = message.getPerformative();
              switch (performative) {
                case ACLMessage.NOT_UNDERSTOOD:
                  logger.log(
                      "Received NOT_UNDERSTOOD for TribeRegistration from"
                          + message.getSender().getLocalName());
                  break;
                case ACLMessage.REFUSE:
                  logger.log(
                      "Received REFUSE for TribeRegistration from"
                          + message.getSender().getLocalName());
                  break;
                case ACLMessage.AGREE:
                  logger.log(
                      "Received AGREE for TribeRegistration from... Waiting for final response from "
                          + message.getSender().getLocalName());
                  break;
              }
            });
  }
}
