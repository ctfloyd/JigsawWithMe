import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Random;

public class JigsawTexture {

	private BufferedImage texture;
	private int effectiveWidth;
	private int effectiveHeight;
	private double scalar;
	
	// Jigsaw Textures are purposefully oversized, such that when adding latches
	// they have 'wiggle room' to make the puzzle fit together in a meaninful way.
	// Thus we need this scalar factor to compute effective centers of images.
	public JigsawTexture(BufferedImage image, double scalar) {
		texture = image;
		effectiveWidth = (int)(image.getWidth() / scalar);
		effectiveHeight = (int)(image.getHeight() / scalar);
		this.scalar = scalar;
		System.out.println("Effective Dimensions: " + effectiveWidth + "x" + effectiveHeight);
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
			points[i + 1] = pair;
		}
		return points;
	}
	
	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	
	public Raster computeNotchData(JigsawPiece.Side side, int radius) {
		WritableRaster canvas = texture.getData().createCompatibleWritableRaster();
		texture.copyData(canvas);
		
		int widthDifference = texture.getWidth() - effectiveWidth;
		Point center = new Point(effectiveWidth + radius, texture.getHeight() / 2);
		
		int perimeterGranularity = 300;
		Point2D[] circle = getCirclePoints(center, radius, perimeterGranularity);
		int maxX = 0;
		for(int i = circle.length - 1; i > 0; i -= 2) {
			int clampedX = clamp((int)circle[i - 1].getX(), 0, texture.getWidth() - 1);
			int clampedY = clamp((int)circle[i - 1].getY(), 0, texture.getHeight());
			int clampedY2 = clamp((int)circle[i].getY(), 0, texture.getHeight());
			int deltaY = clampedY - clampedY2;
			if(clampedX > effectiveWidth + radius / (1 / scalar)) continue;
			if(clampedX > maxX)
				maxX = clampedX;
			
			int[] data = new int[deltaY * 4];
			texture.getRaster().getPixels(clampedX, clampedY2, 1, (int)deltaY, data);
			for(int j = 0; j < data.length; j++) {
				if(j % 4 == 3) {
					data[j] = 0;
				}
			}
			canvas.setPixels(clampedX, clampedY2, 1, (int)deltaY, data);
		}
		int[] data = new int[maxX * texture.getWidth() * texture.getHeight() * 4];
		for(int j = 0; j < data.length; j++) {
			data[j] = 0;
		}
		canvas.setPixels(maxX, 0, texture.getWidth() - maxX, texture.getHeight(), data);
		
		return canvas;
	}
	
	private Raster computeLatchData(JigsawPiece.Side side, int radius) {
		WritableRaster canvas = texture.getData().createCompatibleWritableRaster();
		texture.copyData(canvas);
		
		Point center = new Point(texture.getWidth() - radius, texture.getHeight() / 2);
		int perimeterGranularity = 300;
		Point2D[] circle = getCirclePoints(center, radius, perimeterGranularity);
		
		int maxX = 0;
		for(int i = 0; i < circle.length; i += 2) {
			int clampedX = clamp((int)circle[i].getX(), 0, texture.getWidth() - 1);
			if(clampedX > maxX)
				maxX = clampedX;
			if(i > perimeterGranularity - perimeterGranularity / 3) continue;
			int clampedY = clamp((int)circle[i].getY(), 0, texture.getHeight());
			int clampedY2 = clamp((int)circle[i + 1].getY(), 0, texture.getHeight());
			int deltaY = texture.getHeight()  - clampedY;
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
			canvas.setPixels(clampedX, 0, 1, clampedY2, data);
			canvas.setPixels(clampedX, clampedY, 1, deltaY, data2);
		}
		return canvas;
	}
	
	private void rotateTexture(double radians) {
		AffineTransform rotation = new AffineTransform();
		rotation.translate(texture.getWidth() / 2, texture.getHeight() / 2);
		rotation.rotate(radians);
		rotation.translate(-texture.getWidth() / 2, -texture.getHeight() / 2);
		AffineTransformOp operation = new AffineTransformOp(rotation, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage dst = new BufferedImage(texture.getWidth(), texture.getHeight(), texture.getType());
		operation.filter(texture, dst);
		texture.setData(dst.getData());
	}
	
	public void addLatch(JigsawPiece.Side side, int radius) {
		switch(side) {
		case Left:
			rotateTexture(Math.PI);
			texture.setData(computeLatchData(side, radius));
			rotateTexture(-Math.PI);
			break;
		case Right:
			texture.setData(computeLatchData(side, radius));
			break;
		case Top:
			rotateTexture(Math.PI / 2.0);
			texture.setData(computeLatchData(side, radius));
			rotateTexture(-Math.PI / 2.0);
			break;
		case Bottom:
			rotateTexture(-Math.PI / 2.0);
			texture.setData(computeLatchData(side, radius));
			rotateTexture(Math.PI / 2.0);
			break;
		default:
			return;
		}
	}
	
	public void addNotch(JigsawPiece.Side side, int radius) {
		switch(side) {
		case Left:
			rotateTexture(Math.PI);
			texture.setData(computeNotchData(side, radius));
			rotateTexture(-Math.PI);
			break;
		case Right:
			texture.setData(computeNotchData(side, radius));
			break;
		case Top:
			rotateTexture(Math.PI / 2.0);
			texture.setData(computeNotchData(side, radius));
			rotateTexture(-Math.PI / 2.0);
			break;
		case Bottom:
			rotateTexture(-Math.PI / 2.0);
			texture.setData(computeNotchData(side, radius));
			rotateTexture(Math.PI / 2.0);
			break;
		default:
			return;
		}
	}
	
	public void paint(Graphics g, int x, int y) {
//		Random r = new Random();
//		x = r.nextInt(700);
//		y = r.nextInt(700);
		g.drawImage(texture, x, y, null);
	}
	
}
