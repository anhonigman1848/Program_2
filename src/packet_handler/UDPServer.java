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

	public UDPServer(int port, ServerGui serverGui) {

		this.port = port;

		// FIXME probably shouldnt need to pass
		this.serverGui = serverGui;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[packet_size + 12];

		try (DatagramSocket socket = new DatagramSocket(port)) {

			server_handler = new ServerPacketHandler(packet_size, failure_prob, corruption_prob, this);

			while (true) {
				if (isShutDown)	{return;}

				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

				try {
					socket.receive(incoming);
					this.respond(socket, incoming);
				}

				catch (SocketTimeoutException ex) {
					if (isShutDown) {return;}
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

	public void shutDown() {
		this.isShutDown = true;
	}

	public void respond(DatagramSocket socket, DatagramPacket dgpacket) throws IOException {

		// convert DatagramPacket to Packet
		Packet received = server_handler.dgpacketToPacket(dgpacket);

		try {
			Thread.sleep(timeout_interval);
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
			String name = server_handler.setFile_name(received);
			
			setOutputMessage("Server received first packet of " + name);
		}

		// check for end of file packet
		else if (received.getSeqno() < 0) {
			int bytes = server_handler.getBytes_stored();
			setOutputMessage("Server received end of file; " + bytes + " bytes stored");
		}

		// this is a good packet and not end of file
		else {
			setOutputMessage("Server received packet no " + received.getSeqno());
			server_handler.addToBuffer(received);
		}

		// sending ack
		// if the received packet was corrupted, don't send ack
		if (received.getCksum() != 1) {
			int ackno = received.getAckno();
			Packet ackpacket = new Packet(ackno);
			DatagramPacket outgoing = server_handler.packetToDGPacket(ackpacket, dgpacket.getAddress(),
					dgpacket.getPort());

			// check for failure to send ack
			if (server_handler.failureCheck()) {
				setOutputMessage("Server failed to send ack " + ackno);
			}

			else {
				setOutputMessage("Server sending ack no " + ackno);
				socket.send(outgoing);
			}
		}

	}

	public String getOutputMessage() {
		return outputMessage;
	}

	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
		setChanged();
		notifyObservers(outputMessage);
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

}