import FloatPoint from "./FloatPoint.js";

export default class Rectangle {
	
	// Top left corner
	topLeft: FloatPoint;
	// Bottom right corner
	bottomRight: FloatPoint;
	
	constructor(topLeft: FloatPoint, bottomRight: FloatPoint) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}
	
	getTopLeftCorner(): FloatPoint {
		return this.topLeft;
	}	
	
	getBottomRightCorner(): FloatPoint {
		return this.bottomRight;
	}
	
	intersects(other: Rectangle): boolean {
		if(this.getTopLeftCorner().x() >= other.getBottomRightCorner().x() ||
			other.getTopLeftCorner().x() >= this.getBottomRightCorner().x())
			return false;
		
		if(this.getTopLeftCorner().y() <= other.getBottomRightCorner().y() ||
			other.getTopLeftCorner().y() >= this.getBottomRightCorner().y())
			return false;
			
		return true;
	}
	
	intersectsPoint(point: FloatPoint) {
		if(this.getTopLeftCorner().x() > point.x() || this.getBottomRightCorner().x() < point.x())
			return false;
		
		if(this.getTopLeftCorner().y() > point.y() || this.getBottomRightCorner().y() < point.y())
			return false;
		
		return true;
	}
	
	translated(x: number, y: number): void {
		this.topLeft.translated(x, y);
		this.bottomRight.translated(x, y);
	}
	
	width() {
		return this.bottomRight.x() - this.topLeft.x();
	}
	
	height() {
		return this.bottomRight.y() - this.topLeft.y();
	}
	
}