package packet_handler;

import java.io.IOException;

import java.net.*;

class ReceiverThread extends Thread {

	private DatagramSocket socket;

	private volatile boolean stopped = false;

	ReceiverThread(DatagramSocket socket) {

		this.socket = socket;

	}

	public void halt() {

		this.stopped = true;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[65507];

		while (true) {

			if (stopped) return;

			PacketSender rec = new PacketSender();

			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

			try {

				socket.receive(dp);

				Packet recd = rec.dgpacketToPacket(dp);

				int ackno = recd.getAckno();

				if (ackno == 0) {

					System.out.println("Received EOF ackno");
					
					this.halt();

				}

				else {

					System.out.println("Received ack no " + ackno);

				}

				Thread.yield();

			}

			catch (IOException ex) {

				System.err.println(ex);

			}

		}

	}

}