package codes.knight.aiexperiments.colorgraph;

import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.gamecore.Agent;

public class Colorer implements Agent {

	private Network m_network;
	private float m_fitness;

	public Colorer(int layers, int neuronsPerLayer) {
		setNetwork(new Network(2, layers, neuronsPerLayer, 1));
	}

	@Override
	public Network getNetwork() {
		return m_network;
	}

	@Override
	public void setNetwork(Network network) {
		m_network = network;
	}

	@Override
	public void randomizeNetwork() {
		m_network.randomize();
	}

	@Override
	public float[] feed(float[] input) {
		return m_network.run(input);
	}

	@Override
	public float getFitness() {
		return m_fitness;
	}

	@Override
	public void adjustFitness(float adjustment) {
		m_fitness += adjustment;
	}
}
