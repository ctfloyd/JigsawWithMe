import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;


public class WebSocketFactory {
	
	ServerSocket connection;
	
	public WebSocketFactory(ServerSocket c) {
		connection = c;
	}
	
	public WebSocket create() {
		try {
			return new WebSocket(connection.accept());
		} catch (IOException e) {
			System.err.println("WebSocketFactory could not create a new server : " + e.getMessage());
		}
		return null;
	}
}