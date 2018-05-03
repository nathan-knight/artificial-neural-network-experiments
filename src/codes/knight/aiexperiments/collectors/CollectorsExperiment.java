package codes.knight.aiexperiments.collectors;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import codes.knight.aiexperiments.BinaryGeneticAlgorithm;
import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.utils.Utils;

public class CollectorsExperiment extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private static final String SAVED_AGENT_FILE_PREFIX = "Saves/agents_collectors_";
	public static final int TICKS_PER_GENERATION = 10000;
	private static final int GENERATIONS_PER_AUTOSAVE = 10;
	private static final int FRAME_RATE_CAP = 60;

	private Thread m_masterThread;
	private boolean running = true;
	private long m_targetTickCount = 0;
	private BufferedImage m_backbuffer;

	private ArrayList<Collectors> m_collectorsInstances;
	private ArrayList<Collector> m_collectors;
	private int m_generations = 0;
	private int m_numberOfSimulations;
	private int m_currentSimulation = 0;

	private volatile int m_ticksThisIteration = 0;
	private boolean m_speedmode = false;

	private boolean m_saveHighFitnessAgents = false;
	private float 	m_highFitnessThreshold = 90;

	private boolean m_showNearestLine = false;
	private boolean m_showAngleToCoin = false;

	private boolean m_useCenterDropOff = false;
	private boolean m_neighborAware = false;
	private int     m_hiddenLayers = 1;
	private int 	m_neuronsPerHiddenLayer = 3;
	private int		m_collectorsPerInstance = 20;
	private int		m_generationsPerMix = 10;

	private class Collectors implements Runnable {
		private ArrayList<Collector> m_collectors;
		private ArrayList<Coin> m_coins;
		private Center m_center;

		private Thread m_simulationThread;

		private long m_tickCount = 0;
		private int m_localTicksThisSecond = 0;
		private int m_lastFPS = 0;

		private CollectorsExperiment m_experiment;

		public Collectors(CollectorsExperiment experiment) {
			m_experiment = experiment;
			m_collectors = new ArrayList<>();
			m_center = new Center(getWidth() / 2, getHeight() / 2);
		}

		@Override
		public void run() {
			m_coins = new ArrayList<>();
			for(int i = 0; i < 50; i++) {
				Coin coin = new Coin((int) (Math.random() * getWidth()), (int) (Math.random() * getHeight()));
				m_coins.add(coin);
			}

			long lastTime = System.currentTimeMillis();
			long time = System.currentTimeMillis();
			long lastSecond = time / 1000;

			while(m_tickCount < m_targetTickCount) {
				tick();
				if (!m_speedmode && m_experiment.getDrawnCollectors() == this) {
					draw();
				}
				m_ticksThisIteration++;
				m_localTicksThisSecond++;
				m_tickCount++;
				time = (long) ((1000 / FRAME_RATE_CAP) - (System.currentTimeMillis() - lastTime));
				lastTime = System.currentTimeMillis();
				if (lastSecond - lastTime / 1000 < 0) {
					lastSecond = lastTime / 1000;
					m_lastFPS = m_localTicksThisSecond;
					m_localTicksThisSecond = 0;
				}
				if (!m_speedmode && time > 0) {
					try {
						Thread.sleep(time);
					}
					catch(Exception e){}
				}
			}
		}

		public void join() throws InterruptedException {
			m_simulationThread.join();
		}

		public void start(ArrayList<Collector> collectors) {
			m_collectors = collectors;
			m_simulationThread = new Thread(this);
			m_simulationThread.start();
		}

		public List<Collector> getCollectors() {
			return m_collectors;
		}

		private void tick() {
			m_center.setX(getWidth() / 2);
			m_center.setY(getHeight() / 2);
			for(Collector collector : m_collectors) {
				collector.tick(m_experiment, getWidth(), getHeight(), m_coins, m_collectors, m_center);
			}
		}

		private void draw() {
			if (m_backbuffer == null) m_backbuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			if (m_backbuffer.getWidth() != getWidth() || m_backbuffer.getHeight() != getHeight()) m_backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = getGraphics();
			Graphics2D bbg = (Graphics2D) m_backbuffer.getGraphics();
			bbg.clearRect(0, 0, getWidth(), getHeight());

		for(Collector collector : m_collectors) {
			float x = collector.getX();
			float y = collector.getY();

			collector.draw(bbg);

			if (m_showNearestLine && !collector.hasCoin()) {
				// Draw a line to the nearest coin
				Coin coin = collector.findNearest(m_coins);
				bbg.drawLine(coin.getX(), coin.getY(), (int) x, (int) y);

				if (m_showAngleToCoin) {
					// Draw the angle to the coin
					bbg.drawString(
							String.valueOf(round(collector.angleTo(coin))),
							x - 15, y - 15);
				}

				if (m_neighborAware) {
					Collector c = collector.findNearest(m_collectors);
					bbg.drawLine(c.getX(), c.getY(), (int) x, (int) y);
				}
			}
		}

		for(Coin coin : m_coins) {
			coin.draw(bbg);
		}

		if (m_useCenterDropOff) {
			m_center.draw(bbg);
		}

			bbg.setColor(Color.WHITE);
			bbg.drawString("Generation: " + m_generations, 30, getHeight() - 60);
			bbg.drawString("FPS: " + m_lastFPS, 30, getHeight() - 40);
			bbg.drawString("Viewing simulation: " + (m_currentSimulation + 1) + " of " + m_numberOfSimulations,
					30, getHeight() - 20);

			g.drawImage(m_backbuffer, 0, 0, m_experiment);
		}
	}

	public CollectorsExperiment() {
		this.setSize(800, 600);
		this.setTitle("Collector Test");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_Q) {
					System.out.println("Saving and quitting! Filename: " + getSaveFileName());
					Utils.saveAgentsToDisk(m_generations, m_collectors, getSaveFileName());
					System.exit(0);
				}
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					m_speedmode = !m_speedmode;
					setTitle("Collector Test");
				}
				if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
					viewNextSimulation();
				}
				if (event.getKeyCode() == KeyEvent.VK_LEFT) {
					viewPreviousSimulation();
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
		});
		m_numberOfSimulations = Runtime.getRuntime().availableProcessors();
	}

	public CollectorsExperiment start() {
		this.setVisible(true);
		m_masterThread = new Thread(this);
		m_masterThread.start();

		return this;
	}

	@Override
	public void run() {
		m_collectorsInstances = new ArrayList<>();
		for (int i = 0; i < m_numberOfSimulations; i++) {
			m_collectorsInstances.add(new Collectors(this));
		}

		m_collectors = new ArrayList<>();
		m_generations = initializeCollectors(m_collectors);

		long lastTime = System.currentTimeMillis();

		while (running) {
			m_targetTickCount += TICKS_PER_GENERATION;
			for (int i = 0; i < m_collectorsInstances.size(); i++) {
				Collectors c = m_collectorsInstances.get(i);
				ArrayList<Collector> collectors = new ArrayList<>(m_collectors
						.subList(i * m_collectorsPerInstance, i * m_collectorsPerInstance + m_collectorsPerInstance));
				c.start(collectors);
			}

			if (m_speedmode && System.currentTimeMillis() / 1000 > lastTime / 1000) {
				this.setTitle("Collector Test :: Ticks Per Second: " + (1000 * m_ticksThisIteration / (System.currentTimeMillis() - lastTime)));

				lastTime = System.currentTimeMillis();
				m_ticksThisIteration = 0;
			}

			for (Collectors c : m_collectorsInstances){
				try {
					c.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (m_generations > 0 && m_generations % m_generationsPerMix == 0) {
				breedNextGeneration(m_collectors, true);
			} else {
				m_collectors.clear();
				for (Collectors c : m_collectorsInstances){
					m_collectors.addAll(breedNextGeneration(c.getCollectors(), false));
				}
			}

			m_generations++;
		}
	}

	public CollectorsExperiment saveHighFitnessAgents() {
		m_saveHighFitnessAgents = true;
		return this;
	}

	public CollectorsExperiment setHighFitnessThreshold(float threshold) {
		m_highFitnessThreshold = threshold;
		return this;
	}

	public CollectorsExperiment setShowNearestLine(boolean show) {
		m_showNearestLine = show;
		return this;
	}

	public CollectorsExperiment setShowAngleToCoin(boolean show) {
		m_showAngleToCoin = show;
		return this;
	}

	public CollectorsExperiment setNumberOfHiddenLayers(int num) {
		m_hiddenLayers = num;
		return this;
	}

	public CollectorsExperiment setNeuronsPerHiddenLayer(int num) {
		m_neuronsPerHiddenLayer = num;
		return this;
	}

	public CollectorsExperiment enableCenterDropOff() {
		m_useCenterDropOff = true;
		return this;
	}

	public CollectorsExperiment enableNeighbourAwareness() {
		m_neighborAware = true;
		return this;
	}

	public boolean usesCenterDropOff() {
		return m_useCenterDropOff;
	}

	public boolean usesNeighbourAwareness() {
		return m_neighborAware;
	}

	private String getSaveFileName() {
		StringBuilder sb = new StringBuilder(SAVED_AGENT_FILE_PREFIX);
		boolean usesSpecial = false;

		if (m_useCenterDropOff) {
			sb.append("center");
			usesSpecial = true;
		}

		if (m_neighborAware) {
			if (usesSpecial) sb.append("_");
			sb.append("neighbour");
			usesSpecial = true;
		}

		if (!usesSpecial) {
			sb.append("basic");
		}

		sb.append("_");
		sb.append(m_hiddenLayers);
		sb.append("_");
		sb.append(m_neuronsPerHiddenLayer);
		sb.append(".txt");
		return sb.toString();
	}

	public int getInputNeuronCount() {
		int count = 1;

		if (usesCenterDropOff()) count += 2;
		if (usesNeighbourAwareness()) count += 2;

		return count;
	}

	private float round(float f) {
		return Math.round(f * 100f) / 100f;
	}

	private Collectors getDrawnCollectors() {
		return m_collectorsInstances.get(m_currentSimulation);
	}

	private void saveAgentsToFile() {
		System.out.println("Saving agents to disk!");
		Utils.saveAgentsToDisk(m_generations, m_collectors, getSaveFileName());
	}

	private void initializeNewCollectors(List<Collector> collectors) {
		for(int i = 0; i < m_collectorsPerInstance * m_numberOfSimulations; i++) {
			Collector collector = new Collector(getInputNeuronCount(), m_hiddenLayers, m_neuronsPerHiddenLayer);
			collector.setX((int) (Math.random() * this.getWidth()));
			collector.setY((int) (Math.random() * this.getHeight()));
			collectors.add(collector);
		}
	}

	private int initializeCollectors(List<Collector> collectors) {
		int generations;
		File savedAgents = new File(getSaveFileName());
		if (savedAgents.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(getSaveFileName()))) {
				String line = br.readLine();
				generations = Integer.valueOf(line);
				while ((line = br.readLine()) != null) {
					collectors.add(new Collector(line, getInputNeuronCount(),
							m_hiddenLayers, m_neuronsPerHiddenLayer,
							(float) Math.random() * this.getWidth(),
							(float) Math.random() * this.getHeight()));
				}
				System.out.println("Loaded " + collectors.size() + " collectors from disk");
			} catch (IOException e) {
				generations = 0;
				initializeNewCollectors(collectors);
				e.printStackTrace();
			}
		} else {
			generations = 0;
			initializeNewCollectors(collectors);
		}
		return generations;
	}

	private List<Collector> breedNextGeneration(List<Collector> collectors, boolean print) {
		BinaryGeneticAlgorithm.Breeder<Collector> ga = new BinaryGeneticAlgorithm.Breeder<>(collectors);
		float sumFitness = ga.getFitnessSum();
		float averageFitness = sumFitness / collectors.size();
		if (m_saveHighFitnessAgents && ga.getPeakFitness() >= m_highFitnessThreshold) {
			String filename = "saved_collector " + round(ga.getPeakFitness()) + " " + System.currentTimeMillis() + ".txt";
			System.out.println("Found network with fitness >= " + m_highFitnessThreshold + "! Saving to " + filename);
			Utils.saveNetworkToDisk(ga.getPeakAgent().getNetwork(), filename);
		}
		if (print) System.out.println(new Date() + "\t" + round(averageFitness) + "\t" + round(ga.getPeakFitness()));
		List<Network> nextGenNetworks = ga.breed();
		ArrayList<Collector> nextGen = new ArrayList<>();
		for(Network n : nextGenNetworks) {
			Collector c = new Collector(n, (float) Math.random() * this.getWidth(), (float) Math.random() * this.getHeight());
			nextGen.add(c);
		}
		collectors.clear();
		collectors.addAll(nextGen);
		return collectors;
	}

	private void viewNextSimulation() {
		m_currentSimulation++;
		m_currentSimulation %= m_numberOfSimulations;
	}

	private void viewPreviousSimulation() {
		m_currentSimulation--;
		if (m_currentSimulation < 0) m_currentSimulation = m_numberOfSimulations - 1;
	}
}
