package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.gamecore.Agent;

public class Collector extends Agent {
	
	/*Structure:
	 * Input Nodes:
	 * Angle of nearest coin relative to direction facing
	 * Angle of center relative to direction facing
	 * Carrying coin?
	 * 
	 * Output Nodes:
	 * Turn
	 * Forward/Backward
	 * */
	
	private boolean hasCoin = false;
	private float angle;
	
	public Collector() {
		super(3, 2, 4, 2);
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
		return hasCoin;
	}
	
	public void setHasCoin(boolean value) {
		this.hasCoin = value;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void adjustAngle(float adjustment) {
		angle += adjustment;
	}
}
