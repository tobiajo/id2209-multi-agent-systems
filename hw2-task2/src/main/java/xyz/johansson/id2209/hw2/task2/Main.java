package xyz.johansson.id2209.hw2.task2;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Main {

    public static final int CURATORS = 4;
    private static final String PKG = "xyz.johansson.id2209.hw2.task2.";

    public static void main(String[] args) throws StaleProxyException {

        // Get JADE runtime
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);

        // Create main container with GUI
        rt.createMainContainer(new ProfileImpl()).createNewAgent("rma", "jade.tools.rma.rma", new Object[]{}).start();

        // Create agent containers
        AgentContainer c1 = rt.createAgentContainer(new ProfileImpl());
        AgentContainer c2 = rt.createAgentContainer(new ProfileImpl());
        c1.createNewAgent("ArtistManager", PKG + "ArtistManagerAgent", new Object[]{}).start();
        for (int i = 0; i < CURATORS; i++)
            c2.createNewAgent("Curator" + i, PKG + "CuratorAgent", new Object[]{}).start();
    }
}