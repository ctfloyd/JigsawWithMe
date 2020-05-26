import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Random;

public class Main {

	static HTTPServerFactory httpFactory;
	static WebSocketFactory wsFactory;
	static volatile int httpThreadCount = 0;
	static volatile int wsThreadCount = 0;
	static final int MAX_HTTP_THREADS = 10;
	static final int MAX_WS_THREADS = 5;
	
	public static void httpThread() {
		HTTPServer server = httpFactory.create();
		System.out.println("Connection opened. (" + new Date() + ")");
		server.run();
	}
	
	public static void wsThread(JigsawPieceFactory puzzle) {
		WebSocket ws = wsFactory.create();
		System.out.println("Created new websocket.");
		
		for(JigsawPiece p: puzzle.getPieces()) {
			JigsawPacket packet = new JigsawPacket(p);
			try {
				ws.send(packet.toByteArray());
			} catch (IOException e) {
				System.out.println("Unable to send data over websocket. Exiting now.");
				return;
			}
		}
	}
	
	public static synchronized void main(String[] args) throws IOException {
		ServerSocket httpServer = new ServerSocket(8080);
		System.out.println("Started httpServer, listening for connections on port 8080...");
		httpFactory = new HTTPServerFactory(httpServer);
		httpFactory.attachRoute("/inline", (response) -> {
			response.send("inline works");
			return true;
		});
		
		ServerSocket wsServer = new ServerSocket(8081);
		System.out.println("Started websocket server, listening for connections on port 8081...");
		wsFactory = new WebSocketFactory(wsServer);
		
		JigsawPieceFactory puzzle = new JigsawPieceFactory("test.png");
		JigsawPiece[] pieces = puzzle.getPieces();
		Random r = new Random();
		for(int i = 0; i < pieces.length; i++) {
			if(r.nextInt(100) < 50) {
				pieces[i].addNotch(JigsawPiece.Side.Right);
			} else {
				pieces[i + 1].addLatch(JigsawPiece.Side.Bottom);
			}
			pieces[i].getTexture().writeToFile("./web/game/0/textures", pieces[i].getId() + ".png");
		}
		
		Runnable ws = () -> wsThread(puzzle);
		
		ThreadResolver threadCleanup = (Thread t) -> {
			if(t.getName().equals("http"))
				httpThreadCount--;
			if(t.getName().equals("ws"))
				wsThreadCount--;
		};
		
		while(true) {
			if(httpThreadCount < MAX_HTTP_THREADS) {
				Thread httpT = new NotifierThread(Main::httpThread, threadCleanup);
				httpT.setName("http");
				httpT.start();
				httpThreadCount++;
			}
			if(wsThreadCount < MAX_WS_THREADS) {
				Thread wsT = new NotifierThread(ws, threadCleanup);
				wsT.setName("ws");
				wsT.start();
				wsThreadCount++;
			}
		}
			
	}
}	
