package es.upm.woa.group3.agent.agtribe.registration;

import es.upm.woa.group3.agent.AgentType;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.RegisterTribe;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.requireNonNull;

public class RegisterTribeBehaviour extends OneShotBehaviour {
  public static final int TRIBE_NUMBER = 3;
  private BaseAgent tribeAgent;
  private Logger logger;

  public RegisterTribeBehaviour(BaseAgent unitAgent) {
    this.tribeAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    boolean isRegistered = false;
    while (!isRegistered) {
      try {
        logger.log("Starting Registering tribe behaviour");
        DFAgentDescription agRegistrationDesk =
            tribeAgent.discoverAgent(tribeAgent, AgentType.REGISTRATION_DESK.getAgentType());

        RegisterTribe registerTribe = new RegisterTribe();
        registerTribe.setTeamNumber(TRIBE_NUMBER);

        ACLMessage message =
            tribeAgent.newMessage(
                agRegistrationDesk.getName(),
                ACLMessage.REQUEST,
                Protocol.REGISTER_TRIBE,
                new Action(tribeAgent.getAID(), registerTribe));
        tribeAgent.send(message);
        isRegistered = true;
      } catch (AgentException ex) {
        logger.log("Cannot send RegisterTribe message: " + ex.getMessage());
        try {

          Thread.sleep(1000);
          logger.log("Retrying agent registration");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
