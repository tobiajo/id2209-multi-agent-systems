package xyz.johansson.id2209.hw1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class CuratorAgent extends Agent {

    public static final long UPDATE_PERIOD = 60000;
    private Hashtable<Integer, Artifact> artifactTable;

    @Override
    protected void setup() {
        artifactTable = new Hashtable();
        Common.registerServices(this, new String[] {"Artifact-details"});
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

        ParallelEverythingBehaviour(CuratorAgent agent) {
            addSubBehaviour(new RepeatedUpdateBehaviour(agent, UPDATE_PERIOD));
            addSubBehaviour(new ServerBehaviour(agent, this));
        }
    } // end ParallelEverythingBehaviour

    /**
     * RepeatedUpdatedBehaviour
     */
    private static class RepeatedUpdateBehaviour extends TickerBehaviour {

        CuratorAgent agent;

        RepeatedUpdateBehaviour(CuratorAgent agent, long period) {
            super(agent, period);
            this.agent = agent;
            agent.addBehaviour(new UpdateBehaviour(agent));
        }

        @Override
        protected void onTick() {
            agent.addBehaviour(new UpdateBehaviour(agent));
        }
    } // end RepeatedUpdatedBehaviour

    /***
     * UpdateBehaviour
     */
    private static class UpdateBehaviour extends OneShotBehaviour {

        CuratorAgent agent;

        UpdateBehaviour(CuratorAgent agent) {
            this.agent = agent;
        }

        @Override
        public void action() {
            // update available artifacts
            agent.artifactTable = Main.getArtifactDB();
            System.out.println();
            agent.print("artifacts updated: " + agent.artifactTable.toString());
            System.out.println();
            // send updated genres to tour guide
            try {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(new AID("TourGuide", AID.ISLOCALNAME));
                inform.setLanguage("English");
                inform.setOntology("Artifact-catalogue-ontology");
                inform.setContentObject(getGenres());
                agent.send(inform);
                agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
            } catch (IOException | UnreadableException e) {
                e.printStackTrace();
            }
        }

        HashSet<String> getGenres() {
            HashSet<String> genres = new HashSet();
            Iterator it = agent.artifactTable.values().iterator();
            while (it.hasNext()) {
                genres.add(((Artifact) it.next()).getGenre());
            }
            return genres;
        }
    } // end UpdateBehaviour

    /**
     * ServerBehaviour
     */
    private static class ServerBehaviour extends CyclicBehaviour {

        CuratorAgent agent;
        ParallelEverythingBehaviour parallel;

        ServerBehaviour(CuratorAgent agent, ParallelEverythingBehaviour parallel) {
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

            CuratorAgent agent;
            ACLMessage msg;

            HandleMessageBehaviour(CuratorAgent agent, ACLMessage msg) {
                this.agent = agent;
                this.msg = msg;
            }

            @Override
            public void action() {
                switch (msg.getOntology()) {
                    case "Personalized-tour-ontology":
                        try {
                            HashSet<Integer> personalizedTour = getPersonalizedTour((HashSet<String>) msg.getContentObject());
                            ACLMessage inform = Common.getACLMessage(ACLMessage.INFORM, new AID("TourGuide", AID.ISLOCALNAME), "Personalized-tour-ontology", personalizedTour);
                            inform.addReplyTo((AID) msg.getAllReplyTo().next());
                            agent.send(inform);
                            agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Artifact-details-ontology":
                        try {
                            HashSet<Artifact> artifactDetails = getArtifactDetails((HashSet<Integer>) msg.getContentObject());
                            ACLMessage inform = Common.getACLMessage(ACLMessage.INFORM, msg.getSender(), "Artifact-details-ontology", artifactDetails);
                            agent.send(inform);
                            agent.print("sent:\t\t" + inform.getOntology() + "\t" + inform.getPerformative() + "\tContent:\t" + inform.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }

            HashSet<Integer> getPersonalizedTour(HashSet<String> matches) {
                HashSet<Integer> idSet = new HashSet();
                Iterator it = agent.artifactTable.values().iterator();
                while (it.hasNext()) {
                    Artifact artifact = (Artifact) it.next();
                    if (matches.contains(artifact.getGenre())) idSet.add(artifact.getId());
                }
                return idSet;
            }

            HashSet<Artifact> getArtifactDetails(HashSet<Integer> idSet) {
                HashSet<Artifact> artifactSet = new HashSet();
                Iterator it = idSet.iterator();
                while (it.hasNext()) {
                    Integer id = (Integer) it.next();
                    artifactSet.add(agent.artifactTable.get(id));
                }
                return artifactSet;
            }
        }
    } // end ServerBehaviour
}
