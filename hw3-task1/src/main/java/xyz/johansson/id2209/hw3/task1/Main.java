package xyz.johansson.id2209.hw3.task1;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.Scanner;

public class Main {

    private static final String PKG = "xyz.johansson.id2209.hw3.task1.";
    private static AgentContainer container;

    public static void main(String[] args) {

        try {
            // Get n
            System.out.print("n = ");
            int n = Integer.parseInt(new Scanner(System.in).nextLine());
            if (n < 1) {
                throw new NumberFormatException();
            }

            // Create main container without GUI
            container = Runtime.instance().createMainContainer(new ProfileImpl());

            // Create queen agents
            for (int i = 0; i < n; i++) {
                try {
                    container.createNewAgent("Queen" + i, PKG + "QueenAgent", new Object[]{i, n}).start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("error: invalid input");
        }
    }

    public static void closeVM() {
        new Thread(() -> {
            try {
                Runtime.instance().setCloseVM(true);
                container.kill();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
