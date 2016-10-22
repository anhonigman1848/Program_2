package packet_handler;

import java.io.*;
import java.net.*;

public class UDPServer implements Runnable {

	public final static int DEFAULT_PORT = 7;

	private final int bufferSize; // in bytes

	private final int port;

	private volatile boolean isShutDown = false;

	private PacketSender preceiver;

	public UDPServer(int port, int bufferSize) {

		this.port = port;

		this.bufferSize = bufferSize;

		this.preceiver = new PacketSender();

	}

	public UDPServer(int port) {

		this(port, 1892);

	}

	public UDPServer() {

		this(DEFAULT_PORT);

	}

	@Override
	public void run() {

		byte[] buffer = new byte[bufferSize];

		try (DatagramSocket socket = new DatagramSocket(port)) {

			// socket.setSoTimeout(10000); // check every 10 seconds for
			// shutdown

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

		Packet received = preceiver.dgpacketToPacket(packet);

		int seqno = received.getSeqno();

		if (seqno < 0) {

			System.out.println("Received end of file");

		}

		else {

			System.out.println("Received packet no " + seqno);

		}

		int ackno = received.getAckno();

		Packet ackpacket = new Packet(ackno);

		DatagramPacket outgoing = preceiver.packetToDGPacket(ackpacket,
				packet.getAddress(), packet.getPort());

		if (Math.random() > 0.5) {
			
			System.out.println("Sending ack no " + ackno);

			socket.send(outgoing);

			try {

				Thread.sleep(1000);

			}

			catch (InterruptedException ex) {

				System.out.println(ex);

			}

		}

	}

	public static void main(String[] args) {

		UDPServer server = new UDPServer();

		Thread t = new Thread(server);

		t.start();

	}

}