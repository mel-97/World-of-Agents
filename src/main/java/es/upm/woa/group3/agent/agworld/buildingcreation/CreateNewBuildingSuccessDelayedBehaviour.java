package es.upm.woa.group3.agent.agworld.buildingcreation;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agworld.AgWorldService;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.BuildingType;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateBuilding;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CreateNewBuildingSuccessDelayedBehaviour extends WakerBehaviour {
  private BaseAgent worldAgent;
  private AID requesterUnit;
  private Logger logger;
  private AgWorldService agWorldService;

  private CreateNewBuildingSuccessDelayedBehaviour(Agent a, long tickTimeout) {
    super(a, tickTimeout);
    BaseAgent baseAgent =
        Optional.of(a)
            .filter(BaseAgent.class::isInstance)
            .map(BaseAgent.class::cast)
            .orElseThrow(() -> new AgentException("Agent is not of type BaseAgent"));
    this.worldAgent = requireNonNull(baseAgent);
    this.logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  public CreateNewBuildingSuccessDelayedBehaviour(
      Agent a, AID requesterUnit, AgWorldService agWorldService, long tickTimeout) {
    this(a, tickTimeout);
    this.requesterUnit = requireNonNull(requesterUnit);
    this.agWorldService = requireNonNull(agWorldService);
  }

  @Override
  public void handleElapsedTimeout() {
      BuildingType buildingType = BuildingType.TOWN_HALL;
      CreateBuilding createBuilding = new CreateBuilding();
      createBuilding.setBuildingType(buildingType.getName());
    ACLMessage aclMessage =
        worldAgent.newMessage(
            requesterUnit,
            ACLMessage.REFUSE,
            Protocol.CREATE_BUILDING,
            new Action(requesterUnit, createBuilding));
    if (agWorldService.isGameFinished()) {
      logger.log("Game finished.. Sending REFUSE to Town Hall building request...");
      worldAgent.send(aclMessage);
      return;
    }
    aclMessage.setPerformative(ACLMessage.INFORM);
    logger.log("Sending INFO after elapsed time of Town Hall building request");
    worldAgent.send(aclMessage);
  }
}
