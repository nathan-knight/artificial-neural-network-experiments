package codes.knight.aiexperiments.collectors;

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
		super(3, 5, 5, 2);
	}
	
	public Collector(int x, int y) {
		super(x, y);
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
