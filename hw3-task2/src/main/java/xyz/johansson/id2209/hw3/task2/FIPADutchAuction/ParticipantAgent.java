package xyz.johansson.id2209.hw3.task2.FIPADutchAuction;

import jade.core.Agent;

public abstract class ParticipantAgent extends Agent {

    private int winningBid = -1;
    private String good;

    public int getWinningBid() {
        return winningBid;
    }

    protected void setWinningBid(int winningBid) {
        this.winningBid = winningBid;
    }

    public String getGood() {
        return good;
    }

    protected void setGood(String good) {
        this.good = good;
    }
}
