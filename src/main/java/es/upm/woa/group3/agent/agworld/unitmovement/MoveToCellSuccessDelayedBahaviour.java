package es.upm.woa.group3.agent.agworld.unitmovement;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agworld.AgWorldService;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.model.Unit;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.MoveToCell;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MoveToCellSuccessDelayedBahaviour extends WakerBehaviour {
  private BaseAgent worldAgent;
  private Unit requesterUnit;
  private Logger logger;
  private AgWorldService agWorldService;

  private MoveToCellSuccessDelayedBahaviour(Agent a, long tickTimeout) {
    super(a, tickTimeout);
    BaseAgent baseAgent =
        Optional.of(a)
            .filter(BaseAgent.class::isInstance)
            .map(BaseAgent.class::cast)
            .orElseThrow(() -> new AgentException("Agent is not of type BaseAgent"));
    this.worldAgent = requireNonNull(baseAgent);
    this.logger = Logger.getLogger(worldAgent.getClass().getSimpleName());
  }

  MoveToCellSuccessDelayedBahaviour(
      Agent a, Unit requesterUnit, AgWorldService agWorldService, long tickTimeout) {
    this(a, tickTimeout);
    this.requesterUnit = requireNonNull(requesterUnit);
    this.agWorldService = requireNonNull(agWorldService);
  }

  @Override
  public void handleElapsedTimeout() {
    ACLMessage aclMessage =
        worldAgent.newMessage(
            requesterUnit.getAid(),
            ACLMessage.REFUSE,
            Protocol.MOVE_TO_CELL,
            new Action(requesterUnit.getAid(), new MoveToCell()));
    if (agWorldService.isGameFinished()) {
      logger.log("Game finished.. Sending REFUSE to unit movement request...");
      worldAgent.send(aclMessage);
      return;
    }

    aclMessage.setPerformative(ACLMessage.INFORM);
    logger.log("Sending INFO after elapsed time of unit movement");
    worldAgent.send(aclMessage);
  }
}
