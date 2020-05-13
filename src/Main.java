import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

public class Main {

	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(8080);
			System.out.print("Server Started.\nListening for connections on port 8080...\n");
			
			HTTPServerFactory serverFactory = new HTTPServerFactory(serverConnect);
			
			RouteRunner gameRoute = (response) -> {  response.send("Hello World"); return true;};
			serverFactory.attachRoute("/game", gameRoute);
			
			serverFactory.attachRoute("/inline", (response) -> {
				response.send("inline works");
				return true;
			});
			
			while (true) {
				HTTPServer myServer = serverFactory.create();
				System.out.println("Conenction opened. (" + new Date() + ")");
				
				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Conenction error : " + e.getMessage());
		}
	}
	
	
}
