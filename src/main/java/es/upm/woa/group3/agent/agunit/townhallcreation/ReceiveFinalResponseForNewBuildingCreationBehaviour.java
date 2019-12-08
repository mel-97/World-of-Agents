package es.upm.woa.group3.agent.agunit.townhallcreation;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agunit.createnewunit.CreateNewUnitBehaviour;
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

public class ReceiveFinalResponseForNewBuildingCreationBehaviour extends CyclicBehaviour {

  private BaseAgent unitAgent;
  private Logger logger;

  public ReceiveFinalResponseForNewBuildingCreationBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = unitAgent.receiveMessage(Protocol.CREATE_BUILDING);
    if (nonNull(msg)) {
      try {
        ContentElement ce = unitAgent.extractContent(msg);
        if (ce instanceof Action) {
          Action agAction = (Action) ce;
          Concept conc = agAction.getAction();
          if (conc instanceof CreateBuilding) {
            int i = msg.getPerformative();
            handleResponse(msg, i);
          }
        }
      } catch (AgentException e) {
        e.printStackTrace();
      }
    } else {
      block();
    }
  }

  private void handleResponse(ACLMessage msg, int messageCode) {
    if (messageCode == ACLMessage.FAILURE) {
      logger.log(
          "Received FAILURE from "
              + msg.getSender().getLocalName()
              + "Building not created AFTER AGREE");
    } else if (messageCode == ACLMessage.INFORM) {
      logger.log(
          "Received INFORM from "
              + msg.getSender().getLocalName()
              + "- NEW TOWN_HALL SUCCESSFULLY CREATED!");
      unitAgent.addBehaviour(new CreateNewUnitBehaviour(unitAgent));
    }
  }
}
