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
	let strlen = dv.getUint8(10);
	let texturePath = String.fromCharCode.apply(null, new Uint8Array(buffer, 11, strlen));
	console.log(buffer);
	console.log("Decoded jigsaw packet, received: ", {id, x, y, strlen, texturePath});
	return {id, x, y, texturePath};
}

let websocket = new WebSocket("ws://127.0.0.1:8081");
websocket.onopen = function (event) {
	let canvas = document.getElementById('game');
	canvas.addEventListener('mousemove', mouseMoveHandler);
}


websocket.onmessage = async function (event) {
	let buffer = await event.data.arrayBuffer();
	// id, x, y, texturePath
	let {id, x, y, texturePath} = decodeJigsawPacket(buffer);
	
	let rectangle = new Rectangle(new FloatPoint(x, y), new FloatPoint(0.9, 0.9));
	console.log(rectangle);
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

let canvas = <HTMLCanvasElement>document.getElementById('game');	
let ctx = canvas.getContext('2d');
ctx.fillStyle = "magenta";
ctx.fillRect(0, 0, canvas.width, canvas.height);
requestAnimationFrame(draw);
	
