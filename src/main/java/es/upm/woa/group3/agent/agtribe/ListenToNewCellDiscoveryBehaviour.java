package es.upm.woa.group3.agent.agtribe;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.NotifyCellDetail;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ListenToNewCellDiscoveryBehaviour extends CyclicBehaviour {

  private BaseAgent tribeAgent;
  private Logger logger;

  public ListenToNewCellDiscoveryBehaviour(BaseAgent unitAgent) {
    this.tribeAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {

    ACLMessage msg = tribeAgent.receiveMessage(Protocol.NOTIFY_NEW_CELL_DISCOVERY);
    if (isNull(msg)) {
      block();
      return;
    }
    try {
      Optional.of(msg).filter(m -> m.getPerformative() == ACLMessage.INFORM);

      NotifyCellDetail cellDetail =
          Optional.of(msg)
              .filter(m -> m.getPerformative() == ACLMessage.INFORM)
              .map(m -> tribeAgent.extractContent(m))
              .filter(Action.class::isInstance)
              .map(Action.class::cast)
              .map(Action::getAction)
              .filter(NotifyCellDetail.class::isInstance)
              .map(NotifyCellDetail.class::cast)
              .orElseThrow(() -> new AgentException("Cannot process message"));
      Cell newCell = cellDetail.getNewCell();
      logger.log(
          String.format(
              "NotifyNewCellDiscovery: Discovered new cell at position: [%d x %d]",
              newCell.getX(), newCell.getY()));

    } catch (AgentException ex) {
      ex.printStackTrace();
    }
  }
}
