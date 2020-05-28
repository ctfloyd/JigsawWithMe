import JigsawPiece from "./JigsawPiece.js";
import Rectangle from "./Rectangle.js";
import FloatPoint from "./FloatPoint.js";

function makePuzzlePacket(uid, posX, posY, rot) {
	let buf = new ArrayBuffer(9);
	let view = new DataView(buf, 0);
	view.setInt32(0, uid);
	view.setInt16(4, posX);
	view.setInt16(6, posY);
	view.setInt8(8, rot);
	return view;
}

function decodeJigsawPacket(buffer) {
	let dv = new DataView(buffer, 0);
	let id = dv.getInt16(0);
	let x = dv.getFloat32(2);
	let y = dv.getFloat32(6);
	let width = dv.getFloat32(10);
	let height = dv.getFloat32(14);
	let strlen = dv.getUint8(18);
	console.log(strlen);
	let texturePath = String.fromCharCode.apply(null, new Uint8Array(buffer, 19, strlen));
	console.log("Decoded jigsaw packet, received: ", { id, x, y, strlen, texturePath });
	return { id, x, y, width, height, texturePath };
}

let websocket = new WebSocket("ws://127.0.0.1:8081");
websocket.onopen = function(event) {
	let canvas = document.getElementById('game');
	canvas.addEventListener('mousemove', mouseMoveHandler);
}


websocket.onmessage = async function(event) {
	let buffer = await event.data.arrayBuffer();
	// id, x, y, texturePath
	let { id, x, y, width, height, texturePath } = decodeJigsawPacket(buffer);

	let topLeftX: number = x * canvas.width;
	let topLeftY: number = y * canvas.height;
	let bottomRightX: number = (x + width) * canvas.width;
	let bottomRightY: number = (y + height) * canvas.height;
	let rectangle = new Rectangle(new FloatPoint(topLeftX, topLeftY), new FloatPoint(bottomRightX, bottomRightY));
	puzzlePieces.push(new JigsawPiece(id, rectangle, texturePath, canvas));
}

function mouseMoveHandler(event) {
	let data = makePuzzlePacket(0, event.clientX, event.clientY, 0);
	websocket.send(data);
}

let puzzlePieces = [];
function draw() {

	let ctx = canvas.getContext('2d');
	ctx.fillStyle = 'magenta';
	ctx.fillRect(0, 0, canvas.width, canvas.height);
	puzzlePieces.forEach(p => {
		p.paint();
	});
	requestAnimationFrame(draw);
}

// Left middle right held;
let isDragging: boolean = false;
let intersectingPiece: JigsawPiece = null;
let previousEvent: FloatPoint = null;
let canvas = <HTMLCanvasElement>document.getElementById('game');

canvas.onmousedown = (event: MouseEvent) => {
	isDragging = true;
	puzzlePieces.forEach(piece => {
		if (piece.getBoundingBox().intersectsPoint(new FloatPoint(event.clientX, event.clientY)))
			intersectingPiece = piece;
	});
}

canvas.onmousemove = (event: MouseEvent) => {
	if(isDragging && intersectingPiece) {
		let originalPosition: FloatPoint;
		if(!previousEvent) {
			originalPosition = intersectingPiece.computeCenter();
		} else {
			originalPosition = previousEvent;
		}
		console.log(originalPosition, event.clientX, event.clientY, intersectingPiece.getBoundingBox());
		intersectingPiece.moveBy(originalPosition.x() - event.clientX, originalPosition.y() - event.clientY);
		previousEvent = new FloatPoint(event.clientX, event.clientY);
	}
}

canvas.onmouseup = () => {
	isDragging = false;
	intersectingPiece = null;
	previousEvent = null;
};

let ctx = canvas.getContext('2d');
ctx.fillStyle = "magenta";
ctx.fillRect(0, 0, canvas.width, canvas.height);
requestAnimationFrame(draw);

