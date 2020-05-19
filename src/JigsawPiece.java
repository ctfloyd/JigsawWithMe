import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import javax.imageio.ImageIO;

public class JigsawPiece {

	enum Side {
		Top, Right, Bottom, Left
	};

	private Point position;
	private JigsawTexture texture;
	private EnumMap<Side, Boolean> latched;
	private EnumMap<Side, Boolean> notched;
	private EnumMap<Side, JigsawPiece> neighbors;

	// Assumes a scaled image to account for latches and notches, sets unused pixels
	// transparency to 0;
	public JigsawPiece(Point position, BufferedImage image) {
		this.position = position;
		texture = new JigsawTexture(image);
		latched = new EnumMap<>(Side.class);
		neighbors = new EnumMap<>(Side.class);
		notched = new EnumMap<>(Side.class);
	}

	public EnumMap<Side, Boolean> getLatched() {
		return latched;
	}

	public boolean isSideLatched(Side side) {
		return latched.get(side);
	}

	public EnumMap<Side, Boolean> getNotched() {
		return notched;
	}

	public boolean isSideNotched(Side side) {
		return notched.get(side);
	}

	public void addLatch(Side side) {
		if (latched.get(side) != null)
			return;

		latched.put(side, true);
		notched.put(side, false);

		texture.addLatch(side, 30);
		JigsawPiece neighbor = neighbors.get(side);
		if (neighbor == null)
			return;

		neighbor.getNotched().put(computeOppositeSide(side), true);
		neighbor.getLatched().put(computeOppositeSide(side), false);

	}

	public void addNotch(Side side) {
		if (notched.get(side) != null)
			return;

		notched.put(side, true);
		latched.put(side, false);

		texture.addNotch(side, 30);

		JigsawPiece neighbor = neighbors.get(side);
		if (neighbor == null)
			return;
		neighbor.getNotched().put(computeOppositeSide(side), false);
		neighbor.getLatched().put(computeOppositeSide(side), true);
	}

	private Side computeOppositeSide(Side side) {
		switch (side) {
		case Right:
			return Side.Left;
		case Left:
			return Side.Right;
		case Top:
			return Side.Bottom;
		case Bottom:
			return Side.Top;
		default:
			return null;
		}
	}

	public void paint(Graphics g) {
		int x = (int) position.getX();
		int y = (int) position.getY();
		texture.paint(g, x, y);
	}
}
