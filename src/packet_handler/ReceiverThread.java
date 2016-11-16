package packet_handler;

import java.io.IOException;
import java.net.*;

class ReceiverThread extends Thread {

	UDPClient udpClient;

	private DatagramSocket socket;

	private int packetSize;

	private volatile boolean stopped = false;

	private ClientPacketHandler handler;

	ReceiverThread(DatagramSocket socket, ClientPacketHandler handler, UDPClient udpClient) {

		this.socket = socket;

		this.handler = handler;

		this.udpClient = udpClient;

		this.packetSize = udpClient.getPacketSize();
		
	}

	/**
	 * Stop the Thread
	 */
	public void halt() {

		this.stopped = true;

	}


	@Override
	public void run() {

		byte[] buffer = new byte[packetSize + 12];

		while (true) {

			if (stopped)
				return;

			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

			try {
				
				socket.receive(dp);

				Packet recd = handler.dgpacketToPacket(dp);
				
				short cksum = recd.getCksum();
				
				int ackno = recd.getAckno();

				if (cksum == 0) {

					if (ackno == 0) {
						udpClient.setOutputMessage("Client received EOF ackno");
						for (int i = 0; i < handler.getBufferSize(); i++) {
							handler.stopTimer(i);
						}
						udpClient.shutDownSender();

					}

					else {

						if (ackno > handler.getLastAckReceived()) {

							for (int i = 0; i < ackno; i++) {
								handler.stopTimer(i);
							}
							udpClient.setOutputMessage(
									"Client received acknowledgement of packet " + (ackno - 1) 
									+ "; Server ready for packet " + ackno);

							handler.setLastAckReceived(ackno);

							int pp = handler.getLastPacketSent() - ackno + 1;

							handler.setPacketsPending(pp);

						}

					}

				} else {
					
					udpClient.setOutputMessage(
							"Client received corrupted acknowledgement of packet " + (ackno - 1));
					
				}

				Thread.yield();

			}
			catch (IOException ex) {

				System.err.println(ex);

			}

		}

	}

}