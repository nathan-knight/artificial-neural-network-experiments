package codes.knight.aiexperiments.gamecore;

public abstract class GameObject {
	
	protected float x, y;
	
	public GameObject(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public int getX() {
		return (int) x;
	}
	
	public int getY() {
		return (int) y;
	}
	
	public void move(float dX, float dY) {
		x += dX;
		y += dY;
	}
	
	public float distanceTo(GameObject go) {
		return (float) Math.sqrt(Math.pow(go.getX() - getX(), 2) + Math.pow(go.getY() - getY(), 2));
	}

}
