package solver;

/**
 * Creates a Node object that
 * 
 * @author Route66
 *
 */

public class Node {
	public double x;
	public double y;
	public double r;
	public double GScore;
	public double FScore; // The straight line distance from the current Node
							// to the final Node.
	public double startToNode;
	public double endToNode;
	public boolean startNode;
	public boolean endNode;

	/**
	 * Creates a Node with x and y values
	 * 
	 * @param x
	 *            the x value of the Node
	 * @param y
	 *            the y value of the Node
	 */
	public Node(double x, double y) {
		this.x = x;
		this.y = y;
		this.GScore = 0;
		this.FScore = 0;

	}

	/**
	 * Creates a Node with x and y values from a point
	 * 
	 * @param point
	 *            in the form of a Point2D.Double point
	 */
	public Node(Point2D.Double point) {
		this.x = point.getX();
		this.y = point.getY();
		this.GScore = 0;
		this.FScore = 0;
	}

	/**
	 * Returns the x-value.
	 */
	@Override
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y-value.
	 */
	@Override
	public double getY() {
		return this.y;
	}

	/**
	 * Sets the x and y values of the Node.
	 */
	@Override
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the current GScore of the Node.
	 * 
	 * @return the GScore
	 */
	public double getGScore() {
		return this.GScore;
	}

	/**
	 * Gets the current FScore of the Node.
	 * 
	 * @return the FScore
	 */
	public double getFScore() {
		return this.FScore;
	}

	/**
	 * Provides the String representation of the Node.
	 */
	@Override
	public String toString() {
		return "x = " + this.x + " & y = " + this.y;
	}

	/**
	 * Converts the Node to a Point2D.Double
	 * 
	 * @return a Point2D.Double(x, y)
	 */
	public Point2D.Double toPoint2D() {
		return new Point2D.Double(this.x, this.y);
	}

	/**
	 * Calculates the straight line distance from the current Node to a
	 * different Node.
	 * 
	 * @param n
	 *            a different Node to calculate the distance to.
	 * @return the distance r
	 */
	public double getDistanceTo(Node n) {
		double dx = (this.getX() - n.getX());
		double dy = (this.getY() - n.getY());
		this.r = Math.sqrt(dx * dx + dy * dy);
		return this.r;
	}

	/**
	 * Two Nodes are equal if and only if their x & y values, GScore and FScore
	 * are equivalent.
	 */
	@Override
	public boolean equals(Object o) {
		// Check if the object is an instance of Node using the inbuilt
		// function. Cast the object as a Node. Check if the new Node's x & y
		// values, GScore and FScore are equivalent.
		if (o instanceof Node) {
			Node testNode = (Node) o;
			if (this.x != testNode.getX() || this.y != testNode.getY()
					|| this.GScore != testNode.getGScore()
					|| this.FScore != testNode.getFScore()) {
				return false;
			}
			return true;
		}
		return false;
	}
}
