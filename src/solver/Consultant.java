package solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import problem.Action;
import problem.Cycle;
import problem.GridCell;
import problem.Opponent;
import problem.Player;
import problem.RaceSim;
import problem.RaceSimTools;
import problem.Tour;
import problem.Track;

/**
 * Implement the code to chose the best bike for 3 races with the intention of
 * winning the most prize money.
 * 
 * @author Route66
 * 
 */
public class Consultant {

	/**
	 * Solves a tour. Replace existing code with your code.
	 * 
	 * @param tour
	 */
	public void solveTour(Tour tour, List<Node> goodList) {

		// You should get information from the tour using the getters, and
		// make your plan.

		// Example:
		// Buy the first cycle that is Wild

		MCTS mcts = new MCTS(tour);
		Cycle cycle = (Cycle) goodList.get(0).getObject();
		while (!tour.isFinished()) {

			if (tour.isPreparing()) {

				// Race hasn't started. Choose a track, then prepare your
				// players by choosing their cycles and start positions

				// Example:
				mcts = new MCTS(tour);
				Track track = (Track) goodList.get(0).getParent().getObject();
				cycle = (Cycle) goodList.remove(0).getObject();
				ArrayList<Player> players = new ArrayList<Player>();
				Map<String, GridCell> startingPositions = track
						.getStartingPositions();
				String id = "";
				GridCell startPosition = null;
				for (Map.Entry<String, GridCell> entry : startingPositions
						.entrySet()) {
					id = entry.getKey();
					startPosition = entry.getValue();
					break;
				}
				players.add(new Player(id, cycle, startPosition));

				// Start race
				tour.startRace(track, players);
			}

			// Decide on your next action here. tour.getLatestRaceState()
			// will probably be helpful.

			// Example: Output current position of player
			// RaceState state = tour.getLatestRaceState();
			// System.out.println("Player position: " +
			// state.getPlayers().get(0).getPosition());
			//
			// // Example: Keep moving forward slowly
			// ArrayList<Action> actions = new ArrayList<Action>();
			// actions.add(Action.FS);
			// tour.stepTurn(actions);

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
			Action a = mcts.findNextAction(config, currentSim, true);
			System.out.println("Next action: " + a.toString());
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(a);
			tour.stepTurn(actions);

		}
	}
}
