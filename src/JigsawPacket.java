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
		System.out.println("Got piece x at: " + piece.getPosition());
		bytes.putFloat((float)piece.getPosition().getX());
		bytes.putFloat((float)piece.getPosition().getY());
		bytes.put((byte)texturePath.length());
		bytes.put(texturePath.getBytes());
		// TODO: transmit notch information
		
		return bytes.array();
	}
	
}
