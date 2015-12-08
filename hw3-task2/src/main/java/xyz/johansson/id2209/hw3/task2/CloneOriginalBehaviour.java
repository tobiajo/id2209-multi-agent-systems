package xyz.johansson.id2209.hw3.task2;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.HashMap;
import jade.util.leap.Map;

class CloneOriginalBehaviour extends OneShotBehaviour {

    private Agent agent;
    private AID original;
    private String name;
    private boolean createContainer;

    CloneOriginalBehaviour(Agent agent, AID original, String name, boolean createContainer) {
        this.agent = agent;
        this.original = original;
        this.name = name;
        this.createContainer = createContainer;
    }

    @Override
    public void action() {
        if (agent.getAID() == original) {
            if (createContainer) {
                Profile p = new ProfileImpl();
                p.setParameter(Profile.CONTAINER_NAME, name);
                jade.core.Runtime.instance().createAgentContainer(p);
            }
            agent.doClone((Location) getLocations(agent).get(name), agent.getLocalName() + name);
        }
    }

    private Map getLocations(Agent agent) {
        Map locations = new HashMap();

        // Register language and ontology
        agent.getContentManager().registerLanguage(new SLCodec());
        agent.getContentManager().registerOntology(MobilityOntology.getInstance());

        // Get available locations with AMS
        sendRequest(agent, new Action(agent.getAMS(), new QueryPlatformLocationsAction()));

        //Receive response from AMS
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchSender(agent.getAMS()),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage resp = agent.blockingReceive(mt);
        ContentElement ce = null;
        try {
            ce = agent.getContentManager().extractContent(resp);
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
        Result result = (Result) ce;
        jade.util.leap.Iterator it = result.getItems().iterator();
        while (it.hasNext()) {
            Location loc = (Location) it.next();
            locations.put(loc.getName(), loc);
        }
        return locations;
    }

    private void sendRequest(Agent agent, Action action) {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setLanguage(new SLCodec().getName());
        request.setOntology(MobilityOntology.getInstance().getName());
        try {
            agent.getContentManager().fillContent(request, action);
            request.addReceiver(action.getActor());
            agent.send(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
