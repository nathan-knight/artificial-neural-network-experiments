package codes.knight.aiexperiments.gamecore;

import codes.knight.aiexperiments.Network;

public class Agent extends GameObject {
	
	protected Network network;
	protected float fitness;

	public Agent(int inputNodeCount, int hiddenLayerCount, int hiddenLayerNodeCount, int outputNodeCount) {
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
	}
	
	public float[] feed(float[] values) {
		return network.run(values);
	}
	
	public void adjustFitness(float adjustment) {
		fitness += adjustment;
	}
	
	public float getFitness() {
		return fitness;
	}
	
	public void randomizeNetwork() {
		network.randomize(-1, 1);
	}
	
	public Network getNetwork() {
		return network;
	}
	
	public Agent getInstance(Network brain) {
		return new Agent(brain);
	}
}
