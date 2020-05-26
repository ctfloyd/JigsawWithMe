import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

public class JigsawPiece {

	enum Side {
		Top, Right, Bottom, Left
	};

	private short id;
	private Point2D.Float position;
	private JigsawTexture texture;
	private EnumMap<Side, Boolean> latched;
	private EnumMap<Side, Boolean> notched;
	private EnumMap<Side, JigsawPiece> neighbors;

	// Assumes a scaled image to account for latches and notches, sets unused pixels
	// transparency to 0;
	public JigsawPiece(Point2D.Float position, BufferedImage image, short id) {
		this.position = position;
		this.id = id;
		texture = new JigsawTexture(image, 1.5);
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
	
	public JigsawTexture getTexture() {
		return texture;
	}
	
	public short getId() {
		return id;
	}
	
	public Point2D.Float getPosition() {
		return position;
	}

	public void addLatch(Side side) {
		if (latched.get(side) != null)
			return;

		latched.put(side, true);
		notched.put(side, false);

		texture.addLatch(side, 20);
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

		texture.addNotch(side, 20);

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
}
