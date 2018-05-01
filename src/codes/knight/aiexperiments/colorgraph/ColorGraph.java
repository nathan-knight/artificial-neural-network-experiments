package codes.knight.aiexperiments.colorgraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ColorGraph extends JFrame implements Runnable {

	public static final int TICKS_PER_GENERATION = 20000;
	private static final int GENERATIONS_PER_AUTOSAVE = 10;
	private static final int FRAME_RATE_CAP = 60;

	private Thread thread;

	private long m_tickCount = 1;
	private int m_generations = 0;

	private int m_framesThisSecond = 0;
	private int m_lastFPS = 0;

	private boolean m_running = true;
	private boolean m_speedmode = false;

	private int m_hiddenLayers = 1;
	private int m_neuronsPerHiddenLayer = 3;

	private List<Colorer> m_colorers;
	private BufferedImage m_backbuffer;

	public ColorGraph() {
		this.setSize(800, 600);
		this.setTitle("Color Graph Test");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.VK_ENTER) {
					m_speedmode = !m_speedmode;
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

	public ColorGraph start() {
		this.setVisible(true);
		thread = new Thread(this);
		thread.start();

		return this;
	}

	public ColorGraph setNumberOfHiddenLayers(int num) {
		m_hiddenLayers = num;
		return this;
	}

	public ColorGraph setNeuronsPerHiddenLayer(int num) {
		m_neuronsPerHiddenLayer = num;
		return this;
	}

	@Override
	public void run() {
		m_colorers = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			m_colorers.add(new Colorer(m_hiddenLayers, m_neuronsPerHiddenLayer));
		}

		long lastTime = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		long lastSecond = time / 1000;

		while (m_running) {
			if(m_tickCount % TICKS_PER_GENERATION == 0) {
				m_generations++;
				breedNextGeneration();
			}
			tick();
			if(!m_speedmode) draw();
			m_framesThisSecond++;
			m_tickCount++;
			time = (1000 / FRAME_RATE_CAP) - (System.currentTimeMillis() - lastTime);
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

	}

	private void draw() {
		if(m_backbuffer == null) m_backbuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		if(m_backbuffer.getWidth() != getWidth() || m_backbuffer.getHeight() != getHeight()) m_backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = getGraphics();
		Graphics2D bbg = (Graphics2D) m_backbuffer.getGraphics();

		Colorer colorer = m_colorers.get(0);
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				float rgb = colorer.feed(new float[] { x, y })[0];
				m_backbuffer.setRGB(x, y, (int) (rgb * 16777216f));
			}
		}

		g.drawImage(m_backbuffer, 0, 0, this);
	}

	private void breedNextGeneration() {

	}
}
