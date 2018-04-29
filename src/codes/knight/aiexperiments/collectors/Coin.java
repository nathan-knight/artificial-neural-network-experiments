package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.gamecore.GameObject;

import java.awt.*;

public class Coin extends GameObject {
	
	public Coin(int x, int y) {
		super(x, y);
	}

	@Override
	public void draw(Graphics2D bbg) {
		bbg.setColor(Color.YELLOW);
		bbg.fillOval(getX() - 5, getY() - 5, 10, 10);
	}
}
