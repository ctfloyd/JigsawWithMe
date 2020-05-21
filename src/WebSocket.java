import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocket {

	public static final String HANDSHAKE = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	Socket socket;
	InputStream input;
	OutputStream output;
	Scanner s;
	
	public WebSocket(Socket socket) throws IOException, NoSuchAlgorithmException {
		this.socket = socket;
		input = socket.getInputStream();
		output = socket.getOutputStream();
		handshake();
	}
	
	public byte[] recv(int numBytes) throws IOException {
		int len = 0;
		int protocolLength = 6;
		byte[] b = new byte[numBytes + protocolLength];
		len = input.read(b);
		
		if(len < 0) {
			return null;
		}
		
		// Check to see that this is the last frame being sent.
		// If it's not we don't know how to handle this, close the connection.
		byte informationByte = b[0];
		byte fin = (byte) (informationByte & 0x80);
		byte rsv = (byte) (informationByte & 0x70);
		if(fin != (byte) 0x80 || rsv != 0) {
			System.out.println("Websocket closing connection. Unrecognized fin or rsv.");
			System.out.println("Fin: " + fin + " and RSV: " + rsv);
			socket.close();
			return null;
		}
		
		byte opCode = (byte) (informationByte & 0x0E);
		if(opCode != 0x2) {
			System.out.println("Websocket closing connection. Unrecognized opcode.");
			socket.close();
			return null;
		}
		byte maskAndLengthByte = b[1];
		byte mask = (byte) (maskAndLengthByte & 0x80);
		byte length = (byte) (maskAndLengthByte & 0x7F);
		if(mask != (byte) 0x80) {
			System.out.println("Websocket closing connection. Unrecognized mask.");
			System.out.println("Mask: " + mask);
			System.out.println("Length: " + length);
			socket.close();
			return null;
		}
		
		// If length > 125, this message has an extended payload. We don't expect the jigsaw puzzle protocol
		// to ever send messages that are this long.
		if(length > 125) {
			System.out.println("Websocket closing connection. Length is outside of expected bounds.");
			socket.close();
			return null;
		}
		
		//The key is the next 4 bytes in the array. After the infoByte and M&L byte
		byte[] key = new byte[4];
		int maskOffset = 2;
		for(int i = 0, j = maskOffset; i < 4; i++) {
			key[i] = b[j + i];
		}
		
		byte[] data = new byte[length];
		int dataOffset = 6;
		for(int i = 0, j = dataOffset; i < length; i++) {
			data[i] = (byte) (b[i + j] ^ key[i & 0x3]);
		}
		
		return data;
	}
	
	private void handshake() throws NoSuchAlgorithmException, IOException {
		s = new Scanner(input, "UTF-8");
		
		String data = null;
		Matcher get = null;
		try {
			data = s.useDelimiter("\\r\\n\\r\\n").next();
			get = Pattern.compile("^GET").matcher(data);
		} catch (Exception e) {
			// Todo: don't catch general exception.
			System.out.println("Error.");
		}
		
		if(!get.find())
			return;
		
		Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
		match.find();
		StringBuilder response = new StringBuilder();
		byte[] key = (match.group(1) + HANDSHAKE).getBytes("UTF-8");
		String b64Handshake = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(key));
		response.append("HTTP/1.1 101 Switching Protocols\r\n");
		response.append("Connection: Upgrade\r\n");
		response.append("Upgrade: websocket\r\n");
		response.append("Sec-WebSocket-Accept: ");
		response.append(b64Handshake);
		response.append("\r\n\r\n");
		byte[] responseBytes = response.toString().getBytes();
		output.write(responseBytes, 0, responseBytes.length);
	}
	
	public void close() throws IOException {
		if (s != null) {
			s.close();
		}
		if (socket != null) {
			socket.close();
		}
	}
	
	public boolean isClosed() {
		return socket.isClosed();
	}
}
