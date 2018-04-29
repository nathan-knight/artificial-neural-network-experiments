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
import codes.knight.aiexperiments.Utils;

public class Collectors extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private static final String SAVED_AGENT_FILE_PREFIX = "Saves/agents_collectors_";
	public static final int TICKS_PER_GENERATION = 20000;
	private static final int GENERATIONS_PER_AUTOSAVE = 10;
	private static final int FRAME_RATE_CAP = 60;

	private Thread thread;
	private boolean running = true;
	private BufferedImage m_backbuffer;

	private ArrayList<Collector> m_collectors;
	private ArrayList<Coin> m_coins;
	private Center m_center;
	private long m_tickCount = 1;
	private int m_generations = 0;

	private int m_framesThisSecond = 0;
	private int m_lastFPS = 0;
	private boolean m_speedmode = false;

	private boolean m_saveHighFitnessAgents = false;
	private float 	m_highFitnessThreshold = 90;

	private boolean m_showNearestLine = false;
	private boolean m_showAngleToCoin = false;

	private boolean m_useCenterDropOff = false;
	private boolean m_neighborAware = false;

	public Collectors() {
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
				if(event.getKeyCode() == KeyEvent.VK_ENTER) {
					m_speedmode = !m_speedmode;
				}
				if(event.getKeyCode() == KeyEvent.VK_SHIFT) {
					for(Collector c : m_collectors) {
						c.setX((int) (Math.random() * getWidth()));
						c.setY((int) (Math.random() * getHeight()));
					}
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
		});
	}

	public Collectors start() {
		this.setVisible(true);
		thread = new Thread(this);
		thread.start();

		return this;
	}

	public Collectors saveHighFitnessAgents() {
		m_saveHighFitnessAgents = true;
		return this;
	}

	public Collectors setHighFitnessThreshold(float threshold) {
		m_highFitnessThreshold = threshold;
		return this;
	}

	public Collectors setShowNearestLine(boolean show) {
		m_showNearestLine = show;
		return this;
	}

	public Collectors setShowAngleToCoin(boolean show) {
		m_showAngleToCoin = show;
		return this;
	}

	public Collectors enableCenterDropOff() {
		m_useCenterDropOff = true;
		return this;
	}

	public Collectors enableNeighbourAwareness() {
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
		}

		if (!usesSpecial) {
			sb.append("basic");
		}
		sb.append(".txt");
		return sb.toString();
	}

	public int getInputNeuronCount() {
		int count = 1;

		if (usesCenterDropOff()) count++;
		if (usesNeighbourAwareness()) count++;

		return count;
	}

	@Override
	public void run() {
		m_collectors = new ArrayList<>();
		m_coins = new ArrayList<>();
		m_center = new Center(this.getWidth() / 2, this.getHeight() / 2);

		m_generations = initializeCollectors(m_collectors);

		for(int i = 0; i < 50; i++) {
			Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
			m_coins.add(coin);
		}
		
		long lastTime = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		long lastSecond = time / 1000;

		while(running) {
			if (m_tickCount % (GENERATIONS_PER_AUTOSAVE * TICKS_PER_GENERATION) == 0) {
				saveAgentsToFile();
			}
			if(m_tickCount % TICKS_PER_GENERATION == 0) {
				m_generations++;
				breedNextGeneration();
			}
			tick();
			if(!m_speedmode) draw();
			m_framesThisSecond++;
			m_tickCount++;
			time = (long) ((1000 / FRAME_RATE_CAP) - (System.currentTimeMillis() - lastTime));
			lastTime = System.currentTimeMillis();
			if(lastSecond - lastTime / 1000 < 0) {
				lastSecond = lastTime / 1000;
				this.setTitle("Collector Test :: Generation: " + m_generations + " :: " + m_framesThisSecond + " FPS");
				m_lastFPS = m_framesThisSecond;
				m_framesThisSecond = 0;
			}
			if (time > 0) { 
				try {
					if(!m_speedmode) Thread.sleep(time);
				} 
				catch(Exception e){} 
			}
		}
	}

	private void tick() {
		m_center.setX(this.getWidth() / 2);
		m_center.setY(this.getHeight() / 2);
		for(Collector collector : m_collectors) {
			collector.tick(this, getWidth(), getHeight(), m_coins, m_collectors, m_center);
		}
	}

	private void draw() {
		if(m_backbuffer == null) m_backbuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		if(m_backbuffer.getWidth() != getWidth() || m_backbuffer.getHeight() != getHeight()) m_backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = getGraphics();
		Graphics2D bbg = (Graphics2D) m_backbuffer.getGraphics();
		bbg.clearRect(0, 0, getWidth(), getHeight());

		for(Collector collector : m_collectors) {
			float angle = collector.getAngle();
			float x = collector.getX();
			float y = collector.getY();

			if (m_showNearestLine && !collector.hasCoin()) {
				// Draw a line to the nearest coin
				Coin coin = findNearestCoin(collector);
				bbg.drawLine(coin.getX(), coin.getY(), collector.getX(), collector.getY());

				if (m_showAngleToCoin) {
					// Draw the angle to the coin
					bbg.drawString(
							String.valueOf(round(collector.angleTo(coin))),
							x - 15, y - 15);
				}
			}
			collector.draw(bbg);
		}

		for(Coin coin : m_coins) {
			coin.draw(bbg);
		}

		if (m_useCenterDropOff) {
			m_center.draw(bbg);
		}

		bbg.setColor(Color.WHITE);
		bbg.drawString("Generation: " + m_generations, 30, this.getHeight() - 50);
		bbg.drawString("FPS: " + m_lastFPS, 30, this.getHeight() - 30);

		g.drawImage(m_backbuffer, 0, 0, this);
	}
	
	private Coin findNearestCoin(Collector collector) {
		Coin nearestCoin = null;
		float bestDistance = Float.MAX_VALUE;
		Coin toRemove = null;
		for(Coin coin : m_coins) {
			float distance = coin.distanceTo(collector);
			if(nearestCoin == null || distance < bestDistance) {
				bestDistance = distance;
				nearestCoin = coin;
			}
			if(!collector.hasCoin() && distance < 10) {
				if (m_useCenterDropOff) {
					collector.setHasCoin(true);
					toRemove = coin;
				} else {
					coin.setX((int) (Math.random() * this.getWidth()));
					coin.setY((int) (Math.random() * this.getHeight()));
				}
				collector.adjustFitness(0.3f);
				break;
			}
		}
		if(toRemove != null) m_coins.remove(toRemove);
		return nearestCoin;
	}

	private float round(float f) {
		return Math.round(f * 100f) / 100f;
	}

	private void saveAgentsToFile() {
		System.out.println("Saving agents to disk!");
		Utils.saveAgentsToDisk(m_generations, m_collectors, getSaveFileName());
	}

	private void initializeNewCollectors(List<Collector> collectors) {
		for(int i = 0; i < 20; i++) {
			Collector collector = new Collector(getInputNeuronCount());
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

	private void breedNextGeneration() {
		BinaryGeneticAlgorithm.Breeder<Collector> ga = new BinaryGeneticAlgorithm.Breeder<>(m_collectors);
		float sumFitness = ga.getFitnessSum();
		float averageFitness = sumFitness / m_collectors.size();
		if (m_saveHighFitnessAgents && ga.getPeakFitness() >= m_highFitnessThreshold) {
			String filename = "saved_collector " + round(ga.getPeakFitness()) + " " + System.currentTimeMillis() + ".txt";
			System.out.println("Found network with fitness >= 10! Saving to " + filename);
			Utils.saveNetworkToDisk(ga.getPeakAgent().getNetwork(), filename);
		}
		System.out.println(new Date() + "\t" + round(averageFitness) + "\t" + round(ga.getPeakFitness()));
		List<Network> nextGenNetworks = ga.breed();
		ArrayList<Collector> nextGen = new ArrayList<>();
		for(Network n : nextGenNetworks) {
			Collector c = new Collector(n, (float) Math.random() * this.getWidth(), (float) Math.random() * this.getHeight());
			nextGen.add(c);
		}
		m_collectors = nextGen;

		m_coins.clear();
		for(int i = 0; i < 50; i++) {
			Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
			m_coins.add(coin);
		}
	}
}
