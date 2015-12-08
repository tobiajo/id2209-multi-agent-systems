package xyz.johansson.id2209.hw3.task2;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Main {

    public static final String PKG = "xyz.johansson.id2209.hw3.task2.";
    public static final String[] MUSEUMS = new String[]{"Galileo", "Heritage"};

    public static void main(String[] args) {

        // Create containers
        AgentContainer mainContainer = Runtime.instance().createMainContainer(new ProfileImpl());
        Profile p = new ProfileImpl();
        p.setParameter(Profile.CONTAINER_NAME, "Original");
        AgentContainer agentContainer = Runtime.instance().createAgentContainer(p);

        // Start agent
        try {
            agentContainer.createNewAgent("Profiler", PKG + "ProfilerAgent", new Object[]{agentContainer, MUSEUMS}).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
