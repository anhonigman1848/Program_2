package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;

class SenderThread extends Thread {

	private InetAddress server;

	private DatagramSocket socket;

	private int port;

	private PacketSender psender;

	private volatile boolean stopped = false;

	SenderThread(DatagramSocket socket, InetAddress address, int port,
			PacketSender psender) {

		this.server = address;

		this.port = port;

		this.socket = socket;

		this.psender = psender;

		this.socket.connect(server, port);

	}

	public void halt() {

		this.stopped = true;

	}

	@Override
	public void run() {

		File testfile = new File("testfile.txt");

		// PacketSender sender = new PacketSender();

		byte[] test = psender.convertFile(testfile);

		psender.makePackets(test);

		try {

			while (true) {

				if (stopped)
					return;

				Packet next = psender.nextPacket();

				// System.out.println("Next packet " + next.toString());

				DatagramPacket output = psender.packetToDGPacket(next, server,
						port);

				System.out.println("Sending packet no " + next.getSeqno());

				socket.send(output);

				try {

					Thread.sleep(1000);

				}

				catch (InterruptedException ex) {

					System.out.println(ex);

				}

				if (next.getSeqno() < 0) {

					System.out.println("End of file reached");

					// this.halt();

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