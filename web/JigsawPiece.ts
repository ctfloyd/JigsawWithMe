import FloatPoint from "./FloatPoint";
import Rectangle from "./Rectangle";

export class JigsawPiece {
	
	id: number;
	texturePath: string;
	websocket: WebSocket;
	canvas: HTMLCanvasElement;
	renderingContext: CanvasRenderingContext2D;
	bitmap: ImageBitmap;
	dirty_bitmap: boolean;
	boundingBox: Rectangle;
	
	constructor(id: number, boundingBox: Rectangle, texturePath: string, canvas: HTMLCanvasElement) {
		this.id = id;
		this.texturePath = texturePath;
		this.canvas = canvas;
		this.boundingBox = boundingBox;
		this.renderingContext = canvas.getContext('2d');
	}
	
	async generateBitmap(): Promise<void> {
		let createBitmapPromise = new Promise<HTMLImageElement>((resolve, reject) => {
			let image = new Image();
			image.src = this.texturePath;
			image.onload = () => resolve(image);
			image.onerror = reject;	
		});
		this.bitmap = await createImageBitmap(await createBitmapPromise);
	}
	
	async paint(): Promise<void> {
		if(!this.bitmap || this.dirty_bitmap)
			await this.generateBitmap();
		
		let center = this.computeCenter();
		this.renderingContext.drawImage(this.bitmap, center.x() * this.canvas.width, center.y() * this.canvas.height);
	}
	
	computeCenter(): FloatPoint {
		let centerX = this.boundingBox.getTopLeftCorner().x() + this.boundingBox.width() / 2;
		let centerY = this.boundingBox.getTopLeftCorner().y() + this.boundingBox.height() / 2;
		return new FloatPoint(centerX, centerY);
	}
	
	moveBy(x: number, y: number): void {
		this.boundingBox.translated(x, y);
	}
}