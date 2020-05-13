import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class JigsawPiece {

	Point position;
	BufferedImage image;
	
	public JigsawPiece(Point position, BufferedImage image) {
		this.position = position;
		this.image = image;
	}
	
	
}
