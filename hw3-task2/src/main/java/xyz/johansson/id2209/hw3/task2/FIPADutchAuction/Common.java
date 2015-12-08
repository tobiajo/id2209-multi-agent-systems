package xyz.johansson.id2209.hw3.task2.FIPADutchAuction;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.StringJoiner;

class Common {

    static ACLMessage getACLMessage(int performative, AID[] receivers, String content) {
        ACLMessage msg = new ACLMessage(performative);
        for (AID aid : receivers) msg.addReceiver(aid);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
        msg.setContent(content);
        return msg;
    }

    static String getLocalNames(AID[] aids) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (AID aid : aids) sj.add(aid.getLocalName());
        return sj.toString();
    }

    static AID[] getService(Agent agent, String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            if (result.length > 0) {
                AID[] resultAID = new AID[result.length];
                for (int i = 0; i < result.length; i++) resultAID[i] = result[i].getName();
                return resultAID;
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return new AID[]{};
    }


    static int evaluate(String good) {
        return good.length() * 1000;
    }

    static void print(Agent agent, ACLMessage msg) {
        print(agent, "msg: " + msg.getSender().getLocalName() + " | " + ACLMessage.getPerformative(msg.getPerformative()) + " | " + msg.getContent());
    }

    static void print(Agent agent, String s) {
        System.out.println("(" + agent.getLocalName() + ")\t" + s);
    }

    static void registerServices(Agent agent, String[] services) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        for (int i = 0; i < services.length; i++) {
            ServiceDescription sd = new ServiceDescription();
            sd.setName(agent.getLocalName());
            sd.setType(services[i]);
            dfd.addServices(sd);
        }
        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
