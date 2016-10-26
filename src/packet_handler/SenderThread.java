package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;

class SenderThread extends Thread {

	UDPClient udpClient;
	
	private InetAddress server;

	private DatagramSocket socket;

	private int port;
	
	private int timeout_interval;

	private ClientPacketHandler handler;

	private volatile boolean stopped = false;

	SenderThread(DatagramSocket socket, InetAddress address, int port,
			ClientPacketHandler handler, UDPClient udpClient) {

		this.server = address;

		this.port = port;

		this.socket = socket;

		this.handler = handler;

		this.socket.connect(server, port);
		
		this.udpClient = udpClient;
		
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

		File file = udpClient.getSelectedFile();
		
		String file_name = file.getName();
		
		byte[] file_data = handler.convertFile(file);
		
		int file_length = (int) file.length();
		
		byte[] name_in_bytes = file_name.getBytes();
		
		Packet first_packet = new Packet(0, file_length, name_in_bytes);

		handler.makePackets(file_data, first_packet);

		try {

			while (true) {

				if (stopped)
					return;

				Packet next = handler.nextPacket();
				
				DatagramPacket output = handler.packetToDGPacket(next, server,
						port);

				if (!handler.failureCheck()) {

					udpClient.setOutputMessage("Client sending packet no " + next.getSeqno());
					socket.send(output);

				}

				try {

					Thread.sleep(timeout_interval);

				}

				catch (InterruptedException ex) {

					System.out.println(ex);

				}

				if (next.getSeqno() < 0) {
					
					udpClient.setOutputMessage("Client: End of file reached");
					

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