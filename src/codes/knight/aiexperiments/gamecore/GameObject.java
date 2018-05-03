package codes.knight.aiexperiments.gamecore;

import java.awt.*;
import java.util.List;

public abstract class GameObject {
	
	protected float x, y;
	
	public GameObject(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public abstract void draw(Graphics2D bbg);
	
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

	public static <T extends GameObject> T findNearest(GameObject object, List<T> searchObjects) {
		T nearest = null;
		float bestDistance = Float.MAX_VALUE;
		for (T obj : searchObjects) {
			if (obj == object) continue;
			float distance = object.distanceTo(obj);
			if (distance < bestDistance) {
				nearest = obj;
				bestDistance = distance;
			}
		}
		return nearest;
	}

	public <T extends GameObject> T findNearest(List<T> searchObjects) {
		return findNearest(this, searchObjects);
	}

}
