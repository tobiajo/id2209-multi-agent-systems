package xyz.johansson.id2209.hw2.task2;

import jade.core.Agent;
import xyz.johansson.id2209.hw2.task2.FIPADutchAuction.ParticipantBehaviour;
import xyz.johansson.id2209.hw2.task2.FIPADutchAuction.Strategy;

public class CuratorAgent extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new ParticipantBehaviour(this, Strategy.getRandom()));
    }
}