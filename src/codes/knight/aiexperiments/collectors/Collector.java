package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.gamecore.Agent;

import java.awt.*;

public class Collector extends Agent {
	
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
	
	private boolean m_hasCoin = false;
	private Color m_color = Color.WHITE;
	private float m_angle;
	
	public Collector() {
		super(1, 1, 3, 3);
	}
	
	public Collector(int x, int y) {
		super(x, y);
	}
	
	public Collector(Network n, float x, float y) {
		super(n);
		setX(x);
		setY(y);
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
}
