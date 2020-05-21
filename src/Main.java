import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class Main {

	static HTTPServerFactory httpFactory;
	static WebSocketFactory wsFactory;
	
	public static void httpThread() {
		HTTPServer server = httpFactory.create();
		System.out.println("Connection opened. (" + new Date() + ")");
		server.run();
	}
	
	public static void wsThread() throws NoSuchAlgorithmException {
		WebSocket ws = wsFactory.create();
		System.out.println("Created new websocket.");
		
		boolean leave = false;
		while(!ws.isClosed() || leave) {
			byte[] recv = null;
			try {
				recv = ws.recv(9);
			} catch (IOException e) {
				leave = true;
				continue;
			}
            ByteBuffer buffer = ByteBuffer.wrap(recv);
            int uid = buffer.getInt();
            short posX = buffer.getShort();
            short posY = buffer.getShort();
            byte rotation = buffer.get();
            StringBuilder st = new StringBuilder();
            st.append("Received data: ");
            st.append(" uid: " + uid);
            st.append(" posX: " + posX);
            st.append(" posY: " + posY);
            st.append(" rotation: " + rotation);
            System.out.println(st.toString());
		}
	}
	
	public static void main(String[] args) throws IOException {
		Runnable http = () -> httpThread();
		Runnable ws = () -> {
			try {
				wsThread();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
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
		
		ArrayList<Thread> allThreads = new ArrayList<>();
		while(true) {
			Thread httpT = new Thread(http);
			allThreads.add(httpT);
			httpT.start();
			
			Thread wsT = new Thread(ws);
			allThreads.add(wsT);
			wsT.start();
		}
			
	}
}	
