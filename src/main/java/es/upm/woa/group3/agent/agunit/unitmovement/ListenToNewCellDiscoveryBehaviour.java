package es.upm.woa.group3.agent.agunit.unitmovement;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.NotifyCellDetail;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.requireNonNull;

public class ListenToNewCellDiscoveryBehaviour extends CyclicBehaviour {
  private BaseAgent unitAgent;
  private Logger logger;

  public ListenToNewCellDiscoveryBehaviour(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  @Override
  public void action() {
    ACLMessage msg = unitAgent.receiveMessage(Protocol.NOTIFY_NEW_CELL_DISCOVERY);

    if (msg != null) {
      try {
        if (isNotInformMessage(msg)) return;
        ContentElement ce = unitAgent.getContentManager().extractContent(msg);
        if (!(ce instanceof Action)) {
          logger.log("Received message content is not an action");
          return;
        }

        Action agAction = (Action) ce;
        Concept conc = agAction.getAction();

        if (!(conc instanceof NotifyCellDetail)) {
          logger.log("Received message content is not a NotifyCellDetail action");
          return;
        }
        NotifyCellDetail newUnitInfo = (NotifyCellDetail) conc;
        Cell newCell = newUnitInfo.getNewCell();
        logger.log(
            String.format(
                "NotifyNewCellDiscovery: Discovered new cell at position: [%d x %d]",
                newCell.getX(), newCell.getY()));

      } catch (Codec.CodecException | OntologyException e) {
        logger.log("Cannot read message " + e.getMessage());
      }
    } else {
      block();
    }
  }

  private boolean isNotInformMessage(ACLMessage msg) {
    if (msg.getPerformative() != ACLMessage.INFORM) {
      logger.log("Received message of unexpected type: " + msg.getPerformative());
      return true;
    }
    return false;
  }
}
