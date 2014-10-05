package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import problem.Action;
import problem.Cycle;
import problem.RaceSim;
import problem.RaceSimTools;
import problem.RaceState;
import problem.Tour;
import problem.Track;

public class MCTS {

	private Tour tour;
	private Map<Config, Double> configMap;

	public MCTS(Tour tour) {
		configMap = new HashMap<Config, Double>();
		this.tour = tour;
	}

	public void fillConfigs() {
		for (Track track : tour.getAvailableTracks()) {
			for (Cycle cycle : tour.getPurchasableCycles()) {
				configMap.put(new Config(track, cycle),
						-track.getRegistrationFee() - cycle.getPrice());
			}
		}
	}

	public Map<Action, Double> generateAction(Config config) {
		Map<Action, Double> actionMap = new HashMap<Action, Double>();
		Cycle cycle = config.c;
		switch (cycle.getSpeed()) {
		case SLOW:
			actionMap.put(Action.FS, 0.5);
			actionMap.put(Action.NE, 0.25);
			actionMap.put(Action.SE, 0.25);
			break;
		case MEDIUM:
			actionMap.put(Action.FM, 0.25);
			actionMap.put(Action.FS, 0.25);
			actionMap.put(Action.NE, 0.25);
			actionMap.put(Action.SE, 0.25);
			break;
		case FAST:
			actionMap.put(Action.FF, 0.2);
			actionMap.put(Action.FM, 0.2);
			actionMap.put(Action.FS, 0.2);
			actionMap.put(Action.NE, 0.2);
			actionMap.put(Action.SE, 0.2);
			break;
		}
		return actionMap;
	}

	public Double rolloutFromCell(Config config, RaceSim currentSim) {
		// RaceState state = tour.getLatestRaceState();
		// System.out.println("Player position: " +
		// state.getPlayers().get(0).getPosition());
		//
		// // Example: Keep moving forward slowly
		// ArrayList<Action> actions = new ArrayList<Action>();
		// actions.add(Action.FS);
		// tour.stepTurn(actions);
		Random random = new Random();
		while (!currentSim.isFinished()) {
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(RaceSimTools.chooseRandom(generateAction(config),
					random));
			currentSim.stepTurn(actions);
		}
		if (currentSim.getCurrentStatus() == RaceState.Status.WON) {
			return currentSim.getTrack().getPrize()
					- currentSim.getTotalDamageCost() - config.capitalCost();
		} else {
			return -currentSim.getTotalDamageCost() - config.capitalCost();
		}
	}

	public Action findNextAction(Config config, RaceSim currentSim) {
		Action bestAction = Action.ST;
		double bestScore = -9999;
		int n = 50;
		for (Action a : generateAction(config).keySet()) {
			double score = 0;
			for (int i = 0; i < n; i++) {
				ArrayList<Action> actions = new ArrayList<Action>();
				actions.add(a);
				RaceSim currentSimCopy = new RaceSim(currentSim);
				currentSimCopy.stepTurn(actions);
				score += rolloutFromCell(config, currentSimCopy);
			}
			if ((score / n) > bestScore) {
				bestScore = score / n;
				bestAction = a;
			}
		}
		return bestAction;
	}
}
