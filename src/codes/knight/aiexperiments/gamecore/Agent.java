package codes.knight.aiexperiments.gamecore;

import codes.knight.aiexperiments.Network;

public interface Agent {

	Network getNetwork();
	void setNetwork(Network network);
	void randomizeNetwork();

	float[] feed(float[] input);

	float getFitness();
	void adjustFitness(float adjustment);

	/*public Agent(int inputNodeCount, int hiddenLayerCount, int hiddenLayerNodeCount, int outputNodeCount) {
		super(0, 0);
		network = new Network(inputNodeCount, hiddenLayerCount, hiddenLayerNodeCount, outputNodeCount);
		network.randomize(-1, 1);
	}
	
	public Agent(Network network) {
		super(0, 0);
		this.network = network;
	}
	
	public Agent(int x, int y) {
		super(x, y);
	}*/
	
	/*public void randomizeNetwork() {
		network.randomize(-1, 1);
	}*/
}
