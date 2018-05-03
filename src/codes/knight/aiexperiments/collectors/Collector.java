package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.gamecore.Agent;
import codes.knight.aiexperiments.gamecore.GameObject;
import net.jafama.FastMath;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

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

	private static final int OUTPUT_NEURON_COUNT = 3;

	private static final int DRAW_SIZE = 10;
	private static final int DRAW_COIN_SIZE = 8;
	private static final int COLOR_CEILING = 0xCCCCCC;

	public Collector(int inputs, int hiddenLayers, int neuronsPerLayer) {
		super(0, 0);
		setNetwork(new Network(inputs, hiddenLayers, neuronsPerLayer, OUTPUT_NEURON_COUNT));
		randomizeNetwork();
	}

	public Collector(String n, int inputs, int hiddenLayers, int neuronsPerLayer, float x, float y) {
		super(x, y);
		setNetwork(Network.fromString(n, inputs, hiddenLayers,
				neuronsPerLayer, OUTPUT_NEURON_COUNT));
	}
	
	public Collector(Network n, float x, float y) {
		super(x, y);
		setNetwork(n);
	}

	public void draw(Graphics2D bbg) {
		if (hasCoin()) {
			bbg.setColor(Color.YELLOW);
			bbg.fillOval(getX() - DRAW_COIN_SIZE / 2, getY() - DRAW_COIN_SIZE / 2, DRAW_COIN_SIZE, DRAW_COIN_SIZE);
		}

		bbg.setColor(Color.WHITE);
		bbg.drawLine(getX(), getY(),
				(int)(x + 10 * Math.cos(getAngle())),
				(int)(y + 10 * Math.sin(getAngle())));

		bbg.setColor(getColor());
		Rectangle collectorRect = new Rectangle(-DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
		Path2D.Double rectPath = new Path2D.Double();
		rectPath.append(collectorRect, false);
		AffineTransform transform = new AffineTransform();
		transform.translate(x, y);
		transform.rotate(getAngle());
		rectPath.transform(transform);
		bbg.draw(rectPath);
	}

	public void tick(CollectorsExperiment gameObj, int width, int height, List<Coin> coins, List<Collector> collectors, GameObject center) {
		adjustFitness(-1 / CollectorsExperiment.TICKS_PER_GENERATION);

		Coin nearestCoin = GameObject.findNearest(this, coins);

		if (!hasCoin() && this.distanceTo(nearestCoin) < 10f) {
			this.adjustFitness(1);
			coins.remove(nearestCoin);
			if (gameObj.usesCenterDropOff()) {
				setHasCoin(true);
			} else {
				coins.add(new Coin((int) (Math.random() * width), (int) (Math.random() * height)));
				nearestCoin = findNearest(coins);
			}
		}

		if(hasCoin()) {
			if(distanceTo(center) < 10) {
				setHasCoin(false);
				adjustFitness(1f);
				coins.add(new Coin((int) (Math.random() * width), (int) (Math.random() * height)));
				nearestCoin = findNearest(coins);
			}
		}

		float[] networkInput = new float[gameObj.getInputNeuronCount()];
		int neuron = 0;

		if (gameObj.usesCenterDropOff()) {
			networkInput[neuron++] = angleTo(center);
			networkInput[neuron++] = hasCoin() ? 1 : -1;
		}

		if (gameObj.usesNeighbourAwareness()) {
			Collector nearest = findNearest(collectors);
			networkInput[neuron++] = angleTo(nearest);
			networkInput[neuron++] = distanceTo(nearest);
		}

		networkInput[neuron] = angleTo(nearestCoin);

		float[] output = feed(networkInput);
		float angleAdjustment = (output[0] - 0.5f) * 0.3f;
		adjustAngle(angleAdjustment);
		float dX = (float) Math.cos(getAngle()) * output[1];
		float dY = (float) Math.sin(getAngle()) * output[1];
		move(dX, dY);

		setColor(new Color((int)(output[2] * (16777216 - COLOR_CEILING))));

		//Keep in boundaries
		if(getX() < 0) setX(0);
		if(getY() < 0) setY(0);
		if(getX() > width) setX(width);
		if(getY() > height) setY(height);
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

	public float angleTo(GameObject other) {
		float angle = other == null
				? 0
				: (float) ((FastMath.atan2(getY() - other.getY(), getX() - other.getX()) - getAngle() - Math.PI));
		if (angle < -Math.PI) angle += 2 * Math.PI;
		return angle;
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
