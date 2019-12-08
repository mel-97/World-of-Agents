package es.upm.woa.group3.agent;

import es.upm.woa.group3.agent.agtribe.ListenToNewBuildingCreationBehaviour;
import es.upm.woa.group3.agent.agtribe.ListenToNewCellDiscoveryBehaviour;
import es.upm.woa.group3.agent.agtribe.ListenToNewUnitInfoBehaviour;
import es.upm.woa.group3.agent.agtribe.registration.ReceiveResponseAboutRegistration;
import es.upm.woa.group3.agent.agtribe.registration.RegisterTribeBehaviour;
import es.upm.woa.group3.util.Logger;

public class AgTribe extends BaseAgent {
  private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

  @Override
  protected void setup() {
    logger.log("Starting AgTribe");
    super.setup(AgentType.TRIBE);
  }

  @Override
  protected void registerBehaviours() {
    this.addBehaviour(new RegisterTribeBehaviour(this));
    this.addBehaviour(new ReceiveResponseAboutRegistration(this));
    this.addBehaviour(new ListenToNewUnitInfoBehaviour(this));
    this.addBehaviour(new ListenToNewCellDiscoveryBehaviour(this));
    this.addBehaviour(new ListenToNewBuildingCreationBehaviour(this));
  }
}
