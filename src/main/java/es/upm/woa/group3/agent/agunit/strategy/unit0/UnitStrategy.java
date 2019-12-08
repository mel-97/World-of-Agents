package es.upm.woa.group3.agent.agunit.strategy.unit0;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agunit.strategy.rest.ExploringStrategy;
import es.upm.woa.group3.util.Logger;

import static java.util.Objects.requireNonNull;

public class UnitStrategy {

  private BaseAgent unitAgent;
  private Logger logger;

  public UnitStrategy(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  public void startStrategy() {
    logger.log(String.format("Starting strategy for unit: [%s]", unitAgent.getLocalName()));

    ExploringStrategy exploringStrategy = new ExploringStrategy(unitAgent);
    exploringStrategy.start();


//    if (unitAgent.getLocalName().endsWith("0")) {
//      Unit0Strategy unit0Strategy = new Unit0Strategy(unitAgent);
//      unit0Strategy.start();
//    } else {
//      ExploringStrategy exploringStrategy = new ExploringStrategy(unitAgent);
//      exploringStrategy.start();
//    }
  }
}
