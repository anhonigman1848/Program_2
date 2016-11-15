package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

class SenderThread extends Thread {

	UDPClient udpClient;
	
	private InetAddress server;

	private DatagramSocket socket;

	private int port;
	
	private int timeout_interval;
	
	private Timer timer;

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
	
	public void sendPacket(int seqno) {
		Packet next = handler.getPacket(seqno);
		DatagramPacket output = handler.packetToDGPacket(next, server,
				port);

		// check for failure before sending
		if (!handler.failureCheck(seqno)) {

			try {
				udpClient.setOutputMessage("Client sending packet no " + seqno);
				socket.send(output);
				if (seqno < 0) {
					udpClient.setOutputMessage("Client: End of file reached");
				}
			}
			catch (IOException ex) {
				System.out.println("Error in sendPacket");
			}

		}
		if (handler.getTimer(seqno) != null) {
			handler.stopTimer(seqno);
		} 
		handler.startTimer(seqno);
		handler.timers[seqno].schedule(new SendTask(seqno), timeout_interval);

		try {

			Thread.sleep(timeout_interval / 2);

		}

		catch (InterruptedException ex) {

			System.out.println(ex);

		}

	}
	
	class SendTask extends TimerTask {
		private int seqno;
		SendTask(int seqno) {
			this.seqno = seqno;
		}
		public void run() {
			udpClient.setOutputMessage("Packet " + seqno + " timed out");
			sendPacket(seqno);
		}
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

				while (handler.getPacketsPending() < handler.getWindow_size() &
						handler.getLastPacketSent() < handler.getBufferSize() - 1) {
					int last = handler.getLastPacketSent();
					sendPacket(last + 1);
					handler.setLastPacketSent(last + 1);
					handler.setPacketsPending(handler.getPacketsPending() + 1);
					udpClient.setOutputMessage(handler.getPacketsPending() + " Packets pending");
				}

				Thread.yield();

			}

		}

		catch (Exception ex) {

			System.err.println(ex);

		}

	}

}