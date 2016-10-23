package packet_handler;

import java.io.*;
import java.net.*;
import java.util.Observable;

public class UDPServer extends Observable implements Runnable {
	
	static ServerGui serverGui;

	private final int buffer_size; // in bytes

	private int packet_size;

	private final int port;
	
	private double failure_prob = 0.0;
	
	private double corrupt_prob = 0.0;

	private volatile boolean isShutDown = false;
	
	private String outputMessage = "";

	ServerPacketHandler server_handler;

	public UDPServer(int port, ServerGui serverGui) {

		this.port = port;

		/*this.handler = new ServerPacketHandler(
				packet_size, failure_prob, this);*/

		//FIXME, packet size still needs to be set somewhere
		//this.packet_size = handler.getPacketSize();

		this.buffer_size = packet_size + 12;
		
		//FIXME probably shouldnt need to pass 
		this.serverGui = serverGui;
		
		failure_prob = serverGui.getPacketLossPercentage();

	}

	@Override
	public void run() {

		byte[] buffer = new byte[buffer_size];

		try (DatagramSocket socket = new DatagramSocket(port)) {
			
			server_handler = new ServerPacketHandler(
					packet_size, failure_prob, corrupt_prob,  this);
			
			packet_size = server_handler.getPacketSize();

			while (true) {

				if (isShutDown)
					return;

				DatagramPacket incoming = new DatagramPacket(buffer,
						buffer.length);

				try {

					socket.receive(incoming);
					
					
					this.respond(socket, incoming);

				}

				catch (SocketTimeoutException ex) {

					if (isShutDown)
						return;

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

	public void respond(DatagramSocket socket, DatagramPacket packet)
			throws IOException {

		Packet received = server_handler.dgpacketToPacket(packet);

		int seqno = received.getSeqno();

		if (received.getCksum() == 1){
			setOutputMessage("Corrupted packet! " + received.getSeqno());
		}
		
		else if (seqno < 0) {

			//System.out.println("Server received end of file");
			setOutputMessage("Server received end of file");

		}

		else {

			//System.out.println("Server received packet no " + seqno);
			setOutputMessage("Server received packet no " + seqno);

		}

		if (!server_handler.failureCheck() && received.getCksum() == 0) {

			int ackno = received.getAckno();

			Packet ackpacket = new Packet(ackno);

			DatagramPacket outgoing = server_handler.packetToDGPacket(ackpacket,
					packet.getAddress(), packet.getPort());

			//System.out.println("Server sending ack no " + ackno);
			setOutputMessage("Server sending ack no " + ackno);

			socket.send(outgoing);

		}

		try {

			Thread.sleep(1000);

		}

		catch (InterruptedException ex) {

			System.out.println(ex);

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

	/*
	 * public static void main(String[] args) {
	 * 
	 * UDPServer server = new UDPServer();
	 * 
	 * Thread t = new Thread(server);
	 * 
	 * t.start();
	 * 
	 * }
	 */

}