package es.upm.woa.group3.agent;

public enum AgentType {
  TRIBE("TRIBE"),
  WORLD( "WORLD"),
  UNIT("UNIT"),
  PLATFORM("PLATFORM"),
  REGISTRATION_DESK("REGISTRATION DESK");

  AgentType(String agentType) {
    this.agentType = agentType;
  }

  private String agentType;

  public String getAgentType() {
    return agentType;
  }
}
