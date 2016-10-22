package packet_handler;

import java.io.*;
import java.net.*;

public class UDPServer implements Runnable {

	private final int buffer_size; // in bytes

	private final int packet_size;

	private final int port;

	private volatile boolean isShutDown = false;

	private PacketHandler handler;

	public UDPServer(int port, PacketHandler handler) {

		this.port = port;

		this.handler = handler;

		this.packet_size = handler.getPacketSize();

		this.buffer_size = packet_size + 12;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[buffer_size];

		try (DatagramSocket socket = new DatagramSocket(port)) {

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

		Packet received = handler.dgpacketToPacket(packet);

		int seqno = received.getSeqno();

		if (seqno < 0) {

			System.out.println("Server received end of file");

		}

		else {

			System.out.println("Server received packet no " + seqno);

		}

		if (!handler.failureCheck() && received.getCksum() == 0) {

			int ackno = received.getAckno();

			Packet ackpacket = new Packet(ackno);

			DatagramPacket outgoing = handler.packetToDGPacket(ackpacket,
					packet.getAddress(), packet.getPort());

			System.out.println("Server sending ack no " + ackno);

			socket.send(outgoing);

		}

		try {

			Thread.sleep(1000);

		}

		catch (InterruptedException ex) {

			System.out.println(ex);

		}

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