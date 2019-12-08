package es.upm.woa.group3.agent.agtribe;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.NotifyNewUnit;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ListenToNewUnitInfoBehaviour extends CyclicBehaviour {
  private BaseAgent tribeAgent;
  private Logger logger;

  public ListenToNewUnitInfoBehaviour(BaseAgent unitAgent) {
    this.tribeAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = tribeAgent.receiveMessage(Protocol.NOTIFY_NEW_UNIT);
    if (Objects.isNull(msg)) {
      block();
      return;
    }
    try {
      NotifyNewUnit newUnitInfo =
          Optional.of(msg)
              .filter(m -> m.getPerformative() == ACLMessage.INFORM)
              .map(m -> tribeAgent.extractContent(m))
              .filter(Action.class::isInstance)
              .map(Action.class::cast)
              .map(Action::getAction)
              .filter(NotifyNewUnit.class::isInstance)
              .map(NotifyNewUnit.class::cast)
              .orElseThrow(() -> new AgentException("Cannot process message"));
      logger.log(
          String.format(
              "Received inform request from %s: NEW UNIT HAS BEEN CREATED: %dx%d %s",
              msg.getSender().getLocalName(),
              newUnitInfo.getLocation().getX(),
              newUnitInfo.getLocation().getY(),
              newUnitInfo.getNewUnit().getName()));

    } catch (AgentException ex) {
      ex.printStackTrace();
    }
  }
}
