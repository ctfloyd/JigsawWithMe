import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;

public class JigsawPieceFactory {

	BufferedImage image;
	BufferedImage[] pieceTextures;
	
	static final int WIDTH = 2;
	static final int HEIGHT = 2;
	
	public JigsawPieceFactory(String imagePath) {
		image = null;
		try {
			image = ImageIO.read(new File(imagePath));
		} catch (Exception e) {
			// TODO: revisit
			System.err.println("Could not load image from file : " + imagePath);
			e.printStackTrace();
		}
		pieceTextures = new BufferedImage[WIDTH * HEIGHT];
		splitImage();
	}
	
	public void splitImage() {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int pieceWidth = imageWidth / WIDTH;
		int pieceHeight = imageHeight / HEIGHT;
		for(int i = 0; i < HEIGHT; i++) {
			for(int j = 0; j < WIDTH; j++) {
				int textureIndex = i * HEIGHT + j;
				BufferedImage pieceTexture = new BufferedImage(pieceWidth, pieceHeight, image.getType());
				Raster crop = image.getRaster().createChild(j * pieceWidth, i * pieceHeight, pieceWidth, pieceHeight, 0, 0, null);
				pieceTexture.setData(crop);
				pieceTextures[textureIndex] = pieceTexture;
			}
		}
	}
	
	public BufferedImage[] getPieceTextures() {
		return pieceTextures;
	}
	
	
	public static class ImageCanvas extends Canvas {

		private static final long serialVersionUID = 1L;
		BufferedImage[] img;
		Random r;
		
		public ImageCanvas(BufferedImage[] images) {
			img = images;
			r = new Random();
		}
		
		
		public void paint(Graphics g) {
			for(int i = 0; i < img.length; i++) {
				int x = r.nextInt(500);
				int y = r.nextInt(500);
				g.drawImage(img[i], x, y, null);
			}
		}
	}
	
	public static void main(String[] args) {
		
		JigsawPieceFactory factory = new JigsawPieceFactory("test.jpg");
		BufferedImage[] pieces = factory.getPieceTextures();
		
		Frame frame = new Frame();
		Canvas canvas = new ImageCanvas(pieces);
		
		frame.add(canvas);
		
		frame.setSize(500, 500);
		frame.setLayout(new BoxLayout(frame, BoxLayout.X_AXIS));
		frame.setVisible(true);
	}
}
