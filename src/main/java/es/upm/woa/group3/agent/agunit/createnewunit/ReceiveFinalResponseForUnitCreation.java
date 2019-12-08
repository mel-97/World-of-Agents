package es.upm.woa.group3.agent.agunit.createnewunit;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateUnit;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ReceiveFinalResponseForUnitCreation extends CyclicBehaviour {

  private BaseAgent agent;
  private Logger logger;

  public ReceiveFinalResponseForUnitCreation(BaseAgent agent) {
    this.agent = requireNonNull(agent);
    this.logger = Logger.getLogger(agent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage message = agent.receiveMessage(Protocol.CREATE_UNIT);
    if (isNull(message)) {
      block();
      return;
    }
    try {
      logger.log("UnitCreation: Received Final Response For Unit Creation");
      ContentElement contentElement = agent.extractContent(message);
      Optional.of(contentElement)
          .filter(ce -> ce instanceof Action)
          .map(Action.class::cast)
          .map(Action::getAction)
          .filter(concept -> concept instanceof CreateUnit)
          .ifPresent(c -> handleResponse(message));

    } catch (AgentException e) {
      logger.log("UnitCreation: Cannot process response from World " + e.getMessage());
    }
  }

  private void handleResponse(ACLMessage msg) {
    int messageCode = msg.getPerformative();
    if (messageCode == ACLMessage.FAILURE) {
      logger.log(
          "Received FAILURE from "
              + msg.getSender().getLocalName()
              + "AGENT NOT CREATED AFTER AGREE");
    } else if (messageCode == ACLMessage.INFORM) {
      logger.log(
          "Received INFORM from " + msg.getSender().getLocalName() + "AGENT SUCCESSFULLY CREATED!");
    }
  }
}
