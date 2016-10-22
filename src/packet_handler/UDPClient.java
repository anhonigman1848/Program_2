package packet_handler;

import java.net.*;

public class UDPClient {

	private final static int PORT = 7;
	
	private static int packet_size = 1024;
	
	private static double corruption_prob = 0.25;
	
	private static double failure_prob = 0.1;
	
	public static void main(String[] args) {

		String hostname = "localhost";
		
		PacketHandler server_handler = new PacketHandler(
				packet_size, corruption_prob, failure_prob);

		UDPServer server = new UDPServer(PORT, server_handler);

		Thread t = new Thread(server);

		t.start();

		if (args.length > 0) hostname = args[0];

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();
			
			PacketHandler client_handler = new PacketHandler(
					packet_size, corruption_prob, failure_prob);

			SenderThread sender = new SenderThread(socket, ia, PORT, client_handler);

			sender.start();

			Thread receiver = new ReceiverThread(socket, client_handler);

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