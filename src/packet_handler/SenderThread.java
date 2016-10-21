package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;

class SenderThread extends Thread {

	private InetAddress server;

	private DatagramSocket socket;

	private int port;

	private volatile boolean stopped = false;

	SenderThread(DatagramSocket socket, InetAddress address, int port) {

		this.server = address;

		this.port = port;

		this.socket = socket;

		this.socket.connect(server, port);

	}

	public void halt() {

		this.stopped = true;

	}

	@Override
	public void run() {

		File testfile = new File("testfile.txt");
		
		PacketSender sender = new PacketSender();

		byte[] test = sender.convertFile(testfile);
		
		sender.setPacketSize(256);

		sender.makePackets(test);

		try {

			while (true) {

				if (stopped) return;

				Packet next = sender.nextPacket();

				System.out.println("Next packet " + next.toString());

				DatagramPacket output = sender.packetToDGPacket(next,
						server, port);

				System.out.println("Sending packet no " + next.getSeqno());

				socket.send(output);

				if (next.getSeqno() < 0) {

					System.out.println("End of file");

					this.halt();

				}

				else {

					Thread.yield();

				}

			}

		}

		catch (IOException ex) {

			System.err.println(ex);

		}

	}

}