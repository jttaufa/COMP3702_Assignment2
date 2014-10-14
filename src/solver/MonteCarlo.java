package solver;

import java.util.ArrayList;
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


public class MonteCarlo {
	Random random = new Random();
	int numIter;
	Node root;
	Tour tour;
	Setup setup;
	
	List<Cycle> WildCycles = new ArrayList<Cycle>();
	List<Cycle> DomesticCycles = new ArrayList<Cycle>();
	List<Cycle> ReliableCycles = new ArrayList<Cycle>();
	List<Cycle> UnreliableCycles = new ArrayList<Cycle>();
	List<Cycle> FastCycles = new ArrayList<Cycle>();
	List<Cycle> MediumCycles = new ArrayList<Cycle>();
	List<Cycle> SlowCycles = new ArrayList<Cycle>();

	
	public MonteCarlo(Setup setup, Tour tour, int numIter){
		this.setup = setup;
		this.root = new Node("Tour", "tour", tour, null);
		this.tour = tour;
		this.numIter =numIter;  
		
		
		for (Cycle cycle: tour.getPurchasableCycles()){
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
		
	}
	
	
	/*	Root = Tour (with Profit)
	Next level = Tracks (with Profit + Registration Fee)
	Next levels = Bike properties (Wild vs Domestic, Reliable vs Unreliable, Speeds) (with Profit + Registration Fee + Bike cost)
	Next level = actual simulation of the races. (with Winnings)
	*/
	
	
	public void runMonteCarlo(int nRuns){
		
		for (int i=0; i<nRuns; i++){
			SESB(root);
		}
	}
	
	
	public void SESB(Node node){ //Select child of node
		double C=Math.sqrt(2); //Exploration vs. exploitation parameter
		double maxProfit = 0;
		double expectedProfit;
		Node maxChild = null; 
		
		if (node.hasChildren()){
			//Select
			for (Node child: getChildren(node)){
				expectedProfit = child.getMeanProfit() + C * Math.sqrt(Math.log(node.getNumRuns())/child.getNumRuns());
				if (expectedProfit > maxProfit){
					maxProfit = expectedProfit;
					maxChild = child;
				}
			}
			//Expand
			SESB(maxChild);
			
		} else { //Bike and track selected
			Node n = new Node(node);
			double profit;
			while(n.getParent().getType() != "Track"){
				n = n.getParent();
			}
			
			//Simulate
			profit = runSimulation( (Track) n.getObject(), (Cycle) node.getObject() );
			
			//Backpropagate
			Node nb  = new Node(node);
			while(nb.getParent().getType() != "Tour"){
				nb.updateMeanProfit(profit);
				nb = nb.getParent();
			}
		}
	}
	
	public List<Node> getChildren (Node node){
		List<Node> children = new ArrayList<Node>();
		switch (node.getType()){
			case "Tour": //Children are tracks
				for (Track track: tour.getTracks()){
					children.add(new Node("Track", track.toString(),track, node));
				}
				break;
			case "Track": //Children are bike wildnesses
				for (Cycle cycle: tour.getPurchasableCycles()){
					if (WildCycles.contains(cycle)){
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
		}
		return children;
	}
	
	public double runSimulation(Track track, Cycle cycle){
		
		Tour tour = new Tour(setup);
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
			System.out.println("Player position: "
					+ currentSim.getCurrentState().getPlayers().get(0)
							.getPosition());
			for (Opponent o : currentSim.getCurrentState().getOpponents()) {
				System.out.println("Opponent position: " + o.getPosition());
			}

			if (RaceSimTools.isObstacle(currentSim.getCurrentState()
					.getPlayers().get(0).getPosition(), tour.getCurrentTrack())) {
				System.out.println("Hit static obstacle");
			}
			Action a = mcts.findNextAction(config, currentSim);
			System.out.println("Next action: " + a.toString());
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(a);
			tour.stepTurn(actions);
		}
		return tour.getCurrentMoney() - setup.getStartupMoney();
	}
}
