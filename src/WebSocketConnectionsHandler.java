import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/* A WebSocket can have multiple connections, however the WebSocket
 * class represents a discrete instantiation of a WebSocket. In this class
 * we'd like to define what it means to have multiple clients connected to a socket.
 *
 * In particular we have many connections to many discrete WebSockets, however each game
 * is assigned an ID and these clients should all be able to communicate as if they are
 * talking to one universal WebSocket
 */
public class WebSocketConnectionsHandler {

	HashMap<Integer, ArrayList<WebSocket>> connections;
	
	public WebSocketConnectionsHandler() {
		
	}
	
	void addConnection(int id, WebSocket websocket) {
		ArrayList<WebSocket> connectionsAtId = connections.get(id);
		if(connectionsAtId == null) {
			ArrayList<WebSocket> temp = new ArrayList<>();
			temp.add(websocket);
			connections.put(id,  temp);
			return;
		}
		connectionsAtId.add(websocket);
	}
	
	boolean removeConnection(int id, WebSocket websocket) {
		ArrayList<WebSocket> connectionsAtId = connections.get(id);
		if(connectionsAtId == null) {
			return false;
		}
		return connectionsAtId.remove(websocket);
	}
	
	void broadcast(int id, byte[] data) throws IOException {
		for(WebSocket ws : connections.get(id)) {
			ws.send(data);
		}
	}
	
	void receiveAll(int id) throws IOException {
		for(WebSocket ws : connections.get(id)) {
			ws.recv(10);
		}
	}
	
}
