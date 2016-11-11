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
	
	private int window_size;
	
	
	private SenderThread sender;
	
	private ReceiverThread receiver;


	public void run() {

		String hostname = "localhost";

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();

			ClientPacketHandler client_handler = new ClientPacketHandler(packet_size, window_size, corruption_prob, failure_prob,
					this);

			sender = new SenderThread(socket, ia, PORT, client_handler, this);

			sender.start();

			receiver = new ReceiverThread(socket, client_handler, this);

			receiver.start();

		}

		catch (UnknownHostException ex) {

			System.err.println(ex);

		}

		catch (SocketException ex) {

			System.err.println(ex);

		}

	}

	/**
	 * @return packet_size
	 */
	public int getPacket_size() {
		return packet_size;
	}

	/**
	 * @param packet_size
	 */
	public void setPacket_size(int packet_size) {
		this.packet_size = packet_size;
	}

	/**
	 * @return timeout_interval
	 */
	public int getTimeout_interval() {
		return timeout_interval;
	}

	/**
	 * @param timeout_interval
	 */
	public void setTimeout_interval(int timeout_interval) {
		this.timeout_interval = timeout_interval;
	}

	/**
	 * @return corruption_prob
	 */
	public double getCorruption_prob() {
		return corruption_prob;
	}

	/**
	 * @param corruption_prob
	 */
	public void setCorruption_prob(double corruption_prob) {
		this.corruption_prob = corruption_prob;
	}

	/**
	 * @return failure_prob
	 */
	public double getFailure_prob() {
		return failure_prob;
	}

	/**
	 * @param failure_prob
	 */
	public void setFailure_prob(double failure_prob) {
		this.failure_prob = failure_prob;
	}

	/**
	 * @return outputMessage
	 */
	public String getOutputMessage() {
		return outputMessage;
	}

	/**
	 * Sets the message and notifies Observers
	 * @param outputMessage
	 */
	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
		setChanged();
		notifyObservers(outputMessage);

	}

	/**
	 * @return selectedFile
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * @param selectedFile
	 */
	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}
	
	public void shutDownSender() {
		this.sender.halt();
	}

	public int getWindow_size() {
		return window_size;
	}

	public void setWindow_size(int window_size) {
		//System.out.println("UDPClient: Setting window_size to: " + window_size);
		this.window_size = window_size;
	}



}