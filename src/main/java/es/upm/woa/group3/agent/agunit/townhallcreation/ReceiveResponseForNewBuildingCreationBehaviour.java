package es.upm.woa.group3.agent.agunit.townhallcreation;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateBuilding;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class ReceiveResponseForNewBuildingCreationBehaviour extends CyclicBehaviour {

  private BaseAgent unitAgent;
  private Logger logger;

  public ReceiveResponseForNewBuildingCreationBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      ACLMessage msg = unitAgent.receiveMessage(Protocol.CREATE_BUILDING);
      if (nonNull(msg)) {
        ContentElement ce = unitAgent.extractContent(msg);
        if (ce instanceof Action) {
          handleResponse(msg, (Action) ce);
        }
      } else {
        block();
      }

    } catch (AgentException ex) {
      logger.log(
          "ResponseForCreateNewBuilding: Cannot process response from World " + ex.getMessage());
    }
  }

  private void handleResponse(ACLMessage msg, Action ce) {
    Action agAction = ce;
    Concept conc = agAction.getAction();
    if (conc instanceof CreateBuilding) {
      int msgType = msg.getPerformative();
      if (msgType == ACLMessage.NOT_UNDERSTOOD) {
        logger.log(
            "Received NOT_UNDERSTOOD for CreateNewBuildingAction from"
                + msg.getSender().getLocalName());
      } else if (msgType == ACLMessage.REFUSE) {
        logger.log(
            "Received REFUSE for CreateNewBuildingAction from" + msg.getSender().getLocalName());
      } else if (msgType == ACLMessage.AGREE) {
        logger.log(
            "Received AGREE for CreateNewBuildingAction from... Waiting for final response "
                + msg.getSender().getLocalName());
      }
    }
  }
}
