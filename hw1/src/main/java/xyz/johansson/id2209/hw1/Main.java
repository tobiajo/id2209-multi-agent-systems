package xyz.johansson.id2209.hw1;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.net.UnknownHostException;
import java.util.Hashtable;

public class Main {

    private static final String PKG = "xyz.johansson.id2209.hw1.";
    private static Hashtable<Integer, Artifact> artifactDB;

    public static void main(String[] args) throws StaleProxyException {

        // Init "database"
        artifactDB = new Hashtable();
        for (int i = 0; i < 5; i++) artifactDB.put(i, new Artifact(i));

        // Get JADE runtime
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);

        // Create main container with GUI
        rt.createMainContainer(new ProfileImpl()).createNewAgent("rma", "jade.tools.rma.rma", new Object[] {}).start();

        // Create agent containers
        AgentContainer c1 = rt.createAgentContainer(new ProfileImpl());
        c1.createNewAgent("Curator", PKG + "CuratorAgent", new Object[] {}).start();
        c1.createNewAgent("TourGuide", PKG + "TourGuideAgent", new Object[] {}).start();

        AgentContainer c2 = rt.createAgentContainer(new ProfileImpl());
        for (int i = 0; i < 1; i++) {
            c2.createNewAgent("Profiler" + i, PKG + "ProfilerAgent", new Object[] {new User()}).start();
        }
    }

    public static Hashtable<Integer, Artifact> getArtifactDB() {
        return artifactDB;
    }
}
