package xyz.johansson.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;

public class Common {

    public static ACLMessage getACLMessage(int performative, AID receiver, String ontology, Serializable contentObj) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setLanguage("English");
        msg.setOntology(ontology);
        try {
            msg.setContentObject(contentObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static AID getService(Agent agent, String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            if (result.length > 0)
                return result[0].getName();
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }

    public static void registerServices(Agent agent, String[] services) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        for (int i = 0; i < services.length; i++) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(services[i]);
            sd.setName(agent.getLocalName());
            dfd.addServices(sd);
        }

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
