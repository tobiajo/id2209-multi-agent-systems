package xyz.johansson.id2209.hw3.task2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import xyz.johansson.id2209.hw3.task2.FIPADutchAuction.InitiatorBehaviour;

public class ProfilerAgent extends Agent {

    private static final int CURATORS = 2;

    private AID originalAID;
    private Location originalLocation;

    @Override
    protected void setup() {
        originalAID = getAID();
        originalLocation = here();
        String[] museums = (String[]) getArguments()[1];
        SequentialBehaviour sb = new SequentialBehaviour();
        for (int i = 0; i < museums.length; i++) {
            sb.addSubBehaviour(new CloneOriginalBehaviour(this, originalAID, museums[i], true));
        }
        sb.addSubBehaviour(new OneShotBehaviour() {
            // Start curators
            @Override
            public void action() {
                if (getAID() == originalAID) {
                    for (int i = 1; i <= CURATORS; i++) {
                        try {
                            ((AgentContainer) getArguments()[0]).createNewAgent("Curator" + i, Main.PKG + "CuratorAgent", new Object[]{originalAID, museums}).start();
                        } catch (StaleProxyException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        sb.addSubBehaviour(new SimpleBehaviour() {
            // Receive result from curator clones
            boolean done = false;
            int received = 0;
            int bestPrice = Integer.MAX_VALUE;

            @Override
            public void action() {
                if (getAID() == originalAID) {
                    ACLMessage inform = receive();
                    if (inform != null) {
                        int price = Integer.parseInt(inform.getContent());
                        if (price != -1 && price < bestPrice) {
                            bestPrice = price;
                        }
                        received++;
                        if (received == museums.length * CURATORS) {
                            done = true;
                        }
                    }
                } else {
                    done = true;
                }
            }

            @Override
            public boolean done() {
                return done;
            }

            @Override
            public int onEnd() {
                if (getAID() == originalAID) {
                    System.out.println("\n(" + getLocalName() + ")\tbest price: " + bestPrice);
                    System.exit(0);
                }
                return super.onEnd();
            }
        });
        addBehaviour(sb);
    }

    private Agent getAgent() {
        return this;
    }

    @Override
    protected void afterClone() {
        SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new InitiatorBehaviour(this, "(something expensive)", here().getName()));
        sb.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                doMove(originalLocation);
            }
        });
        addBehaviour(sb);
    }

    @Override
    protected void afterMove() {
    }
}
