package es.upm.woa.group3.util;

import es.upm.woa.group3.model.Protocol;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class MessageUtils {

  private MessageUtils() {
    throw new AssertionError("Utility class");
  }

  @Deprecated
  public static ACLMessage newAclMessage(
      AID receiverAid, int messageType, Ontology ontology, Codec codec, Protocol protocol) {
    ACLMessage msg = new ACLMessage(messageType);
    msg.setOntology(ontology.getName());
    msg.setLanguage(codec.getName());
    msg.addReceiver(receiverAid);
    msg.setProtocol(protocol.getName());
    return msg;
  }

  @Deprecated
  public static ACLMessage responseFromMessage(
      ACLMessage message, Ontology ontology, Codec codec, int messageType) {
    ACLMessage reply = message.createReply();
    reply.setOntology(ontology.getName());
    reply.setLanguage(codec.getName());
    // should be member of ACLMessage, for example: NOT_UNDERSTOOD
    reply.setPerformative(messageType);
    return reply;
  }


}
