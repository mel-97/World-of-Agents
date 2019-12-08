package es.upm.woa.group3.agent.agunit.strategy.unit0;

import es.upm.woa.group3.agent.BaseAgent;
import es.upm.woa.group3.agent.agunit.townhallcreation.CreateNewBuildingBehaviour;
import es.upm.woa.group3.agent.agunit.unitmovement.MoveToCellBehaviour;
import es.upm.woa.group3.util.Logger;

import static java.util.Objects.requireNonNull;

public class Unit0Strategy {

  private BaseAgent unitAgent;
  private Logger logger;

  public Unit0Strategy(BaseAgent unitAgent) {
    this.unitAgent = requireNonNull(unitAgent);
    this.logger = Logger.getLogger(unitAgent.getClass().getSimpleName());
  }

  void start() {
    // unit 0 wants to create new townhall

    CreateNewBuildingBehaviour createBehaviour = new CreateNewBuildingBehaviour(unitAgent);
    unitAgent.addBehaviour(createBehaviour);


//    unitAgent.addBehaviour(new MoveToCellBehaviour(unitAgent));
  }
}
