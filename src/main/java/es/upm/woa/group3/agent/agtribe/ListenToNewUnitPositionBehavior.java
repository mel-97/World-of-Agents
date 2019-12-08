package es.upm.woa.group3.agent.agtribe;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;


public class ListenToNewUnitPositionBehavior extends CyclicBehaviour {
    private BaseAgent tribeAgent;
    private Logger logger;

    public ListenToNewUnitPositionBehavior(BaseAgent unitAgent){
        this.tribeAgent = requireNonNull(unitAgent);
        this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
    }

    @Override
    public void action() {
        ACLMessage msg = tribeAgent.receiveMessage(Protocol.NOTIFY_NEW_UNIT);
        if (isNull(msg)) {
            block();
            return;
        }

    }

}
