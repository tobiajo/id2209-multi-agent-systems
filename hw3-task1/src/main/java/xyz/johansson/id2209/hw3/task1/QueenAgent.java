package xyz.johansson.id2209.hw3.task1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

public class QueenAgent extends Agent {

    private int id;
    private int n;

    protected void setup() {
        Object[] args = getArguments();
        id = (int) args[0];
        n = (int) args[1];
        addBehaviour(new QueenBehaviour());
    }

    private class QueenBehaviour extends SimpleBehaviour {
        AID prev;
        AID next;
        int solutions;
        boolean done;
        boolean board[][];

        @Override
        public void onStart() {
            System.out.println(getLocalName() + ": started");
            prev = new AID("Queen" + (id - 1), AID.ISLOCALNAME);
            next = new AID("Queen" + (id + 1), AID.ISLOCALNAME);
            solutions = 0; // only used by the highest queen
            done = false;
        }

        @Override
        public void action() {
            if (id == 0) {
                board = new boolean[n][n];
            } else {
                ACLMessage msg = blockingReceive(); // receive from prev
                switch (msg.getPerformative()) {
                    case ACLMessage.INFORM:
                        try {
                            board = (boolean[][]) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ACLMessage.CANCEL:
                        if (id != n - 1) {
                            send(getACLMessage(ACLMessage.CANCEL, next, null)); // forward
                        }
                        done = true;
                        return;
                }
            }

            for (int i = 0; i <= n - 1; i++) {
                if (possible(board, id, i)) {
                    board[id][i] = true;
                    if (id == n - 1) {
                        printSolution(board, ++solutions);
                        break;
                    }
                    send(getACLMessage(ACLMessage.INFORM, next, board));
                    blockingReceive();
                    board[id][i] = false;
                }
            }

            if (id == 0) {
                System.out.println("\nAll solutions to " + n + "-queens is listed above\n");
                send(getACLMessage(ACLMessage.CANCEL, next, null));
                done = true;
                return;
            } else {
                send(getACLMessage(ACLMessage.INFORM, prev, null)); // inform prev that possible positions is tested
            }
        }

        @Override
        public boolean done() {
            return done;
        }

        @Override
        public int onEnd() {
            System.out.println(getLocalName() + ": ended");
            if (id == n - 1) {
                Main.closeVM();
            }
            return super.onEnd();
        }
    }

    private ACLMessage getACLMessage(int perf, AID receiver, boolean[][] board) {
        ACLMessage msg = new ACLMessage(perf);
        msg.addReceiver(receiver);
        try {
            msg.setContentObject(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private boolean possible(boolean board[][], int row, int col) {
        int i, j;
        // check ↗
        for (i = row + col, j = 0; j <= n - 1; i--, j++) {
            if (i >= 0 && i <= n - 1) {
                if (board[i][j] == true) {
                    return false;
                }
            }
        }
        // check →
        for (j = 0; j <= n - 1; j++) {
            if (board[row][j] == true) {
                return false;
            }
        }
        // check ↘
        for (i = row - col, j = 0; j <= n - 1; i++, j++) {
            if (i >= 0 && i <= n - 1) {
                if (board[i][j] == true) {
                    return false;
                }
            }
        }
        // check ↓
        for (i = 0; i <= n - 1; i++) {
            if (board[i][col] == true) {
                return false;
            }
        }
        return true;
    }

    private void printSolution(boolean[][] board, int count) {
        System.out.println("\nSolution " + count + ":");
        for (int i = 0; i <= n - 1; i++) {
            for (int j = 0; j <= n - 1; j++) {
                System.out.print(board[i][j] ? "Q " : "□ ");
            }
            System.out.println();
        }
    }
}
