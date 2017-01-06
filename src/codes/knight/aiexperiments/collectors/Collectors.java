package codes.knight.aiexperiments.collectors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Collectors extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private Thread thread;
	private boolean running = true;
	BufferedImage backbuffer;

	private ArrayList<Collector> collectors;
	private ArrayList<Coin> coins;
	private Center center;
	private long tickCount = 0;

	public Collectors() {
		this.setSize(800, 600);
		this.setTitle("Collector Test");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		collectors = new ArrayList<Collector>();
		coins = new ArrayList<Coin>();
		center = new Center(this.getWidth() / 2, this.getHeight() / 2);

		for(int i = 0; i < 20; i++) {
			Collector collector = new Collector();
			collector.setX((int) (Math.random() * this.getWidth()));
			collector.setY((int) (Math.random() * this.getHeight()));
			collectors.add(collector);
		}

		for(int i = 0; i < 50; i++) {
			Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
			coins.add(coin);
		}

		while(running) {
			tick();
			draw();
			tickCount++;
		}
	}

	private void tick() {
		for(Collector collector : collectors) {
			Coin nearestCoin = null;
			if(collector.hasCoin()) {
				if(collector.distanceTo(center) < 10) {
					collector.setHasCoin(false);
					collector.adjustFitness(1f);
					Coin coin = new Coin((int) (Math.random() * this.getWidth()), (int) (Math.random() * this.getHeight()));
					coins.add(coin);
					nearestCoin = findNearestCoin(collector);
				}
			} else {
				nearestCoin = findNearestCoin(collector);
			}
			
			float[] networkInput = new float[] {
					collector.hasCoin() ? 0 : (float) Math.atan2(collector.getX() - nearestCoin.getX(), collector.getY() - nearestCoin.getY()),
					(float) Math.atan2(collector.getX() - center.getX(), collector.getY() - center.getY()),
					collector.hasCoin() ? 1 : 0
			};
			
			float[] output = collector.feed(networkInput);
			float angleAdjustment = (output[0] - .5f) * 0.1f;
			collector.adjustAngle(angleAdjustment);
			float dX = (float) Math.cos(collector.getAngle());
			float dY = (float) Math.sin(collector.getAngle());
			collector.move(dX, dY);
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
		Graphics bbg = backbuffer.getGraphics();
		bbg.clearRect(0, 0, getWidth(), getHeight());

		bbg.setColor(Color.WHITE);
		for(Collector collector : collectors) {
			float xShift = 5;
			float yShift = 5;
			float cornerAngle = 90;
			float angle = collector.getAngle();
			float x = collector.getX();
			float y = collector.getY();
			Point a = new Point(), b = new Point(), c = new Point(), d = new Point();
			
			a.setLocation(x + xShift * Math.cos(cornerAngle + angle), y + yShift * Math.sin(cornerAngle + angle));
			d.setLocation(x + xShift * Math.cos(cornerAngle + angle + Math.PI/2), y + yShift * Math.sin(cornerAngle + angle + Math.PI/2));
			c.setLocation(x + xShift * Math.cos(cornerAngle + angle + Math.PI), y + yShift * Math.sin(cornerAngle + angle + Math.PI));
			b.setLocation(x + xShift * Math.cos(cornerAngle + angle + 3*(Math.PI/2)), y + yShift * Math.sin(cornerAngle + angle + (Math.PI/2) * 3));
			
			bbg.drawLine(a.x, a.y, b.x, b.y);
			bbg.drawLine(b.x, b.y, c.x, c.y);
			bbg.drawLine(c.x, c.y, d.x, d.y);
			bbg.drawLine(d.x, d.y, a.x, a.y);
			
			if(collector.hasCoin()) {
				bbg.fillOval((int) (x - xShift), (int) (y - yShift), 10, 10);
			} else {
				Coin coin = findNearestCoin(collector);
				bbg.drawLine(coin.getX(), coin.getY(), collector.getX(), collector.getY());
			}
			
		}
		
		bbg.setColor(Color.YELLOW);
		for(Coin coin : coins) {
			bbg.fillOval(coin.getX(), coin.getY(), 10, 10);
		}
		
		bbg.setColor(Color.GREEN);
		bbg.drawOval(center.getX(), center.getY(), 20, 20);
		
		g.drawImage(backbuffer, 0, 0, this);
	}
	
	private Coin findNearestCoin(Collector collector) {
		Coin nearestCoin = null;
		float bestDistance = Float.MAX_VALUE;
		Coin toRemove = null;
		for(Coin coin : coins) {
			float distance = coin.distanceTo(collector);
			if(nearestCoin == null || distance < bestDistance) {
				bestDistance = distance;
				nearestCoin = coin;
			}
			if(distance < 10) {
				collector.setHasCoin(true);
				collector.adjustFitness(0.1f);
				toRemove = coin;
			}
		}
		if(toRemove != null) coins.remove(toRemove);
		return nearestCoin;
	}

}
