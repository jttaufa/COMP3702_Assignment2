package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.lang.Math;

import problem.Action;
import problem.Cycle;
import problem.GridCell;
import problem.Opponent;
import problem.Player;
import problem.RaceSim;
import problem.RaceSimTools;
import problem.Setup;
import problem.Tour;
import problem.Track;
import solver.Node;


public class MonteCarlo {
	Random random = new Random();
	int numIter;
	Node root;
	Tour tour;
	Setup setup;
	HashMap<Node,List<Node>> parentMap = new HashMap<Node,List<Node>>();
	
	/*List<Cycle> WildCycles = new ArrayList<Cycle>();
	List<Cycle> DomesticCycles = new ArrayList<Cycle>();
	List<Cycle> ReliableCycles = new ArrayList<Cycle>();
	List<Cycle> UnreliableCycles = new ArrayList<Cycle>();
	List<Cycle> FastCycles = new ArrayList<Cycle>();
	List<Cycle> MediumCycles = new ArrayList<Cycle>();
	List<Cycle> SlowCycles = new ArrayList<Cycle>();
	*/
	
	public MonteCarlo(Setup setup, Tour tour, int numIter){
		this.setup = setup;
		this.root = new Node("Tour", "tour", tour, null);
		this.tour = tour;
		this.numIter =numIter;  
				
		/*for (Cycle cycle: tour.getPurchasableCycles()){
			if (cycle.isWild()){
				WildCycles.add(cycle);
			} else {
				DomesticCycles.add(cycle);
			}
			
			if (cycle.isReliable()){
				ReliableCycles.add(cycle);
			} else {
				UnreliableCycles.add(cycle);
			}
			
			switch (cycle.getSpeed()) {
				case SLOW:
					SlowCycles.add(cycle);
					break;
				case MEDIUM:
					MediumCycles.add(cycle);
					break;
				case FAST:
					FastCycles.add(cycle);
					break;
			}
		}
		*/
		
	}
	
	
	/*	Root = Tour (with Profit)
	Next level = Tracks (with Profit + Registration Fee)
	Next levels = Bike properties (Wild vs Domestic, Reliable vs Unreliable, Speeds) (with Profit + Registration Fee + Bike cost)
	Next level = actual simulation of the races. (with Winnings)
	*/
	
	
	public void runMonteCarlo(int nRuns){
		parentMap.put(root, getChildren(root));
		
		for (Node trackNode: parentMap.get(root)){
			List<Node> children = getChildren(trackNode);
			parentMap.put(trackNode, children);
		}
		
		//new Node(node);
		for (int i=0; i<nRuns; i++){
			SESB(root);
			root.updateMeanProfit(0);
		}
		//Select best
		List<Node> configList = new ArrayList<Node>();
		for (Node node: parentMap.keySet()){
			if(node.getType() != "Tour") {
				for(Node node1: parentMap.get(node)) {
					System.out.println(node.getNumRuns() + node.getName()+"; " + node1.getName() + ": " + node1.getMeanProfit());
					//if (node1.getMeanProfit() > 0){ //Ignore races with losses
						configList.add(node1);
					//}
				}
			}
		}
		/*Collections.sort(configList, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				if (o1.getMeanProfit() < o2.getMeanProfit()) {
					return 1;
				} else if (o2.getMeanProfit() < o1.getMeanProfit()) {
					return -1;
				} else {
					return 0;
				}
			}
		});*/
		
		//Select bike/track combinations which yield high profit
		List<List<Node>> goodList = new ArrayList<List<Node>>();
		configList.add(new Node("Empty1","Empty","Empty",new Node("Empty1","Empty1","Empty1",null)));
		configList.add(new Node("Empty2","Empty","Empty",new Node("Empty2","Empty2","Empty2",null)));
		for (Node cycleNode1: configList){
			for (Node cycleNode2: configList){
				if(cycleNode2 != cycleNode1 && cycleNode2.getParent() != cycleNode1.getParent()) {
					for(Node cycleNode3: configList) {
						if(cycleNode3 != cycleNode1 && cycleNode3 != cycleNode2 && cycleNode3.getParent() != cycleNode1.getParent() && cycleNode2.getParent() != cycleNode3.getParent()) {
							double cost;
							if(cycleNode1.getName() != "Empty") {
								cost = ((Cycle) cycleNode1.getObject()).getPrice();
							} else {
								cost = 0;
							}
							if(cycleNode2.getName() != "Empty") {
								if(cycleNode2.getName() == cycleNode1.getName()) {
									//do nothing
								} else {
									cost += ((Cycle) cycleNode2.getObject()).getPrice();
								} 
							}
							if((cycleNode3.getName() == cycleNode1.getName()) ||(cycleNode3.getName() == cycleNode2.getName())|| (cycleNode3.getName() == "Empty")) {
								//do nothing
							} else {
								cost += ((Cycle) cycleNode3.getObject()).getPrice();
							}
							if(cycleNode1.getName() != "Empty") {
								cost+= ((Track) cycleNode1.getParent().getObject()).getRegistrationFee();
							}
							if(cycleNode2.getName() != "Empty") {
								cost+= ((Track) cycleNode2.getParent().getObject()).getRegistrationFee();
							}
							if(cycleNode3.getName() != "Empty") {
								cost+= ((Track) cycleNode3.getParent().getObject()).getRegistrationFee();
							}

							if(cost > setup.getStartupMoney()) {
								//Yo fucked
							} else {
								List<Node> temp = new ArrayList<Node>();
								temp.add(cycleNode1);
								temp.add(cycleNode2);
								temp.add(cycleNode3);
								goodList.add(temp);
							}
						}
					}
				}
			}
		}
		
		Collections.sort(goodList, new Comparator<List<Node>>() {
			public int compare(List<Node> o1, List<Node> o2) {
				double profit1 = 0;
				double profit2 = 0;
				for(Node n: o1) {
					profit1 += n.getMeanProfit();
				}
				for(Node n: o2) {
					profit2 += n.getMeanProfit();
				}
				if (profit1 < profit2) {
					return 1;
				} else if (profit2 < profit1) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		
		for(Node n5 : goodList.get(0)) {
			if(n5.getName()!= "Empty") {
				tour.registerTrack((Track) n5.getParent().getObject(), 1);
				if(!tour.getPurchasedCycles().contains((Cycle)n5.getObject())) {
					tour.buyCycle((Cycle) n5.getObject()); 
				}
			}
		}
		
		Consultant consultant = new Consultant();
		consultant.solveTour(tour,goodList.get(0));

		//
	}
	
	
	public void SESB(Node node){ //Select child of node
		double C=1000.0; //Exploration vs. exploitation parameter
		double maxProfit = -999999;
		double expectedProfit;
		Node maxChild = null; 
		
		if (node.hasChildren()){
			//Select			
			for (Node child: parentMap.get(node)){
				//if (child.getNumRuns() < 1){
				//	expectedProfit = child.getMeanProfit();
				//} else {
				expectedProfit = child.getMeanProfit() + C * Math.sqrt(Math.log(node.getNumRuns())/child.getNumRuns());
				//}

				if (expectedProfit > maxProfit){
					maxProfit = expectedProfit;
					maxChild = child;
				}
				
			}
			//Expand
			System.out.println(maxChild);
			SESB(maxChild);
			
		} else { //Bike and track selected		
			Node cycleNode = node;
			Node trackNode = node.getParent();
			
			double profit;
			/*while(n.getParent().getType() != "Tour"){
				n = n.getParent();
			}*/
			
			//Simulate
			profit = runSimulation( (Track) trackNode.getObject(), (Cycle) cycleNode.getObject() );
			System.out.println(profit);
			//Backpropagate
			//Node nb  = new Node(node);
			/*while(nb.getType() != "Tour"){
				System.out.println("Updating " + nb.getType()+ "with profit "  + profit);
				nb.updateMeanProfit(profit);
				nb = nb.getParent();
			}*/
			cycleNode.updateMeanProfit(profit);
			trackNode.updateMeanProfit(profit);
			//System.out.println(node.getNumRuns());
		}
	}
	
	
	public List<Node> getChildren (Node node){
		List<Node> children = new ArrayList<Node>();
		switch (node.getType()){
			case "Tour": //Children are tracks
				for (Track track: tour.getTracks()){
					children.add(new Node("Track", track.getName(),track, node));
				}
				break;
			case "Track": //Children are bike wildnesses
				for (Cycle cycle: tour.getPurchasableCycles()){
					children.add(new Node("Cycle",cycle.getName(), cycle, node));

				}
					/*if (WildCycles.contains(cycle)){
						children.add(new Node("Bike_Wildness","Wild", cycle, node));
					} else {
						children.add(new Node("Bike_Wildness","Domestic", cycle, node));
					}
				}
				break;
			case "Bike_Wildness": //Children are bike reliabilities
				for (Cycle cycle: tour.getPurchasableCycles()){
					if (ReliableCycles.contains(cycle)){
						children.add(new Node("Bike_Reliability","Reliable", cycle, node));
					} else {
						children.add(new Node("Bike_Reliability","Unreliable", cycle,node));
					}
				}				
				break;
			case "Bike_Reliability": //Children are bike speeds
				for (Cycle cycle: tour.getPurchasableCycles()){
					if (FastCycles.contains(cycle)){
						children.add(new Node("Bike_Speed","Fast", cycle, node));
					} else if (MediumCycles.contains(cycle)){
						children.add(new Node("Bike_Speed","Medium", cycle, node));
					} else if (SlowCycles.contains(cycle)){
						children.add(new Node("Bike_Speed","Slow", cycle, node));
					}
				}				
				break;
			case "Bike_Speed": //Include?
				//Speed has no children
				break;
				*/
			case "Cycle":
				break;
		}
		return children;
	}
	
	public double runSimulation(Track track, Cycle cycle){
		
		Tour tour = new Tour(setup);
		tour.money = 10000;
		double initialMoney = 10000;
		tour.buyCycle(cycle);
		tour.registerTrack(track, 1);//track.getStartingPositions().size());

		MCTS mcts = new MCTS(tour);
		//ArrayList<Action> actions = new ArrayList<Action>();
		
		while (!tour.isFinished()) { //Running through race
			
			if (tour.isPreparing()) {
				// Race hasn't started. Choose a track, then prepare your
				// players by choosing their cycles and start positions
				//Track track = tour.getUnracedTracks().get(0);

				ArrayList<Player> players = new ArrayList<Player>();
				Map<String, GridCell> startingPositions = track
						.getStartingPositions();
				String id = "";
				GridCell startPosition = null;
				//Add each player to race
				for (Map.Entry<String, GridCell> entry : startingPositions
						.entrySet()) {
					id = entry.getKey();
					startPosition = entry.getValue();
					break; //Adds only one player if this is here
				}
				players.add(new Player(id, cycle, startPosition));

				// Start race
				tour.startRace(track, players);
			}

			
			//Step through each action
			Config config = new Config(tour.getCurrentTrack(), cycle);
			RaceSim currentSim = tour.getCurrentSim();
			/*System.out.println("Player position: "
					+ currentSim.getCurrentState().getPlayers().get(0)
							.getPosition());
			for (Opponent o : currentSim.getCurrentState().getOpponents()) {
				System.out.println("Opponent position: " + o.getPosition());
			}
			

			if (RaceSimTools.isObstacle(currentSim.getCurrentState()
					.getPlayers().get(0).getPosition(), tour.getCurrentTrack())) {
				System.out.println("Hit static obstacle");
			}
			*/
			Action a = mcts.findNextAction(config, currentSim);
			//System.out.println("Next action: " + a.toString());
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(a);
			tour.stepTurn(actions);
		}
		return tour.getCurrentMoney() - initialMoney;
	}
}
