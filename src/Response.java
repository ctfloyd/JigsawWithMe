import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class Response {
	
	Socket connection;
	PrintWriter out;

	public Response(Socket c) throws IOException {
		connection = c;
		out = new PrintWriter(c.getOutputStream());
	}
	
	public void send(String message) {
		out.println("HTTP/1.1 200 OK");
		out.println("Server: HTTPServer from ctfloyd: 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: text/html");
		out.println("Content-length: " + message.length());
		out.println();
		out.flush();
		
		out.println(message);
		out.flush();
	}
	
}
