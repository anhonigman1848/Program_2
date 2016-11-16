package packet_handler;

import java.io.File;
import java.net.*;
import java.util.Observable;

public class UDPClient extends Observable implements Runnable {

	static ClientGui clientGui;

	protected File selectedFile;

	private String outputMessage = "";

	private final static int PORT = 7;

	private int packetSize;

	private int timeoutInterval;

	private double corruptionProb;

	private double failureProb;
	
	private int windowSize;
	
	
	private SenderThread sender;
	
	private ReceiverThread receiver;


	public void run() {

		String hostname = "localhost";

		try {

			InetAddress ia = InetAddress.getByName(hostname);

			DatagramSocket socket = new DatagramSocket();

			ClientPacketHandler client_handler = new ClientPacketHandler(packetSize, windowSize, corruptionProb, failureProb,
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
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * @return timeoutInterval
	 */
	public int getTimeoutInterval() {
		return timeoutInterval;
	}

	/**
	 * @param timeoutInterval
	 */
	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}

	/**
	 * @return corruptionProb
	 */
	public double getCorruptionProb() {
		return corruptionProb;
	}

	/**
	 * @param corruptionProb
	 */
	public void setCorruptionProb(double corruptionProb) {
		this.corruptionProb = corruptionProb;
	}

	/**
	 * @return failureProb
	 */
	public double getFailureProb() {
		return failureProb;
	}

	/**
	 * @param failureProb
	 */
	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
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

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}



}