import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import javax.imageio.ImageIO;

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
				Point2D.Float position = new Point2D.Float(j * pieceWidth / (float)imageWidth, i * pieceHeight / (float)imageHeight);
				float rectWidth = pieceWidth / (float) imageWidth;
				float rectHeight = pieceHeight / (float) imageHeight;
				Rectangle2D.Float r = new Rectangle2D.Float((float)position.getX(), (float) position.getY(), rectWidth, rectHeight);
				pieces[textureIndex] = new JigsawPiece(r, pieceTexture, textureIndex);
			}
		}
	}

	public JigsawPiece[] getPieces() {
		return pieces;
	}
}
