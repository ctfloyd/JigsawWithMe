import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class HTTPServerFactory {
	
	ServerSocket connection;
	private Map<String, RouteRunner> routes;
	
	public HTTPServerFactory(ServerSocket c) {
		connection = c;
		routes = new HashMap<>();
	}
	
	public HTTPServer create() {
		try {
			return new HTTPServer(connection.accept(), routes);
		} catch (IOException e) {
			System.err.println("HTTPServerFactory could not create a new server : " + e.getMessage());
		}
		return null;
	}
	
	public void attachRoute(String route, RouteRunner handle) {
		routes.put(route, handle);
	}

}
