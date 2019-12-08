package es.upm.woa.group3.agent.agunit.strategy.rest;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agunit.unitmovement.MoveToCellBehaviour;
import es.upm.woa.group3.util.Logger;

import static java.util.Objects.requireNonNull;

public class ExploringStrategy {
  private BaseAgent unitAgent;
  private Logger logger;

  public ExploringStrategy(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  public void start() {
    unitAgent.addBehaviour(new MoveToCellBehaviour(unitAgent));
  }
}
