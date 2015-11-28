package xyz.johansson.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.HashSet;
import java.util.Iterator;

public class TourGuideAgent extends Agent {

    private HashSet<String> genres;

    @Override
    protected void setup() {
        genres = new HashSet();
        Common.registerServices(this, new String[] {"Personalized-tour"});
        addBehaviour(new ParallelEverythingBehaviour(this));
        print("is ready!");
    }

    private void print(String s) {
        System.out.println("[" + getLocalName() + "]\t" + s);
    }

    /**
     * ParallelEverythingBehaviour
     */
    private static class ParallelEverythingBehaviour extends ParallelBehaviour {

        ParallelEverythingBehaviour(TourGuideAgent agent) {
            addSubBehaviour(new ServerBehaviour(agent, this));
        }
    } // end ParallelEverythingBehaviour

    /**
     * ServerBehaviour
     */
    private static class ServerBehaviour extends CyclicBehaviour {

        TourGuideAgent agent;
        ParallelEverythingBehaviour parallel;

        ServerBehaviour(TourGuideAgent agent, ParallelEverythingBehaviour parallel) {
            this.agent = agent;
            this.parallel = parallel;
        }

        @Override
        public void action() {
            ACLMessage msg = agent.receive();
            if (msg != null) {
                agent.print("received:\t" + msg.getOntology() + "\t" + msg.getPerformative() + "\tSender:\t\t" + msg.getSender().getLocalName());
                parallel.addSubBehaviour(new HandleMessageBehaviour(agent, msg)); // spawn as a parallel sub behaviour
            }
        }

        static class HandleMessageBehaviour extends OneShotBehaviour {

            TourGuideAgent agent;
            ACLMessage msg;

            HandleMessageBehaviour(TourGuideAgent agent, ACLMessage msg) {
                this.agent = agent;
                this.msg = msg;
            }

            @Override
            public void action() {
                switch (msg.getOntology()) {
                    case "Artifact-catalogue-ontology":
                        try {
                            agent.genres = (HashSet) msg.getContentObject();
                            System.out.println();
                            agent.print("genres updated: " + agent.genres.toString());
                            System.out.println();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Personalized-tour-ontology":
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            try {
                                HashSet<String> matches = getMatches((User) msg.getContentObject());
                                ACLMessage request = Common.getACLMessage(ACLMessage.REQUEST, new AID("Curator", AID.ISLOCALNAME), "Personalized-tour-ontology", matches);
                                request.addReplyTo(msg.getSender());
                                agent.send(request);
                                agent.print("sent:\t\t" + request.getOntology() + "\t" + request.getPerformative() + "\tContent:\t" + request.getContentObject());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        } else if (msg.getPerformative() == ACLMessage.INFORM) {
                            try {
                                ACLMessage inform = Common.getACLMessage(ACLMessage.INFORM, (AID) msg.getAllReplyTo().next(), "Personalized-tour-ontology", msg.getContentObject());
                                agent.send(inform);
                                agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            HashSet<String> getMatches(User user) {
                HashSet<String> matches = new HashSet();
                Iterator it = user.getInterests().iterator();
                while (it.hasNext()) {
                    String interest = (String) it.next();
                    if (agent.genres.contains(interest)) matches.add(interest);
                }
                return matches;
            }
        }
    } // end ServerBehaviour
}