/*
 * ICS 460 Project 2 UDP with Stop and Wait packet handling
 * Group 4
 * Andrew Honigman, Jeff Beaupre, Sam Nwabunike
 */

package packet_handler;

import java.awt.EventQueue;

public class RunUDP {

	private final static int PORT = 7;

	private int packet_size;

	private int timeout_interval;

	private double corruption_prob;

	private double failure_prob;

	private int window_size;

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
	 * @param corruption_prob
	 */
	public void setCorruption_prob(double corruption_prob) {

		this.corruption_prob = corruption_prob;
		this.udpClient.setCorruption_prob(corruption_prob);
		this.udpServer.setCorruption_prob(corruption_prob);
	}

	/**
	 * Set Packet failure, by user input
	 * @param failure_prob
	 */
	public void setFailure_prob(double failure_prob) {

		this.failure_prob = failure_prob;
		this.udpClient.setFailure_prob(failure_prob);
		this.udpServer.setFailure_prob(failure_prob);
	}

	/**
	 * Set Packet size, by user input
	 * @param packet_size
	 */
	public void setPacket_size(int packet_size) {
		this.packet_size = packet_size;
		this.udpClient.setPacket_size(packet_size);
		this.udpServer.setPacket_size(packet_size);
	}

	/**
	 * Set Client timeout, by user input
	 * @param timeout_interval
	 */
	public void setTimeout_interval(int timeout_interval) {
		this.timeout_interval = timeout_interval;
		this.udpClient.setTimeout_interval(timeout_interval);
		this.udpServer.setTimeout_interval(timeout_interval);
	}

	private void setWindow_size(int window_size) {
		//System.out.println("RunUDP: Setting window_size to: " + window_size);

		this.window_size = window_size;
		this.udpClient.setWindow_size(window_size);
		
		
	}
	
	/**
	 * Set initial Parameters, by user input
	 * @param failure_prob
	 * @param corruption_prob
	 * @param packet_size
	 * @param timeout_interval
	 */
	public void setParameters(double failure_prob, double corruption_prob, int packet_size, int timeout_interval, int window_size) {

		this.setPacket_size(packet_size);
		this.setTimeout_interval(timeout_interval);
		this.setCorruption_prob(corruption_prob);
		this.setFailure_prob(failure_prob);
		this.setWindow_size(window_size);

		Thread t = new Thread(udpServer);
		t.start();

	}


	/**
	 * @return packet_size
	 */
	public int getPacket_size() {
		return packet_size;
	}

	/**
	 * @return timeout_interval
	 */
	public int getTimeout_interval() {
		return timeout_interval;
	}

	/**
	 * @return corruption_prob
	 */
	public double getCorruption_prob() {
		return corruption_prob;
	}

	/**
	 * @return failure_prob
	 */
	public double getFailure_prob() {
		return failure_prob;
	}

	
	//Main method
	public static void main(String[] args) {
		RunUDP mainRunUDP = new RunUDP();

	}
}