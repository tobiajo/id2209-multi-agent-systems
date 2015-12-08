package xyz.johansson.id2209.hw3.task2.FIPADutchAuction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class ParticipantBehaviour extends FSMBehaviour {

    private ParticipantAgent agent;
    private Strategy strategy;
    private String museum;
    private AID initiator;
    private int bid;

    public ParticipantBehaviour(ParticipantAgent agent, Strategy strategy, String museum) {
        this.agent = agent;
        this.strategy = strategy;
        this.museum = museum;
        // register states
        registerFirstState(new S0Behaviour(), "S0");
        registerState(new S1Behaviour(), "S1");
        registerLastState(new S2Behaviour(), "S2");
        // register transitions
        registerDefaultTransition("S0", "S1");
        registerDefaultTransition("S1", "S2");
    }

    @Override
    public int onEnd() {
        //Common.print(agent, "ParticipantBehaviour ended");
        return super.onEnd();
    }

    /**
     * Register auction interest
     */
    private class S0Behaviour extends OneShotBehaviour {

        @Override
        public void action() {
            Common.registerServices(agent, new String[]{"participant " + museum});
            Common.print(agent, "registered as participant, strategy: " + strategy.toString());
        }
    }

    /**
     * Receive good description
     */
    private class S1Behaviour extends SimpleBehaviour {

        boolean done = false;

        @Override
        public void action() {
            ACLMessage msg = agent.receive();
            if (msg != null) {
                Common.print(agent, msg);
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    initiator = msg.getSender();
                    agent.setGood(msg.getContent());
                    int value = Common.evaluate(agent.getGood());
                    switch (strategy) {
                        case LOW:
                            bid = (int) (value * 0.5);
                            break;
                        case MEDIUM:
                            bid = (int) (value * 0.75);
                            break;
                        default:
                            bid = (int) (value * 1.0);
                            break;
                    }
                    done = true;
                }
            }
        }

        @Override
        public boolean done() {
            return done;
        }
    }

    /**
     * Receive auction messages and act upon them
     */
    private class S2Behaviour extends SimpleBehaviour {

        boolean done = false;
        int cfp;

        @Override
        public void action() {
            ACLMessage msg = agent.receive();
            if (msg != null) {
                Common.print(agent, msg);
                switch (msg.getPerformative()) {
                    case ACLMessage.CFP:
                        cfp = Integer.parseInt(msg.getContent());
                        react(cfp);
                        break;
                    case ACLMessage.REJECT_PROPOSAL:
                        done = true;
                        break;
                    case ACLMessage.ACCEPT_PROPOSAL:
                        agent.setWinningBid(cfp);
                        done = true;
                        break;
                    case ACLMessage.INFORM:
                        done = true;
                        break;
                }
            }
        }

        @Override
        public boolean done() {
            return done;
        }

        void react(int price) {
            if (price <= bid) {
                agent.send(Common.getACLMessage(ACLMessage.PROPOSE, new AID[]{initiator}, Integer.toString(price)));
            }
        }
    }
}