package solver;

/**
 * Creates a Node object that
 * 
 * @author Route66
 * 
 */

public class Node {
	private double meanProfit; // Average profit won from this node
	private Node parent; // Parent node
	private String type; // Type of node (Track, Speed, Reliability, Wildness)
	private Object nodeObj; // Object to which node is referring (could be a
							// Track, Bike, or Tour)
	private int nIter; // Number of times node has been called
	private String name;

	/**
	 * Creates a Node with x and y values
	 * 
	 * @param x
	 *            the x value of the Node
	 * @param y
	 *            the y value of the Node
	 */
	public Node(String nodeType, String nodeName, Object nodeObj, Node parent) {
		this.type = nodeType;
		this.name = nodeName;
		this.nodeObj = nodeObj;
		this.parent = parent;
		this.meanProfit = 0;
		/*
		 * if (nodeObj instanceof Cycle){ this.meanProfit = - ((Cycle)
		 * nodeObj).getPrice(); } else if (nodeObj instanceof Track){
		 * this.meanProfit = - ((Track) nodeObj).getPrize() + ((Track)
		 * nodeObj).getRegistrationFee(); }
		 */
		this.nIter = 1;
	}

	public Node(Node node) {
		this.type = node.getType();
		this.meanProfit = node.getMeanProfit();
		this.nIter = node.getNumRuns();
		this.name = node.getNodeName();
		this.parent = node.getParent();
	}

	public String getNodeName() {
		return this.name;
	}

	/**
	 * Returns the average profit for this selected node
	 */
	public double getMeanProfit() {
		return this.meanProfit;
	}

	public int getNumRuns() {
		return this.nIter;
	}

	public Node getParent() {
		return this.parent;
	}

	public Object getObject() {
		return this.nodeObj;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public boolean hasChildren() {
		return (this.type != "Cycle");
	}

	/**
	 * Updates the average profits of this node for a new profit value
	 */
	public void updateMeanProfit(double newProfit) {
		meanProfit = (((nIter - 1) * meanProfit) + newProfit) / nIter; // Recalculate
																		// new
																		// mean
		nIter++; // Increment the number of iterations
	}

	/**
	 * Provides the String representation of the Node.
	 */
	@Override
	public String toString() {
		String str;
		if (this.type == "Tour") {
			str = type + "\t Object: " + nodeObj + "\tParent: None";
			str += "\t Ave Profit for " + nIter + " runs: $" + meanProfit;

		} else {
			str = type + "\t Object: " + nodeObj + "\tParent: "
					+ parent.getName();
			str += "\t Ave Profit for " + nIter + " runs: $" + meanProfit;
		}
		return str;
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
			if (this.nodeObj != testNode.getObject()
					|| this.parent != testNode.getParent()
					|| this.type != testNode.getType()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 7;
		int result;

		result = prime * ((type == null) ? 0 : type.hashCode());
		result = result + prime * ((parent == null) ? 0 : parent.hashCode());

		return result;
	}

}
