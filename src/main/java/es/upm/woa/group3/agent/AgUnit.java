package es.upm.woa.group3.agent;

import es.upm.woa.group3.agent.agunit.createnewunit.ReceiveFinalResponseForUnitCreation;
import es.upm.woa.group3.agent.agunit.createnewunit.ReceiveResponseForNewUnitCreation;
import es.upm.woa.group3.agent.agunit.strategy.unit0.UnitStrategy;
import es.upm.woa.group3.agent.agunit.townhallcreation.ReceiveFinalResponseForNewBuildingCreationBehaviour;
import es.upm.woa.group3.agent.agunit.townhallcreation.ReceiveResponseForNewBuildingCreationBehaviour;
import es.upm.woa.group3.agent.agunit.unitmovement.ListenToNewCellDiscoveryBehaviour;
import es.upm.woa.group3.agent.agunit.unitmovement.ReceiveFinalResponseAboutMoveToCellBehaviour;
import es.upm.woa.group3.agent.agunit.unitmovement.ReceiveResponseForMoveToCellBehaviour;

public class AgUnit extends BaseAgent {

  @Override
  protected void setup() {
    setup(AgentType.UNIT);
  }

  @Override
  protected void registerBehaviours() {
    new UnitStrategy(this).startStrategy();
    addUnitCreationBehaviours();
    addUnitMovementBehaviours();
    addBuildingCreationBehaviours();
  }

  private void addUnitCreationBehaviours() {
    // call in strategy
    //    addBehaviour(new CreateNewUnitBehaviour(this));

    // start listers now
    addBehaviour(new ReceiveResponseForNewUnitCreation(this));
    addBehaviour(new ReceiveFinalResponseForUnitCreation(this));
  }

  private void addUnitMovementBehaviours() {

    // call in strategy
    //    addBehaviour(new MoveToCellBehaviour(this));

    // start listers now
    addBehaviour(new ReceiveResponseForMoveToCellBehaviour(this));
    addBehaviour(new ReceiveFinalResponseAboutMoveToCellBehaviour(this));
    addBehaviour(new ListenToNewCellDiscoveryBehaviour(this));
  }

  private void addBuildingCreationBehaviours() {
    // call in strategy
    //    addBehaviour(new CreateNewBuildingBehaviour(this));


    // start listers now
    addBehaviour(new ReceiveResponseForNewBuildingCreationBehaviour(this));
    addBehaviour(new ReceiveFinalResponseForNewBuildingCreationBehaviour(this));
  }
}
