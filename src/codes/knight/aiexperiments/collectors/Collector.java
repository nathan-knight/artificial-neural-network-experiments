package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.gamecore.Agent;
import codes.knight.aiexperiments.gamecore.GameObject;

import java.awt.*;

public class Collector extends GameObject implements Agent {
	
	/*Structure:
	 * Input Nodes:
	 * Angle of nearest coin relative to direction facing
	 * Angle of center relative to direction facing
	 * Carrying coin?
	 * 
	 * Output Nodes:
	 * Turn angle
	 * Speed
	 * */

	private Network m_network;
	private float m_fitness;

	private boolean m_hasCoin = false;
	private Color m_color = Color.WHITE;
	private float m_angle;
	
	public Collector() {
		super(0, 0);
		setNetwork(new Network(1, 1, 3, 3));
		randomizeNetwork();
	}
	
	public Collector(Network n, float x, float y) {
		super(x, y);
		setNetwork(n);
	}

	public boolean hasCoin() {
		return m_hasCoin;
	}
	
	public void setHasCoin(boolean value) {
		this.m_hasCoin = value;
	}
	
	public float getAngle() {
		return m_angle;
	}
	
	public void adjustAngle(float adjustment) {
		m_angle += adjustment;
		m_angle %= Math.PI * 2;
	}

	public Color getColor() {
		return m_color;
	}

	public void setColor(Color color) {
		this.m_color = color;
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
		getNetwork().randomize();
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
