package xyz.johansson.id2209.hw3.task2;

import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import xyz.johansson.id2209.hw3.task2.FIPADutchAuction.ParticipantAgent;
import xyz.johansson.id2209.hw3.task2.FIPADutchAuction.ParticipantBehaviour;
import xyz.johansson.id2209.hw3.task2.FIPADutchAuction.Strategy;

public class CuratorAgent extends ParticipantAgent {

    private AID originalProfilerAID;
    private AID originalAID;
    private Location originalLocation;

    @Override
    protected void setup() {
        originalProfilerAID = (AID) getArguments()[0];
        String[] museums = (String[]) getArguments()[1];
        originalAID = getAID();
        originalLocation = here();
        SequentialBehaviour sb = new SequentialBehaviour();
        for (int i = 0; i < museums.length; i++) {
            sb.addSubBehaviour(new CloneOriginalBehaviour(this, originalAID, museums[i], false));
        }
        addBehaviour(sb);
    }

    @Override
    protected void afterClone() {
        SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new ParticipantBehaviour(this, Strategy.getRandom(), here().getName()));
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
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(originalProfilerAID);
        inform.setContent(Integer.toString(getWinningBid()));
        send(inform);
    }
}
