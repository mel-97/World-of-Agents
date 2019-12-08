package es.upm.woa.group3.agent;

import es.upm.woa.group3.agent.agregistrationdesk.ListenToRegisterNewTribeRegistrationBehaviour;
import es.upm.woa.group3.util.Logger;

public class AgRegistrationDesk extends BaseAgent {
  private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

  @Override
  protected void setup() {
    logger.log("Starting AgRegistrationDesk");
    super.setup(AgentType.REGISTRATION_DESK);
  }

  @Override
  protected void registerBehaviours() {
    addBehaviour(new ListenToRegisterNewTribeRegistrationBehaviour(this));
  }
}
