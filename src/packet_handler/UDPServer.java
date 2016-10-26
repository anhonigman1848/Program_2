package packet_handler;

import java.io.*;
import java.net.*;
import java.util.Observable;

public class UDPServer extends Observable implements Runnable {

	static ServerGui serverGui;

	private int packet_size;

	private int timeout_interval;

	private final int port;

	private double failure_prob;

	private double corruption_prob;

	private volatile boolean isShutDown = false;

	private String outputMessage = "";

	ServerPacketHandler server_handler;

	/**
	 * @param port
	 * @param serverGui
	 */
	public UDPServer(int port, ServerGui serverGui) {

		this.port = port;

		this.serverGui = serverGui;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[packet_size + 12];

		try (DatagramSocket socket = new DatagramSocket(port)) {

			server_handler = new ServerPacketHandler(packet_size, failure_prob, corruption_prob, this);

			while (true) {
				if (isShutDown) {
					return;
				}

				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

				try {
					socket.receive(incoming);
					this.respond(socket, incoming);
				}

				catch (SocketTimeoutException ex) {
					if (isShutDown) {
						return;
					}
				}

				catch (IOException ex) {
					System.err.println(ex);
				}
			}
		}

		catch (SocketException ex) {
			System.err.println(ex);
		}
	}

	/**
	 * Stops the Server
	 */
	public void shutDown() {
		this.isShutDown = true;
	}

	/**
	 * Server Response
	 * 
	 * @param socket
	 * @param dgpacket
	 * @throws IOException
	 */
	public void respond(DatagramSocket socket, DatagramPacket dgpacket) throws IOException {

		// convert DatagramPacket to Packet
		Packet received = server_handler.dgpacketToPacket(dgpacket);

		try {
			Thread.sleep(timeout_interval / 3);
		}

		catch (InterruptedException ex) {
			System.out.println(ex);
		}

		// check for corrupted packet
		if (received.getCksum() == 1) {
			setOutputMessage("Server received and discarded corrupted packet " + received.getSeqno());
		}

		// check for first packet
		else if (received.getSeqno() == 0) {
			byte[] name_in_bytes = received.getData();
			String name = new String(name_in_bytes);
			server_handler.setFile_name(received);
			int length = received.getAckno();
			server_handler.setFile_length(length);
			setOutputMessage("Server received packet 0 of " + name);

		}

		// check for end of file packet
		else if (received.getSeqno() < 0) {
			server_handler.outputFile();
			setOutputMessage("Server received end of file");
		}

		// this is a good packet and not end of file
		else {
			setOutputMessage("Server received packet no " + received.getSeqno());
			server_handler.addToBuffer(received);
		}

		// sending ack
		// if the received packet was corrupted, don't send ack
		if (received.getCksum() != 1) {
			int ackno;
			if (received.getSeqno() == 0) {
				ackno = 1;
			} else {
				ackno = received.getAckno();
			}
			Packet ackpacket = new Packet((short) 0, ackno);

			// check for failure to send ack
			if (server_handler.failureCheck()) {
				setOutputMessage("Server failed to send ack " + ackno);
			}

			else {
				DatagramPacket outgoing = server_handler.packetToDGPacket(ackpacket, dgpacket.getAddress(),
						dgpacket.getPort());
				setOutputMessage("Server sending ack no " + ackno);
				socket.send(outgoing);
			}
		}

	}

	/**
	 * @return outputMessage
	 */
	public String getOutputMessage() {
		return outputMessage;
	}

	/**
	 * Sets the message and notifies Observers
	 * 
	 * @param outputMessage
	 */
	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
		setChanged();
		notifyObservers(outputMessage);
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

}