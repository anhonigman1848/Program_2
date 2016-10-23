package packet_handler;
import java.awt.EventQueue;

public class RunUDP {

	
	//FIXME temp solution, these 4 variables should be deleted once we move the packetHandler to the server and have it creat it.
	private final static int PORT = 7;
	
	private static int packet_size = 1024;
	
	private static double corruption_prob = 0.25;
	
	private static double failure_prob = 0.1;
	
	public RunUDP(){
		
		
		UDPClient udpClient = new UDPClient();
		ClientGui clientGui = new ClientGui(udpClient);
		clientGui.setVisible(true);
		
		udpClient.addObserver(clientGui);
		
		/*PacketHandler server_handler = new ClientPacketHandler(
				packet_size, corruption_prob, failure_prob);*/
		
		
		ServerGui serverGui = new ServerGui();
		UDPServer udpServer = new UDPServer(PORT, serverGui);
		udpServer.addObserver(serverGui);
		
		
		Thread t = new Thread(udpServer);

		t.start();
		
		serverGui.setVisible(true);
		
		//udpServer.run();
		//udpClient.run();
		
		
		
		
	}
	
	public static void main(String[] args) {
			RunUDP mainRunUDP = new RunUDP();
			
			
	}
}
