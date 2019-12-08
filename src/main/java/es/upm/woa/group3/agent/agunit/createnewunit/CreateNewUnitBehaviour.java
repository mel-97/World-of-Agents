package es.upm.woa.group3.agent.agunit.createnewunit;

import es.upm.woa.group3.agent.AgentType;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.CreateUnit;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.requireNonNull;

/** Create new unit behaviour On launch tries to create new unit */
public class CreateNewUnitBehaviour extends OneShotBehaviour {
  private BaseAgent unitAgent;
  private Logger logger;

  public CreateNewUnitBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    try {
      logger.log("Sending message to create new Unit");
      DFAgentDescription worldAgent = unitAgent.discoverAgent(unitAgent, AgentType.WORLD.name());
      ACLMessage message =
          unitAgent.newMessage(
              worldAgent.getName(),
              ACLMessage.REQUEST,
              Protocol.CREATE_UNIT,
              new Action(unitAgent.getAID(), new CreateUnit()));

      unitAgent.send(message);
    } catch (AgentException ex) {
      logger.log("Cannot send CreateNewUnit message: " + ex.getMessage());
    }
  }
}
