import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPServer implements Runnable {
	
	static final File WEB_ROOT = new File("./web");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	
	static final int PORT = 8080;
	static final boolean verbose = false;
	
	static int ID = 0;
	
	
	private Socket connect;
	private Map<String, RouteRunner> routes;
	
	public HTTPServer(Socket c, Map<String, RouteRunner> routes) {
		connect = c;
		this.routes = routes;
		ID++;
	}
	
	@Override
	public void run() {
		
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			String input = in.readLine();
			
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			
			fileRequested = parse.nextToken().toLowerCase();
			
			if(!method.equals("GET") && !method.equals("HEAD")) {
				if(verbose) {
					System.out.println("501 Not Implemented: " + method + " method.");
				}
				
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				
				byte[] fileData = readFileData(file, fileLength);
				
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: HTTPServer from ctfloyd: 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println();
				out.flush();
				
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
			} else {
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}
				
				System.out.println("File requested: " + fileRequested);
				if(routes.containsKey(fileRequested)) {
					routes.get(fileRequested).run(new Response(connect));
					return;
				}
				
				
				File file = new File(WEB_ROOT, fileRequested);
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);
				
				if(method.equals("GET")) {
					byte[] fileData = readFileData(file, fileLength);
					
					out.println("HTTP/1.1 200 OK");
					out.println("Server: HTTPServer from ctfloyd: 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println();
					out.flush();
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				
				if (verbose) {
					System.out.println("File " + fileRequested + " of type " + content + " returned");
				}
			}
		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception: " + ioe);
			}
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close();
			} catch (Exception e) {
				System.out.println("Error closing stream : " + e.getMessage());
			}
			
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
		
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}
		return fileData;
	}
	
	private String getContentType(String fileRequested) {
		if(fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else if(fileRequested.endsWith(".js") || fileRequested.endsWith(".ts"))
			return "application/javascript";
		else
			return "text/plain";
	}
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from ctfloyd : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush(); 
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if (verbose) {
			System.out.println("File " + fileRequested + " not found.");
		}
	}
	
}