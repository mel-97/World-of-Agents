package es.upm.woa.group3.agent;

import es.upm.woa.group3.error.AgentException;
import es.upm.woa.group3.model.Protocol;
import es.upm.woa.group3.util.Logger;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.GameOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public abstract class BaseAgent extends Agent {
  protected Codec codec = new SLCodec();
  protected Ontology ontology = GameOntology.getInstance();

  public Codec getCodec() {
    return codec;
  }

  public Ontology getOntology() {
    return ontology;
  }

  void setup(AgentType agentType) {
    super.setup();
    if (nonNull(agentType)) {
      this.registerIntoSD(this, getName(), getLocalName(), agentType.getAgentType());
    }
    getContentManager().registerLanguage(codec);
    getContentManager().registerOntology(ontology);
    registerBehaviours();
  }

  protected abstract void registerBehaviours();

  private void registerIntoSD(Agent agent, String name, String localName, String agentType) {
    try {
      Logger logger = Logger.getLogger(localName);
      // Creates its own description
      DFAgentDescription agentDescription = new DFAgentDescription();
      ServiceDescription serviceDescription = new ServiceDescription();
      serviceDescription.setName(name);
      serviceDescription.setType(agentType);
      agentDescription.addServices(serviceDescription);
      // Registers its description in the DF
      DFService.register(agent, agentDescription);
      logger.log(
          String.format(
              "Agent named: [%s] has been registered in the DF(Yellow Pages)", getLocalName()));

      System.out.println("AGENT NAME: "  + getLocalName());
    } catch (FIPAException e) {
      e.printStackTrace();
    }
  }

  private DFAgentDescription getAgentDescription(String type) {
    DFAgentDescription agentDescription = new DFAgentDescription();
    ServiceDescription serviceDescription = new ServiceDescription();
    // put destination agent here
    serviceDescription.setType(type);
    agentDescription.addServices(serviceDescription);
    return agentDescription;
  }

  public Optional<AID> startNewAgent(ContainerController cc, String name, Agent anAgent) {

    try {
      AgentController ac = cc.acceptNewAgent(name, anAgent);
      ac.start();
      return Optional.of(anAgent.getAID());
    } catch (StaleProxyException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public DFAgentDescription discoverAgent(Agent myAgent, String type) {
    try {
      DFAgentDescription description = getAgentDescription(type);
      List<DFAgentDescription> foundAgents = Arrays.asList(DFService.search(myAgent, description));

      if (foundAgents.isEmpty()) {
        throw new AgentException("Cannot find agent of type: " + type);
      }
      return foundAgents.get(0);
    } catch (FIPAException e) {
      throw new AgentException("Cannot find agent of type" + e.getMessage());
    }
  }

  public ACLMessage receiveMessage(Protocol protocol) {

    MessageTemplate templateWithProtocol = MessageTemplate.MatchProtocol(protocol.getName());
    MessageTemplate templateWithLanguage = MessageTemplate.MatchLanguage(codec.getName());
    MessageTemplate templateWithOntology = MessageTemplate.MatchOntology(ontology.getName());

    MessageTemplate finalMatch =
        MessageTemplate.and(
            templateWithProtocol, MessageTemplate.and(templateWithLanguage, templateWithOntology));

    return receive(finalMatch);
  }

  public ACLMessage newMessage(AID receiverAid, int messageType, Protocol protocol, Action action) {
    ACLMessage message = new ACLMessage(messageType);
    message.setOntology(this.ontology.getName());
    message.setLanguage(this.codec.getName());
    message.addReceiver(receiverAid);
    message.setProtocol(protocol.getName());
    try {
      getContentManager().fillContent(message, action);
    } catch (Codec.CodecException | OntologyException e) {
      throw new AgentException("Cannot create new message: " + e.getMessage());
    }
    return message;
  }

  public ContentElement extractContent(ACLMessage message) {
    try {
      return getContentManager().extractContent(message);
    } catch (Codec.CodecException | OntologyException e) {
      throw new AgentException("Cannot extract content: " + e.getMessage());
    }
  }

  public <C> C validateMessage(
      int actualMessageType, int expectedMessageType, ContentElement ce, Class<C> conceptClass) {
    if (actualMessageType != expectedMessageType) {
      throw new AgentException("Message is not of type ACLMessage.REQUEST");
    }

    C concept =
        Optional.ofNullable(ce)
            .filter(Action.class::isInstance)
            .map(Action.class::cast)
            .map(Action::getAction)
            .filter(conceptClass::isInstance)
            .map(conceptClass::cast)
            .orElseThrow(
                () ->
                    new AgentException(
                        String.format(
                            "Validation of message failed. Content element is not of type Action or concept is not of required type: %s",
                            conceptClass.getSimpleName())));
    return concept;
  }

  public ACLMessage responseFromMessage(ACLMessage message, Protocol protocol) {
    return responseFromMessage(message, ACLMessage.NOT_UNDERSTOOD, protocol);
  }

  public ACLMessage responseFromMessage(ACLMessage message, int messageType, Protocol protocol) {
    ACLMessage reply = message.createReply();
    reply.setOntology(ontology.getName());
    reply.setLanguage(codec.getName());
    reply.setProtocol(protocol.getName());
    reply.setPerformative(messageType);
    return reply;
  }

  public ACLMessage responseFromMessage(
      ACLMessage message, int messageType, Protocol protocol, Action action) {
    ACLMessage response = responseFromMessage(message, messageType, protocol);
    try {
      getContentManager().fillContent(response, action);
      return response;
    } catch (Codec.CodecException | OntologyException e) {
      throw new AgentException(
          String.format(
              "Cannot create response for message. Sender: [%s], protocol:[%s]",
              message.getSender(), message.getProtocol()));
    }
  }

  public <T> T getContentFromCell(Cell cell, Class<T> tClass) {
    return Optional.ofNullable(cell)
        .map(Cell::getContent)
        .filter(tClass::isInstance)
        .map(tClass::cast)
        .orElseThrow(() -> new RuntimeException("Error - Cell content not of a required type"));
  }

  public void fillContent(ACLMessage message, Action action) {
    try {
      getContentManager().fillContent(message, action);
    } catch (Codec.CodecException | OntologyException e) {
      message.setPerformative(ACLMessage.NOT_UNDERSTOOD);
      send(message);
      throw new AgentException(e);
    }
  }

  public void sendRefuseResponseOnError(ACLMessage message, Action action, Protocol protocol) {
    send(responseFromMessage(message, ACLMessage.REFUSE, protocol, action));
  }
}
