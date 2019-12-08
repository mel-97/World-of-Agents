package es.upm.woa.group3.agent.agunit.unitmovement;

import es.upm.woa.group3.agent.AgentType;
import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.NotifyUnitPosition;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import es.upm.woa.ontology.Cell;

import static java.util.Objects.requireNonNull;

public class InformTribeAboutUnitPosition extends OneShotBehaviour {

    private BaseAgent unitAgent;
    private Logger logger;

    public InformTribeAboutUnitPosition (BaseAgent unitAgent) {
        this.unitAgent = requireNonNull(unitAgent);
        this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
    }

    @Override
    public void action() {

        try {
            DFAgentDescription tribeAgent = unitAgent.discoverAgent(myAgent, AgentType.WORLD.name());

            Cell unitPosition = new Cell(); //Todo; extract cell from AgUnit
            unitPosition.setX(1); // test position
            unitPosition.setY(1);

            NotifyUnitPosition notifyUnitPosition = new NotifyUnitPosition();
            notifyUnitPosition.setCell(unitPosition); //Todo: set AgTribe

            Action notifyUnitPositionAction = new Action(unitAgent.getAID(), notifyUnitPosition);
            ACLMessage message =
                    unitAgent.newMessage(
                            myAgent.getAID(), // Todo: extract tribe and send to tribe
                            ACLMessage.REQUEST,
                            Protocol.NOTIFY_UNIT_POSITION,
                            notifyUnitPositionAction);
            logger.log("Sending message to notify tribe about new unit position");

            unitAgent.send(message);
        } catch (AgentException e) {
            logger.log("Cannot send new notification of new unit position... " + e.getMessage());
        }
    }

}
