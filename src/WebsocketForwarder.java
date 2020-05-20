import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsocketForwarder {
	
	private static void printInputStream(InputStream inputStream) throws IOException {
		int len = 0;
		byte[] b = new byte[1024];
		while(true) {
			len = inputStream.read(b);
			if(len != 1) {
				byte rLength = 0;
				int rMaskIndex = 2;
				int rDataStart = 0;
				
				byte data = b[1];
				byte op = (byte) 127;
				rLength = (byte) (data & op);
				
				if(rLength == (byte) 126) rMaskIndex = 4;
				if(rLength==(byte)127) rMaskIndex=10;

                byte[] masks = new byte[4];

                int j=0;
                int i=0;
                for(i=rMaskIndex;i<(rMaskIndex+4);i++){
                    masks[j] = b[i];
                    j++;
                }

                rDataStart = rMaskIndex + 4;

                int messLen = len - rDataStart;

                byte[] message = new byte[messLen];

                for(i=rDataStart, j=0; i<len; i++, j++){
                    message[j] = (byte) (b[i] ^ masks[j % 4]);
                }

                ByteBuffer buffer = ByteBuffer.wrap(message);
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

                b = new byte[1024];
			}
		}
	}
		

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		ServerSocket socket = new ServerSocket(8042);
		try {
			System.out.println("Websocket Forwarder has started on 127.0.0.1:8042...");
			Socket client = socket.accept();
			System.out.println("Conenction received");
			
			InputStream in = client.getInputStream();
			OutputStream out = client.getOutputStream();
			Scanner s = new Scanner(in, "UTF-8");
			try {
			String data = s.useDelimiter("\\r\\n\\r\\n").next();
			Matcher get = Pattern.compile("^GET").matcher(data);
			
			if(get.find()) {
				Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
				match.find();
				byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
						+ "Connection: Upgrade\r\n"
						+ "Upgrade: websocket\r\n"
						+ "Sec-WebSocket-Accept: "
						+ Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
						+ "\r\n\r\n").getBytes("UTF-8"); 
				System.out.println("Response: " + response);
				out.write(response, 0, response.length);
				
				printInputStream(in);
			}
		} finally {
			s.close();
		}
	} finally {
		socket.close();
	}
	}
}
