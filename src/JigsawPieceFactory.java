import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;

public class JigsawPieceFactory {

	BufferedImage image;
	JigsawPiece[] pieces;

	static final int WIDTH = 3;
	static final int HEIGHT = 3;

	public JigsawPieceFactory(String imagePath) {
		image = null;
		try {
			image = ImageIO.read(new File(imagePath));
		} catch (Exception e) {
			// TODO: revisit
			System.err.println("Could not load image from file : " + imagePath);
			e.printStackTrace();
		}
		System.out.println("Image Type: " + image.getType());
		pieces = new JigsawPiece[WIDTH * HEIGHT];
		splitImage();
	}

	public void splitImage() {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int pieceWidth = imageWidth / WIDTH;
		int pieceHeight = imageHeight / HEIGHT;
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				short textureIndex = (short) (i * HEIGHT + j);
				BufferedImage pieceTexture = new BufferedImage(pieceWidth, pieceHeight, image.getType());
				int width = (int) (j * pieceWidth + (int) (pieceWidth * 1.5) > image.getWidth() ? pieceWidth
						: pieceWidth * 1.5);
				int height = (int) (i * pieceHeight + (int) (pieceHeight * 1.5) > image.getHeight() ? pieceHeight
						: pieceHeight * 1.5);
				int startX = (int) (j * pieceWidth - pieceWidth * 0.25 > 0 ? j * pieceWidth - pieceWidth * 0.25 : 0);
				int startY = (int) (i * pieceHeight - pieceHeight * 0.25 > 0 ? i * pieceWidth - pieceWidth * 0.25 : 0);
				
				
				Raster crop = image.getRaster().createChild(startX, startY, width, height, 0, 0, null);
				pieceTexture.setData(crop);
				pieces[textureIndex] = new JigsawPiece(new Point(j * pieceWidth, i * pieceHeight), pieceTexture, textureIndex);
			}
		}
	}

	public JigsawPiece[] getPieces() {
		return pieces;
	}

	public static class JigsawCanvas extends Canvas {

		private static final long serialVersionUID = 1L;
		JigsawPiece[] pieces;
		Random r;

		public JigsawCanvas(JigsawPiece[] pieces) {
			this.pieces = pieces;
			r = new Random();
		}

		public void paint(Graphics g) {
//			for (int i = 0; i < pieces.length; i++)
//				pieces[i].paint(g);
			pieces[4].paint(g);
			pieces[5].paint(g);
		}
	}

	public static void main(String[] args) {

		JigsawPieceFactory factory = new JigsawPieceFactory("test.png");
		JigsawPiece[] pieces = factory.getPieces();
		pieces[4].addLatch(JigsawPiece.Side.Right);
		pieces[4].addLatch(JigsawPiece.Side.Bottom);
		pieces[4].addLatch(JigsawPiece.Side.Top);
		pieces[4].addLatch(JigsawPiece.Side.Left);
		pieces[5].addNotch(JigsawPiece.Side.Left);
		pieces[5].addNotch(JigsawPiece.Side.Right);
		pieces[5].addNotch(JigsawPiece.Side.Bottom);
		pieces[5].addNotch(JigsawPiece.Side.Top);

		Frame frame = new Frame();
		Canvas canvas = new JigsawCanvas(pieces);
		canvas.setBackground(Color.WHITE);
		
		frame.add(canvas);
		frame.setBackground(Color.MAGENTA);
		frame.setSize(1024, 1024);
		frame.setLayout(new BoxLayout(frame, BoxLayout.X_AXIS));
		frame.setVisible(true);
	}
}
