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
		for (Track track : tour.getUnracedTracks()) {
			for (Cycle cycle : tour.getPurchasableCycles()) {
				configMap.put(new Config(track, cycle),
						-track.getRegistrationFee() - cycle.getPrice());
			}
		}
	}

	public Map<Action, Double> generateAction(Config config, int i) {
		Map<Action, Double> actionMap = new HashMap<Action, Double>();
		Cycle cycle = config.c;
		switch (cycle.getSpeed()) {
		case SLOW:
			if (i == 1) {
				actionMap.put(Action.FS, 0.7);
				// actionMap.put(Action.NE, 0.2);
				actionMap.put(Action.SE, 0.3);
			}
			if (i == 0) {
				actionMap.put(Action.FS, 0.6);
				actionMap.put(Action.NE, 0.2);
				actionMap.put(Action.SE, 0.2);
			}
			if (i == -1) {
				actionMap.put(Action.FS, 0.7);
				actionMap.put(Action.NE, 0.3);
				// actionMap.put(Action.SE, 0.3);
			}
			break;
		case MEDIUM:
			if (i == 0) {
				actionMap.put(Action.FM, 0.4);
				actionMap.put(Action.FS, 0.2);
				actionMap.put(Action.NE, 0.2);
				actionMap.put(Action.SE, 0.2);
			}
			if (i == 1) {
				actionMap.put(Action.FM, 0.5);
				actionMap.put(Action.FS, 0.25);
				// actionMap.put(Action.NE, 0.2);
				actionMap.put(Action.SE, 0.25);
			}
			if (i == -1) {
				actionMap.put(Action.FM, 0.5);
				actionMap.put(Action.FS, 0.25);
				actionMap.put(Action.NE, 0.25);
				// actionMap.put(Action.SE, 0.2);
			}
			break;
		case FAST:
			if (i == 0) {
				actionMap.put(Action.FF, 0.35);
				actionMap.put(Action.FM, 0.2);
				actionMap.put(Action.FS, 0.15);
				actionMap.put(Action.NE, 0.15);
				actionMap.put(Action.SE, 0.15);
			}
			if (i == 1) {
				actionMap.put(Action.FF, 0.5);
				actionMap.put(Action.FM, 0.2);
				actionMap.put(Action.FS, 0.15);
				// actionMap.put(Action.NE, 0.15);
				actionMap.put(Action.SE, 0.15);
			}
			if (i == -1) {
				actionMap.put(Action.FF, 0.5);
				actionMap.put(Action.FM, 0.2);
				actionMap.put(Action.FS, 0.15);
				actionMap.put(Action.NE, 0.15);
				// actionMap.put(Action.SE, 0.15);
			}

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
			int y = currentSim.getCurrentState().getPlayers().get(0)
					.getPosition().getRow();
			int yparam = 0;
			if (y == 0) {
				yparam = 1;
			} else if (y == currentSim.getTrack().getNumRows() - 1) {
				yparam = -1;
			}
			actions.add(RaceSimTools.chooseRandom(
					generateAction(config, yparam), random));
			currentSim.stepTurn(actions);
		}
		if (currentSim.getCurrentStatus() == RaceState.Status.WON) {
			return currentSim.getTrack().getPrize()
					- currentSim.getTotalDamageCost() - config.capitalCost();
		} else {
			return -currentSim.getTotalDamageCost() - config.capitalCost();
		}
	}

	public Action findNextAction(Config config, RaceSim currentSim,
			boolean online) {
		Action bestAction = Action.ST;
		double bestScore = -9999;
		int n = 50;
		int counter = 0;
		int y = currentSim.getCurrentState().getPlayers().get(0).getPosition()
				.getRow();
		int yparam = 0;
		if (y == 0) {
			yparam = 1;
		} else if (y == currentSim.getTrack().getNumRows() - 1) {
			yparam = -1;
		}
		for (Action a : generateAction(config, yparam).keySet()) {
			int n1 = generateAction(config, yparam).keySet().size();
			double score = 0;
			if (online) {
				double start = System.currentTimeMillis();
				counter = 0;
				while (System.currentTimeMillis() < start + (800 / n1)) {
					// for (int i = 0; i < n; i++) {
					ArrayList<Action> actions = new ArrayList<Action>();
					actions.add(a);
					RaceSim currentSimCopy = new RaceSim(currentSim);
					currentSimCopy.stepTurn(actions);
					score += rolloutFromCell(config, currentSimCopy);
					counter++;
				}

				if ((score / counter) > bestScore) {
					bestScore = score / counter;
					bestAction = a;
				}
			} else {
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
		}
		return bestAction;
	}
}
