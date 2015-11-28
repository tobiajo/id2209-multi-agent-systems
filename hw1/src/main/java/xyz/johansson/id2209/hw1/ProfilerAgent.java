package xyz.johansson.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;
import jade.proto.states.MsgReceiver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringJoiner;

public class ProfilerAgent extends Agent {

    public static final long UPDATE_DELAY = 10000;
    private HashSet<Artifact> artifactSet;
    private User user;
    public AID tourGuide;

    @Override
    protected void setup() {
        artifactSet = new HashSet();
        user = (User) getArguments()[0];
        addSubscriptionBehaviour("Personalized-tour");
        addSubscriptionBehaviour("Artifact-details");
        addBehaviour(new DelayedUpdateBehaviour(this, UPDATE_DELAY));
        print("is ready!");
    }

    private void addSubscriptionBehaviour(String service) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(service);
        template.addServices(templateSd);
        ACLMessage templateMsg = DFService.createSubscriptionMessage(this, getDefaultDF(), template, null);
        addBehaviour(new SubscriptionInitiator(this, templateMsg) {
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    print("received\t" + inform.getOntology() + "\t\t" + inform.getPerformative() + "\tServices:\t" + getServices(dfds));

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private void printProvidedServices() {
        try {
            print("provided services: " + getServices((DFService.search(this, new DFAgentDescription()))));
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private String getServices(DFAgentDescription[] dfds) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (DFAgentDescription dfd : dfds) {
            String provider = dfd.getName().getLocalName();
            Iterator it = dfd.getAllServices();
            while (it.hasNext()) {
                ServiceDescription sd = (ServiceDescription) it.next();
                sj.add(provider + ": " + sd.getType());
            }
        }
        return sj.toString();
    }

    private void print(String s) {
        System.out.println("[" + getLocalName() + "]\t" + s);
    }

    /**
     * DelayedUpdateBehaviour
     */
    private static class DelayedUpdateBehaviour extends WakerBehaviour {

        ProfilerAgent agent;

        DelayedUpdateBehaviour(ProfilerAgent agent, long timeout) {
            super(agent, timeout);
            this.agent = agent;
        }

        @Override
        protected void onWake() {
            System.out.println();
            agent.printProvidedServices();
            System.out.println();
            agent.addBehaviour(new UpdateBehaviour(agent));
        }
    } // end DelayedUpdateBehaviour

    /**
     * UpdateBehaviour
     */
    private static class UpdateBehaviour extends FSMBehaviour {

        HashSet<Integer> idSet;

        UpdateBehaviour(ProfilerAgent agent) {
            idSet = new HashSet();
            registerFirstState(new S0Behaviour(agent, this), "S0");
            registerState(new S1Behaviour(agent, this), "S1");
            registerLastState(new OneShotBehaviour() {
                @Override
                public void action() {
                    System.out.println();
                    agent.print("interesting items: " + agent.artifactSet);
                    System.out.println();
                }
            }, "S2");
            registerTransition("S0", "S1", 0);
            registerTransition("S0", "S2", 1);
            registerDefaultTransition("S1", "S2");
        }

        static class S0Behaviour extends SequentialBehaviour {
            ProfilerAgent agent;
            UpdateBehaviour update;
            DataStore ds;

            S0Behaviour(ProfilerAgent agent, UpdateBehaviour update) {
                this.agent = agent;
                this.update = update;
                addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage request = Common.getACLMessage(ACLMessage.REQUEST, Common.getService(agent, "Personalized-tour"), "Personalized-tour-ontology", agent.user);
                        agent.send(request);
                        try {
                            agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent:\t" + request.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                });
                addSubBehaviour(new MsgReceiver(agent, MessageTemplate.MatchOntology("Personalized-tour-ontology"), MsgReceiver.INFINITE, ds = new DataStore(), 0));
            }

            @Override
            public int onEnd() {
                if (ds.get(0) == null) {
                    agent.print("received:\tnothing!");
                    return 1;
                }
                ACLMessage inform = (ACLMessage) ds.get(0);
                try {
                    agent.print("received:\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
                    update.idSet = (HashSet<Integer>) inform.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        } // end S0Behaviour

        static class S1Behaviour extends SequentialBehaviour {
            ProfilerAgent agent;
            UpdateBehaviour update;
            DataStore ds;

            S1Behaviour(ProfilerAgent agent, UpdateBehaviour update) {
                this.agent = agent;
                this.update = update;
                addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage request = Common.getACLMessage(ACLMessage.REQUEST, Common.getService(agent, "Artifact-details"), "Artifact-details-ontology", update.idSet);
                        agent.send(request);
                        try {
                            agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent:\t" + request.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                });
                addSubBehaviour(new MsgReceiver(agent, MessageTemplate.MatchOntology("Artifact-details-ontology"), MsgReceiver.INFINITE, ds = new DataStore(), 1));
            }

            @Override
            public int onEnd() {
                if (ds.get(1) == null) {
                    agent.print("received:\tnothing!");
                    return 1;
                }
                ACLMessage inform = (ACLMessage) ds.get(1);
                try {
                    agent.print("received:\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
                    agent.artifactSet = (HashSet<Artifact>) inform.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        } // end S1Behaviour
    } // end UpdateBehaviour
}