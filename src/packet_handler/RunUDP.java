package packet_handler;
import java.awt.EventQueue;

public class RunUDP {

	
	private final static int PORT = 7;
	
	private int packet_size;
	
	private int timeout_interval;
	
	private double corruption_prob;
	
	private double failure_prob;
	
	private UDPClient udpClient;
	
	private UDPServer udpServer;
	
	public RunUDP(){
		
		
		this.udpClient = new UDPClient();
		ClientGui clientGui = new ClientGui(udpClient, this);
		clientGui.setVisible(true);
		
		udpClient.addObserver(clientGui);
		
		/*PacketHandler server_handler = new ClientPacketHandler(
				packet_size, corruption_prob, failure_prob);*/
		
		
		ServerGui serverGui = new ServerGui();
		udpServer = new UDPServer(PORT, serverGui);
		udpServer.addObserver(serverGui);
		
		
		//Thread t = new Thread(udpServer);

		//t.start();
		
		serverGui.setVisible(true);
		
		//udpServer.run();
		//udpClient.run();
		
		
		
		
	}
	
	public void setCorruption_prob(double corruption_prob) {
		
		this.corruption_prob = corruption_prob;
		this.udpClient.setCorruption_prob(corruption_prob);
		this.udpServer.setCorruption_prob(corruption_prob);
	}
	
	public void setFailure_prob(double failure_prob) {
		
		this.failure_prob = failure_prob;
		this.udpClient.setFailure_prob(failure_prob);
		this.udpServer.setFailure_prob(failure_prob);
	}
	
	public void setPacket_size(int packet_size) {
		this.packet_size = packet_size;
		this.udpClient.setPacket_size(packet_size);
		this.udpServer.setPacket_size(packet_size);
	}

	public void setTimeout_interval(int timeout_interval) {
		this.timeout_interval = timeout_interval;
		this.udpClient.setTimeout_interval(timeout_interval);
		this.udpServer.setTimeout_interval(timeout_interval);
	}

	public void setParameters(double failure_prob, double corruption_prob,
			int packet_size, int timeout_interval) {
		
		this.setPacket_size(packet_size);
		this.setTimeout_interval(timeout_interval);
		this.setCorruption_prob(corruption_prob);
		this.setFailure_prob(failure_prob);
		// FIXME - need to shut down udpServer Thread if it is already running
		Thread t = new Thread(udpServer);
		t.start();
		
	}
	
	public int getPacket_size() {
		return packet_size;
	}

	public int getTimeout_interval() {
		return timeout_interval;
	}

	public double getCorruption_prob() {
		return corruption_prob;
	}

	public double getFailure_prob() {
		return failure_prob;
	}

	public static void main(String[] args) {
			RunUDP mainRunUDP = new RunUDP();
			
			
	}
}