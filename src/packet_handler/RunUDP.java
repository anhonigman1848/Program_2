/*
 * ICS 460 Project 2 UDP with Stop and Wait packet handling
 * Group 4
 * Andrew Honigman, Jeff Beaupre, Sam Nwabunike
 */

package packet_handler;

import java.awt.EventQueue;

public class RunUDP {

	private final static int PORT = 7;

	private int packetSize;

	private int timeoutInterval;

	private double corruptionProb;

	private double failureProb;

	private int windowSize;

	private UDPClient udpClient;

	private UDPServer udpServer;


	/**
	 * Setup Client and Server
	 */
	public RunUDP() {

		this.udpClient = new UDPClient();
		ClientGui clientGui = new ClientGui(udpClient, this);
		clientGui.setVisible(true);

		udpClient.addObserver(clientGui);

		ServerGui serverGui = new ServerGui();
		udpServer = new UDPServer(PORT, serverGui);
		udpServer.addObserver(serverGui);

		serverGui.setVisible(true);

	}

	/**
	 * Set the Checksum corruption, by user input
	 * @param corruptionProb
	 */
	public void setCorruptionProb(double corruptionProb) {

		this.corruptionProb = corruptionProb;
		this.udpClient.setCorruptionProb(corruptionProb);
		this.udpServer.setCorruptionProb(corruptionProb);
	}

	/**
	 * Set Packet failure, by user input
	 * @param failureProb
	 */
	public void setFailureProb(double failureProb) {

		this.failureProb = failureProb;
		this.udpClient.setFailureProb(failureProb);
		this.udpServer.setFailureProb(failureProb);
	}

	/**
	 * Set Packet size, by user input
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
		this.udpClient.setPacketSize(packetSize);
		this.udpServer.setPacketSize(packetSize);
	}

	/**
	 * Set Client timeout, by user input
	 * @param timeoutInterval
	 */
	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
		this.udpClient.setTimeoutInterval(timeoutInterval);
		this.udpServer.setTimeoutInterval(timeoutInterval);
	}

	private void setWindowSize(int windowSize) {

		this.windowSize = windowSize;
		this.udpClient.setWindowSize(windowSize);
		
		
	}
	
	/**
	 * Set initial Parameters, by user input
	 * @param failureProb
	 * @param corruptionProb
	 * @param packetSize
	 * @param timeoutInterval
	 */
	public void setParameters(double failureProb, double corruptionProb, int packetSize, int timeoutInterval, int windowSize) {
		

		this.setPacketSize(packetSize);
		this.setTimeoutInterval(timeoutInterval);
		this.setCorruptionProb(corruptionProb);
		this.setFailureProb(failureProb);
		this.setWindowSize(windowSize);

		Thread t = new Thread(udpServer);
		t.start();

	}


	/**
	 * @return packetSize
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * @return timeoutInterval
	 */
	public int getTimeoutInterval() {
		return timeoutInterval;
	}

	/**
	 * @return corruptionProb
	 */
	public double getCorruptionProb() {
		return corruptionProb;
	}

	/**
	 * @return failureProb
	 */
	public double getFailureProb() {
		return failureProb;
	}

	
	//Main method
	public static void main(String[] args) {
		RunUDP mainRunUDP = new RunUDP();

	}
}