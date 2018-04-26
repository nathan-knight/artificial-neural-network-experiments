package codes.knight.aiexperiments.collectors;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import codes.knight.aiexperiments.BinaryGeneticAlgorithm;
import codes.knight.aiexperiments.GeneticAlgorithm;
import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.Utils;

public class Collectors extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private static final String SAVED_AGENT_FILE = "Saves/agents_collectors_basic.txt";

	private Thread thread;
	private boolean running = true;
	private BufferedImage backbuffer;

	private ArrayList<Collector> collectors;
	private ArrayList<Coin> coins;
	private Center center;
	private long tickCount = 1;
	private int ticksPerGeneration = 20000;
	private int generationsPerAutosave = 10;
	private int generations = 0;

	private int frameRateCap = 60;
	private int framesThisSecond = 0;
	private boolean speedmode = false;

	public Collectors() {
		this.setSize(800, 600);
		this.setTitle("Collector Test");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_Q) {
					System.out.println("Saving and quitting! Filename: " + SAVED_AGENT_FILE);
					Utils.saveAgentsToDisk(generations, collectors, SAVED_AGENT_FILE);
					System.exit(0);
				}
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					speedmode = !speedmode;
				}
				if(arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
					for(Collector c : collectors) {
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
		this.setVisible(true);
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		collectors = new ArrayList<>();
		coins = new ArrayList<>();
		center = new Center(this.getWidth() / 2, this.getHeight() / 2);

		File savedAgents = new File(SAVED_AGENT_FILE);

		if (savedAgents.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(SAVED_AGENT_FILE))) {
				String line = br.readLine();
				generations = Integer.valueOf(line);
				while ((line = br.readLine()) != null) {
					collectors.add(new Collector(line,
							(float) Math.random() * this.getWidth(),
							(float) Math.random() * this.getHeight()));
				}
				System.out.println("Loaded " + collectors.size() + " collectors from disk");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			for(int i = 0; i < 20; i++) {
				Collector collector = new Collector();
				collector.setX((int) (Math.random() * this.getWidth()));
				collector.setY((int) (Math.random() * this.getHeight()));
				collectors.add(collector);
			}
		}

		for(int i = 0; i < 50; i++) {
			Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
			coins.add(coin);
		}
		
		long lastTime = System.currentTimeMillis();
//		long delta = System.currentTimeMillis() - lastTime;
		long time = System.currentTimeMillis();
		long lastSecond = time / 1000;

		while(running) {
//			delta = System.currentTimeMillis() - lastTime;
			if (tickCount % (generationsPerAutosave * ticksPerGeneration) == 0) {
				saveAgentsToFile();
			}
			if(tickCount % ticksPerGeneration == 0) {
				generations++;
				//GeneticAlgorithm<Collector> ga = new GeneticAlgorithm<Collector>(collectors);
				BinaryGeneticAlgorithm.Breeder<Collector> ga = new BinaryGeneticAlgorithm.Breeder<>(collectors);
				float sumFitness = ga.getFitnessSum();
				float averageFitness = sumFitness / collectors.size();
				/*if (ga.getPeakFitness() >= 20) {
					String filename = "saved_collector " + round(ga.getPeakFitness()) + " " + System.currentTimeMillis() + ".txt";
					System.out.println("Found network with fitness >= 10! Saving to " + filename);
					Utils.saveNetworkToDisk(ga.getPeakAgent().getNetwork(), filename);
				}*/
				// System.out.println("Evolving! Average fitness: " + averageFitness + ", peak fitness: " + ga.getPeakFitness());
				System.out.println(new Date() + "\t" + round(averageFitness) + "\t" + round(ga.getPeakFitness()));
				List<Network> nextGenNetworks = ga.breed();
				ArrayList<Collector> nextGen = new ArrayList<>();
				for(Network n : nextGenNetworks) {
					Collector c = new Collector(n, (float) Math.random() * this.getWidth(), (float) Math.random() * this.getHeight());
					nextGen.add(c);
				}
				collectors = nextGen;
				
				coins.clear();
				for(int i = 0; i < 50; i++) {
					Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
					coins.add(coin);
				}
			}
			tick();
			if(!speedmode) draw();
			framesThisSecond++;
			tickCount++;
			time = (long) ((1000 / frameRateCap) - (System.currentTimeMillis() - lastTime));
			lastTime = System.currentTimeMillis();
			if(lastSecond - lastTime / 1000 < 0) {
				lastSecond = lastTime / 1000;
				this.setTitle("Collector Test :: Generation: " + generations + " :: " + framesThisSecond + " FPS");//, APEX: " + pop.getFittest());
				framesThisSecond = 0;
			}
			if (time > 0) { 
				try {
					if(!speedmode) Thread.sleep(time); 
				} 
				catch(Exception e){} 
			}
		}
	}

	private void tick() {
		center.setX(this.getWidth() / 2);
		center.setY(this.getHeight() / 2);
		for(Collector collector : collectors) {
			
			//Time fitness cost:
			collector.adjustFitness(-1/ticksPerGeneration);
			
			Coin nearestCoin = findNearestCoin(collector);
			if(collector.hasCoin()) {
				if(collector.distanceTo(center) < 10) {
					collector.setHasCoin(false);
					collector.adjustFitness(1f);
					Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
					coins.add(coin);
					nearestCoin = findNearestCoin(collector);
				}
			}
			float angleToCoin = nearestCoin == null ? 0 : (float) ((Math.atan2(collector.getY() - nearestCoin.getY(), collector.getX() - nearestCoin.getX()) - collector.getAngle() - Math.PI));
			if (angleToCoin < -Math.PI) angleToCoin += 2 * Math.PI;
			float[] networkInput = new float[] {
					angleToCoin
			};
			
			float[] output = collector.feed(networkInput);
			float angleAdjustment = (output[0] - 0.5f) * 0.3f;
			collector.adjustAngle(angleAdjustment);
			float dX = (float) Math.cos(collector.getAngle()) * output[1];
			float dY = (float) Math.sin(collector.getAngle()) * output[1];
			collector.move(dX, dY);

			collector.setColor(new Color((int)(output[2] * 16777216)));
			
			//Keep in boundaries
			if(collector.getX() < 0) collector.setX(0);
			if(collector.getY() < 0) collector.setY(0);
			if(collector.getX() > this.getWidth()) collector.setX(this.getWidth());
			if(collector.getY() > this.getHeight()) collector.setY(this.getHeight());
		}
	}

	private void draw() {
		if(backbuffer == null) backbuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		if(backbuffer.getWidth() != getWidth() || backbuffer.getHeight() != getHeight()) backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = getGraphics();
		Graphics2D bbg = (Graphics2D) backbuffer.getGraphics();
		bbg.clearRect(0, 0, getWidth(), getHeight());

		for(Collector collector : collectors) {
			float xShift = 5;
			float yShift = 5;
			int size = 10;
			float cornerAngle = 90;
			float angle = collector.getAngle();
			float x = collector.getX();
			float y = collector.getY();

			bbg.setColor(Color.WHITE);
			bbg.drawLine(collector.getX(), collector.getY(),
					(int)(x + 10 * Math.cos(angle)),
					(int)(y + 10 * Math.sin(angle)));

			if(collector.hasCoin()) {
				bbg.fillOval((int) (x - xShift), (int) (y - yShift), 10, 10);
			} else {
				// Draw a line to the nearest coin
				Coin coin = findNearestCoin(collector);
				bbg.drawLine(coin.getX(), coin.getY(), collector.getX(), collector.getY());


				// Draw the angle to the coin
				/*float angleToCoin = (float) (Math.toDegrees(Math.atan2(collector.getY() - coin.getY(), collector.getX() - coin.getX()) - angle - Math.PI));
				if (angleToCoin < -180) angleToCoin += 360f;
				bbg.drawString(
						String.valueOf(round(angleToCoin)),
						x - 15, y - 15);*/
			}

			bbg.setColor(collector.getColor());

			Rectangle collectorRect = new Rectangle(-size / 2, -size / 2, size, size);
			Path2D.Double rectPath = new Path2D.Double();
			rectPath.append(collectorRect, false);
			AffineTransform transform = new AffineTransform();
			transform.translate(x, y);
			transform.rotate(angle);
			rectPath.transform(transform);
			bbg.draw(rectPath);
		}
		
		bbg.setColor(Color.YELLOW);
		for(Coin coin : coins) {
			bbg.fillOval(coin.getX() - 5, coin.getY() - 5, 10, 10);
		}
		
		/*bbg.setColor(Color.GREEN);
		bbg.drawOval(center.getX() - 10, center.getY() - 10, 20, 20);*/
		
		g.drawImage(backbuffer, 0, 0, this);
	}
	
	private Coin findNearestCoin(Collector collector) {
		Coin nearestCoin = null;
		float bestDistance = Float.MAX_VALUE;
		//Coin toRemove = null;
		for(Coin coin : coins) {
			float distance = coin.distanceTo(collector);
			if(nearestCoin == null || distance < bestDistance) {
				bestDistance = distance;
				nearestCoin = coin;
			}
			if(!collector.hasCoin() && distance < 10) {
				//collector.setHasCoin(true);
				collector.adjustFitness(0.3f);
				coin.setX((int) (Math.random() * this.getWidth()));
				coin.setY((int) (Math.random() * this.getHeight()));
				//toRemove = coin;
				break;
			}
		}
		//if(toRemove != null) coins.remove(toRemove);
		return nearestCoin;
	}

	private float round(float f) {
		return Math.round(f * 100f) / 100f;
	}

	private void saveAgentsToFile() {
		System.out.println("Saving agents to disk!");
		Utils.saveAgentsToDisk(generations, collectors, SAVED_AGENT_FILE);
	}
}
