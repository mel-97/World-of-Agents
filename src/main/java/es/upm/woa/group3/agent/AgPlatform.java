package es.upm.woa.group3.agent;

import es.upm.woa.group3.util.Logger;

public class AgPlatform extends BaseAgent {
  private Logger logger = Logger.getLogger(this.getClass().getSimpleName());

  @Override
  protected void setup() {
    logger.log("Starting AgPlatform");
    super.setup(AgentType.PLATFORM);
    startAgentRegistrationDesk();
    startAgentWorld();
  }

  @Override
  protected void registerBehaviours() {}

  private void startAgentWorld() {
    startNewAgent(getContainerController(), AgentType.WORLD.name(), new AgWorld())
        .orElseThrow(() -> new RuntimeException("Error during launching new AgWorld"));
  }

  private void startAgentRegistrationDesk() {
    logger.log("starting registration desk agent");
    startNewAgent(
            getContainerController(), AgentType.REGISTRATION_DESK.name(), new AgRegistrationDesk())
        .orElseThrow(() -> new RuntimeException("Error during launching new AgRegistrationDesk"));
  }
}
