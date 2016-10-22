package packet_handler;

import java.net.*;

public class UDPClient {

	public final static int PORT = 7;
	
	public static void main(String[] args) {

		String hostname = "localhost";
		
		UDPServer server = new UDPServer();

		Thread t = new Thread(server);

		t.start();

		if (args.length > 0) hostname = args[0];

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();
			
			PacketHandler handler = new PacketHandler();

			handler.setPacketSize(1024);

			SenderThread sender = new SenderThread(socket, ia, PORT, handler);

			sender.start();

			Thread receiver = new ReceiverThread(socket, handler);

			receiver.start();

		} 
		
		catch (UnknownHostException ex) { 
		  
		  System.err.println(ex);

		} 
		
		catch (SocketException ex) { 
		  
		  System.err.println(ex);

		}

	}

}