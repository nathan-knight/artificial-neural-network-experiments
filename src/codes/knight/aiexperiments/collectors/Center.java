package codes.knight.aiexperiments.collectors;

import codes.knight.aiexperiments.gamecore.GameObject;

import java.awt.*;

public class Center extends GameObject {

	public Center(int x, int y) {
		super(x, y);
	}

	@Override
	public void draw(Graphics2D bbg) {
		bbg.setColor(Color.GREEN);
		bbg.drawOval(getX() - 10, getY() - 10, 20, 20);
	}
}
