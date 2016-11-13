package packet_handler;

import java.io.IOException;

import java.net.*;

class ReceiverThread extends Thread {

	UDPClient udpClient;

	private DatagramSocket socket;

	private int packet_size;

	private volatile boolean stopped = false;

	private ClientPacketHandler handler;

	private int timeout_interval;

	ReceiverThread(DatagramSocket socket, ClientPacketHandler handler, UDPClient udpClient) {

		this.socket = socket;

		this.handler = handler;

		this.udpClient = udpClient;

		this.packet_size = udpClient.getPacket_size();
		
		this.timeout_interval = udpClient.getTimeout_interval();

	}

	/**
	 * Stop the Thread
	 */
	public void halt() {

		this.stopped = true;

	}


	@Override
	public void run() {

		byte[] buffer = new byte[packet_size + 12];

		while (true) {

			if (stopped)
				return;

			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

			try {
				//FIXME added and confirmed it works, but is this the only place? Added additional Catch for timeout, what needs to be in there?
				socket.setSoTimeout(timeout_interval);
				
				socket.receive(dp);

				Packet recd = handler.dgpacketToPacket(dp);
				
				int ackno = recd.getAckno();

				if (ackno == 0) {

					udpClient.setOutputMessage("Client received EOF ackno");
					udpClient.shutDownSender();

				}

				else {

					if (ackno > handler.getLastAckReceived()) {

						udpClient.setOutputMessage("Client received ack no " + ackno);

						handler.setLastAckReceived(ackno);
						
						handler.setFirstUnacked(ackno);

					}

				}

				Thread.yield();

			}
			catch(SocketTimeoutException ste){
				System.err.println(ste);
			}

			catch (IOException ex) {

				System.err.println(ex);

			}

		}

	}

}