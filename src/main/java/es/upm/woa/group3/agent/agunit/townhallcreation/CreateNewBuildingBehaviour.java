package es.upm.woa.group3.agent.agunit.townhallcreation;

import es.upm.woa.group3.agent.AgentType;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateBuilding;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.requireNonNull;

public class CreateNewBuildingBehaviour extends OneShotBehaviour {
  private BaseAgent unitAgent;
  private Logger logger;

  public CreateNewBuildingBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    DFAgentDescription worldAgent = unitAgent.discoverAgent(myAgent, AgentType.WORLD.name());
    BuildingType buildingType = BuildingType.TOWN_HALL;
    CreateBuilding createBuilding = new CreateBuilding();
    createBuilding.setBuildingType(buildingType.getName());
    ACLMessage message =
        unitAgent.newMessage(
            worldAgent.getName(),
            ACLMessage.REQUEST,
            Protocol.CREATE_BUILDING,
            new Action(unitAgent.getAID(), createBuilding));

    try {
//      logger.log("Sending message to create new Building");
      unitAgent.send(message);
    } catch (AgentException e) {
      logger.log("Cannot send message to create new Building" + e.getMessage());
    }
  }
}
