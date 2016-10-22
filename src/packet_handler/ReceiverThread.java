package packet_handler;

import java.io.IOException;

import java.net.*;

class ReceiverThread extends Thread {

	private DatagramSocket socket;

	private volatile boolean stopped = false;

	private PacketHandler handler;

	ReceiverThread(DatagramSocket socket, PacketHandler handler) {

		this.socket = socket;

		this.handler = handler;

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

				Packet recd = handler.dgpacketToPacket(dp);

				int ackno = recd.getAckno();

				if (ackno == 0) {

					System.out.println("Client received EOF ackno");

					// this.halt();

				}

				else {

					if (ackno > handler.getLastAckReceived()) {

						System.out.println("Client received ack no " + ackno);

						handler.setLastAckReceived(ackno);

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