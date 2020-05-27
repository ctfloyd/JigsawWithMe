import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;

public class JigsawPacket {
	
	JigsawPiece piece;
	
	public JigsawPacket(JigsawPiece p) {
		piece = p;
	}
	
	public byte[] toByteArray() {
		String texturePath = "game/0/textures" + piece.getId() + ".png";
		
		// TODO: size match this;
		ByteBuffer bytes = ByteBuffer.allocate(32);
		bytes.putShort(piece.getId());
		Rectangle2D.Float rect = piece.getRect();
		float topLeftX = (float) rect.getX();
		float topLeftY = (float) rect.getY();
		float rectWidth = (float) rect.getWidth();
		float rectHeight = (float) rect.getHeight();
		bytes.putFloat(topLeftX);
		bytes.putFloat(topLeftY);
		bytes.putFloat(rectWidth);
		bytes.putFloat(rectHeight);
		bytes.put((byte)texturePath.length());
		bytes.put(texturePath.getBytes());
		// TODO: transmit notch information
		
		return bytes.array();
	}
	
}
