package xyz.johansson.id2209.hw2.task2.FIPADutchAuction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class InitiatorBehaviour extends FSMBehaviour {

    public static final int WAITING_TIME = 20000;
    public static final int DECREMENT_PERIOD = 1000;

    private Agent agent;
    private String good;
    private int price;
    private AID[] participants;

    public InitiatorBehaviour(Agent agent, String good) {
        this.agent = agent;
        this.good = good;
        price = Common.evaluate(good) * 3;
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
        //Common.print(agent, "InitiatorBehaviour ended");
        return super.onEnd();
    }

    /**
     * Wait participants
     */
    private class S0Behaviour extends WakerBehaviour {

        S0Behaviour() {
            super(agent, WAITING_TIME);
            Common.print(agent, "awaits participants to register (" + WAITING_TIME / 1000 + " s)");
        }

        @Override
        protected void onWake() {
            participants = Common.getService(agent, "participant");
            Common.print(agent, "participants: " + Common.getLocalNames(participants));
        }
    }

    /**
     * Send start-of-auction and the open cry price
     */
    private class S1Behaviour extends OneShotBehaviour {

        @Override
        public void action() {
            agent.send(Common.getACLMessage(ACLMessage.INFORM, participants, good));
            agent.send(Common.getACLMessage(ACLMessage.CFP, participants, Integer.toString(price)));
        }
    }

    /**
     * Decrement price and wait bids in parallel
     */
    private class S2Behaviour extends ParallelBehaviour {

        S2Behaviour() {
            super(WHEN_ANY); // terminates when any child is done
            addSubBehaviour(new TickerBehaviour(agent, DECREMENT_PERIOD) {

                @Override
                protected void onTick() {
                    price *= 0.9;
                    agent.send(Common.getACLMessage(ACLMessage.CFP, participants, Integer.toString(price)));
                }
            });
            addSubBehaviour(new SimpleBehaviour() {

                boolean done = false;

                @Override
                public void action() {
                    ACLMessage msg = agent.receive();
                    if (msg != null) {
                        if (msg.getPerformative() == ACLMessage.PROPOSE) {
                            if (Integer.parseInt(msg.getContent()) == price) {
                                Common.print(agent, msg);
                                agent.send(Common.getACLMessage(ACLMessage.ACCEPT_PROPOSAL, new AID[]{msg.getSender()}, "you won"));
                                agent.send(Common.getACLMessage(ACLMessage.INFORM, participants, "auction ended"));
                                done = true;
                            } else {
                                agent.send(Common.getACLMessage(ACLMessage.REJECT_PROPOSAL, new AID[]{msg.getSender()}, "invalid bid"));
                            }
                        }
                    }
                }

                @Override
                public boolean done() {
                    return done;
                }
            });
        }
    }
}