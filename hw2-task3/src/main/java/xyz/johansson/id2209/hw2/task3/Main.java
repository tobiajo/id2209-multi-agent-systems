package xyz.johansson.id2209.hw2.task3;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Main {

    public static final int CURATORS = 4;
    private static final String PKG = "xyz.johansson.id2209.hw2.task3.";

    public static void main(String[] args) throws StaleProxyException {

        // Get JADE runtime
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);

        // Create main container with GUI
        rt.createMainContainer(new ProfileImpl()).createNewAgent("rma", "jade.tools.rma.rma", new Object[]{}).start();

        // Create agent containers
        AgentContainer c1 = rt.createAgentContainer(new ProfileImpl());
        AgentContainer c2 = rt.createAgentContainer(new ProfileImpl());
    }
}