package packet_handler;

import java.io.IOException;

import java.net.*;

class ReceiverThread extends Thread {

	private DatagramSocket socket;

	private volatile boolean stopped = false;

	private PacketSender psender;

	ReceiverThread(DatagramSocket socket, PacketSender psender) {

		this.socket = socket;

		this.psender = psender;

	}

	public void halt() {

		this.stopped = true;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[65507];

		while (true) {

			if (stopped)
				return;

			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

			try {

				socket.receive(dp);

				Packet recd = psender.dgpacketToPacket(dp);

				int ackno = recd.getAckno();

				if (ackno == 0) {

					System.out.println("Received EOF ackno");

					this.halt();

				}

				else {

					if (ackno > psender.getLastAckReceived()) {

						System.out.println("Received ack no " + ackno);

						psender.setLastAckReceived(ackno);

					}

				}

				Thread.yield();

			}

			catch (IOException ex) {

				System.err.println(ex);

			}

		}

	}

}