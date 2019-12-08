package es.upm.woa.group3.agent.agregistrationdesk;

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

public class ListenToRegisterNewTribeRegistrationBehaviour extends CyclicBehaviour {
  private BaseAgent registrationAgent;
  private Logger logger;

  public ListenToRegisterNewTribeRegistrationBehaviour(BaseAgent unitAgent) {
    this.registrationAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = registrationAgent.receiveMessage(Protocol.REGISTER_TRIBE);

    if (isNull(msg)) {
      block();
      return;
    }
    handleMessage(msg);
  }

  private void handleMessage(ACLMessage msg) {
    try {
      RegisterTribe registerTribe =
          registrationAgent.validateMessage(
              msg.getPerformative(),
              ACLMessage.REQUEST,
              registrationAgent.extractContent(msg),
              RegisterTribe.class);

      logger.log(String.format("Tribe no. [%d] wants to register", registerTribe.getTeamNumber()));

      Action actionFromMessage = getActionFromMessage(msg);

      ACLMessage response =
          registrationAgent.responseFromMessage(
              msg, ACLMessage.AGREE, Protocol.REGISTER_TRIBE, actionFromMessage);

      logger.log("Sending agree to tribe registarion for tribe: " + registerTribe.getTeamNumber());
      registrationAgent.send(response);
    } catch (AgentException ex) {
      logger.log("Error during registration: " + ex.getMessage());
      registrationAgent.sendRefuseResponseOnError(
          msg, getActionFromMessage(msg), Protocol.REGISTER_TRIBE);
    }
  }

  private Action getActionFromMessage(ACLMessage msg) {
    return Optional.of(msg)
        .filter(m -> m.getPerformative() == ACLMessage.REQUEST)
        .map(m -> registrationAgent.extractContent(m))
        .filter(Action.class::isInstance)
        .map(Action.class::cast)
        .orElseThrow(() -> new AgentException("Cannot get action from tribe"));
  }
}
