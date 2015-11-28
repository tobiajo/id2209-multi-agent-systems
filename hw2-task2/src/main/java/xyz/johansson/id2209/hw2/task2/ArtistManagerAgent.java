package xyz.johansson.id2209.hw2.task2;

import jade.core.Agent;
import xyz.johansson.id2209.hw2.task2.FIPADutchAuction.InitiatorBehaviour;

public class ArtistManagerAgent extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new InitiatorBehaviour(this, "\"good description\""));
    }
}