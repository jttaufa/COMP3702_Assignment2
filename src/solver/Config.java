package solver;

import problem.Cycle;
import problem.Track;

public class Config {

	public Track t;
	public Cycle c;
	private int counter = 0;

	public Config(Track t, Cycle c) {
		this.t = t;
		this.c = c;
	}

	@Override
	public int hashCode() {
		return (t.hashCode() * 7) + (17 * c.hashCode());
	}

	public Double capitalCost() {

		return t.getRegistrationFee() + c.getPrice();
	}

	public int getCounter() {
		return counter;
	}

	public void incrementCounter() {
		counter += 1;
	}
}
