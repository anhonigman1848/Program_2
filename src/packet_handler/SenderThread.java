package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;

class SenderThread extends Thread {

	UDPClient udpClient;
	
	private InetAddress server;

	private DatagramSocket socket;

	private int port;

	private PacketHandler handler;

	private volatile boolean stopped = false;

	SenderThread(DatagramSocket socket, InetAddress address, int port,
			PacketHandler handler, UDPClient udpClient) {

		this.server = address;

		this.port = port;

		this.socket = socket;

		this.handler = handler;

		this.socket.connect(server, port);
		
		this.udpClient = udpClient;

	}

	public void halt() {

		this.stopped = true;

	}

	@Override
	public void run() {

		//File testfile = new File("testfile.txt");
		File testfile = udpClient.getSelectedFile();

		byte[] test = handler.convertFile(testfile);

		handler.makePackets(test);

		try {

			while (true) {

				if (stopped)
					return;

				Packet next = handler.nextPacket();
				
				DatagramPacket output = handler.packetToDGPacket(next, server,
						port);

				if (!handler.failureCheck()) {

					/*System.out.println("Client sending packet no "
							+ next.getSeqno());*/
					
					udpClient.setOutputMessage("Client sending packet no " + next.getSeqno());
					socket.send(output);

				}

				try {

					Thread.sleep(1000);

				}

				catch (InterruptedException ex) {

					System.out.println(ex);

				}

				if (next.getSeqno() < 0) {

					//System.out.println("Client: End of file reached");
					
					udpClient.setOutputMessage("Client: End of file reached");
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