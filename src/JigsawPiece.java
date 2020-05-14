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
	private BufferedImage image;
	private EnumMap<Side, Boolean> latched;
	private EnumMap<Side, Boolean> notched;
	private EnumMap<Side, JigsawPiece> neighbors;

	// Assumes a scaled image to account for latches and notches, sets unused pixels transparency to 0;
	public JigsawPiece(Point position, BufferedImage image) {
		this.position = position;
		this.image = image;
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

		JigsawPiece neighbor = neighbors.get(side);
		if(neighbor == null)
			return;
		
		neighbor.getNotched().put(computeOppositeSide(side), true);
		neighbor.getLatched().put(computeOppositeSide(side), false);

	}

	public void addNotch(Side side) {
		if (notched.get(side) != null)
			return;
		
		notched.put(side, true);
		latched.put(side, false);

		JigsawPiece neighbor = neighbors.get(side);
		if(neighbor == null)
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
	
	private Point computeCenterOfSideWithOffset(Side side, int offset) {
		System.out.println("Image height: " + image.getHeight());
		switch(side) {
		case Right:
			return new Point(image.getWidth() - offset, image.getHeight() / 2);
		case Left:
			return new Point(offset, image.getHeight() / 2);
		case Top:
			return new Point(image.getWidth() / 2, offset);
		case Bottom:
			return new Point(image.getWidth() / 2, image.getHeight() - offset);
		default:
			return null;
		}
	}
	
	private Point[] getCirclePoints(Point center, double radius, int numPoints) {
		Point[] points = new Point[numPoints];
		for(int i = 0; i < numPoints; i += 2) {
			Point p = new Point();
			p.x = (int)(Math.round(Math.cos(Math.PI / numPoints * i) * radius) + center.x);
			p.y = (int)(Math.round(Math.sin(Math.PI / numPoints * i) * radius) + center.y);
			points[i] = p;
			Point pair = new Point();
			pair.x = p.x;
			pair.y = center.y - (p.y - center.y);
//			System.out.println("pair.y: " + pair.y + " p.y" + p.y);
			points[i + 1] = pair;
		}
		return points;
	}
	
	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	public void paint(Graphics g) {
		BufferedImage toPaint = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		WritableRaster paintData = image.getData().createCompatibleWritableRaster();
		image.copyData(paintData);
		for(Side s : Side.values()) {
			if(notched.get(s) != null && notched.get(s)) {
				Point center = computeCenterOfSideWithOffset(s, 20);
				Point2D[] circle = getCirclePoints(center, 30, 300);
				for(int i = 0; i < circle.length; i += 2) {
					int clampedX = clamp((int)circle[i].getX(), 0, toPaint.getWidth());
					int clampedY = clamp((int)circle[i].getY(), 0, toPaint.getHeight());
					int clampedY2 = clamp((int)circle[i + 1].getY(), 0, toPaint.getHeight());
					int deltaY = clampedY - clampedY2;
					int[] data = new int[deltaY * 4];
					image.getRaster().getPixels(clampedX, clampedY2, 1, (int)deltaY, data);
					for(int j = 0; j < data.length; j++) {
						if(j % 4 == 3) {
							data[j] = 0;
						}
					}
					//System.out.println("Clamped X: " + clampedX + " Clamped Y:" + clampedY + " Clamped Y2:" + clampedY2 + " maxWidth: " + (toPaint.getWidth() - 1) + " maxHeight: " + (toPaint.getHeight() - 1));
					paintData.setPixels(clampedX, clampedY2, 1, (int)deltaY, data);
				}
			}
			if(latched.get(s) != null && latched.get(s)) {
				Point center = computeCenterOfSideWithOffset(s, 30);
				Point2D[] circle = getCirclePoints(center, 30, 300);
				int maxX = 0;
				for(int i = 0; i < circle.length; i += 2) {
					int clampedX = clamp((int)circle[i].getX(), 0, toPaint.getWidth() - 1);
					if(clampedX > maxX)
						maxX = clampedX;
					System.out.println(clampedX);
					if(i > 200) continue;
					int clampedY = clamp((int)circle[i].getY(), 0, toPaint.getHeight());
					int clampedY2 = clamp((int)circle[i + 1].getY(), 0, toPaint.getHeight());
					int deltaY = image.getHeight()  - clampedY;
					int[] data = new int[clampedY2 * 4];
					for(int j = 0; j < data.length; j++) {
						if(j % 4 == 0)
							data[j] = 0;
					}
					int[] data2 = new int[deltaY * 4];
					for(int j = 0; j < data2.length; j++) {
						if(j % 4 == 0)
							data2[j] = 0;
					}
//					System.out.println("Clamped X: " + clampedX + " Clamped Y:" + clampedY + " Clamped Y2:" + clampedY2 + " maxWidth: " + (toPaint.getWidth() - 1) + " maxHeight: " + (toPaint.getHeight() - 1));
					paintData.setPixels(clampedX, 0, 1, clampedY2, data);
					paintData.setPixels(clampedX, clampedY, 1, deltaY, data2);
					
				}
//				int[] clearData = new int[(image.getWidth() - maxX) * image.getHeight() * 3];
//				for(int i = 0; i < clearData.length; i++) {
//					clearData[i] = 255;
//				}
//				paintData.setPixels(maxX, 0, image.getWidth() - maxX, image.getHeight(), clearData);
				
			}
		}
		toPaint.setData(paintData);
		int x = (int) position.getX();
		int y = (int) position.getY();
		g.drawImage(toPaint, x, y, null);
	}
}
