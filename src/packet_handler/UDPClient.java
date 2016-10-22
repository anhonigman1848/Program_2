package packet_handler;

import java.net.*;

public class UDPClient {

	public final static int PORT = 7;
	
	public static void main(String[] args) {

		String hostname = "localhost";

		if (args.length > 0) hostname = args[0];

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();
			
			PacketSender psender = new PacketSender();

			psender.setPacketSize(1024);

			SenderThread sender = new SenderThread(socket, ia, PORT, psender);

			sender.start();

			Thread receiver = new ReceiverThread(socket, psender);

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