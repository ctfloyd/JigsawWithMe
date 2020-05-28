import FloatPoint from "./FloatPoint.js";
import Rectangle from "./Rectangle.js";

export default class JigsawPiece {
	
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
	
	getBoundingBox(): Rectangle {
		return this.boundingBox;
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
		// TODO: this should compute from center, once its computed correctly
		this.renderingContext.drawImage(this.bitmap, this.boundingBox.getTopLeftCorner().x(), this.boundingBox.getTopLeftCorner().y());
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