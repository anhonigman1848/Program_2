package packet_handler;

import java.io.File;
import java.net.*;
import java.util.Observable;

public class UDPClient extends Observable implements Runnable {

	static ClientGui clientGui;

	protected File selectedFile;

	private String outputMessage = "";
	
	private final static int PORT = 7;
	
	private int packet_size;
	
	private int timeout_interval;
	
	private double corruption_prob;
	
	private double failure_prob;
	
	public void run(){	
		//Create the GUI for the client
		//clientGui = new ClientGui();
		//clientGui.setVisible(true);
		

		String hostname = "localhost";
		
		/*PacketHandler server_handler = new PacketHandler(
				packet_size, corruption_prob, failure_prob);*/

		
		//UDPServer server = new UDPServer(PORT, server_handler);
		

		//Thread t = new Thread(server);

		//t.start();

		//if (args.length > 0) hostname = args[0];

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();
						
			ClientPacketHandler client_handler = new ClientPacketHandler(
					packet_size, corruption_prob, failure_prob, this);

			SenderThread sender = new SenderThread(socket, ia, PORT, client_handler, this);

			sender.start();

			Thread receiver = new ReceiverThread(socket, client_handler, this);

			receiver.start();

		} 
		
		catch (UnknownHostException ex) { 
		  
		  System.err.println(ex);

		} 
		
		catch (SocketException ex) { 
		  
		  System.err.println(ex);

		}

	}

	public int getPacket_size() {
		return packet_size;
	}

	public void setPacket_size(int packet_size) {
		this.packet_size = packet_size;
	}

	public int getTimeout_interval() {
		return timeout_interval;
	}

	public void setTimeout_interval(int timeout_interval) {
		this.timeout_interval = timeout_interval;
	}

	public double getCorruption_prob() {
		return corruption_prob;
	}

	public void setCorruption_prob(double corruption_prob) {
		this.corruption_prob = corruption_prob;
	}

	public double getFailure_prob() {
		return failure_prob;
	}

	public void setFailure_prob(double failure_prob) {
		this.failure_prob = failure_prob;
	}

	public String getOutputMessage() {
		return outputMessage;
	}

	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
		setChanged();
		notifyObservers(outputMessage);
		
	}
	
	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

}