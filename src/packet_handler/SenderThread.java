package packet_handler;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.TimerTask;

class SenderThread extends Thread {

	UDPClient udpClient;
	
	private InetAddress server;

	private DatagramSocket socket;

	private int port;
	
	private int timeoutInterval;
	
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
		
		this.timeoutInterval = udpClient.getTimeoutInterval();

	}

	/**
	 * Stop the Thread
	 */
	public void halt() {

		this.stopped = true;

	}
	
	public void sendPacket(int seqno) {
		Packet next = handler.getPacket(seqno);
		
		// check for corrupted packet
		if (handler.corruptionCheck(seqno)) {
			next.setCksum((short) 1);
		} else {
			next.setCksum((short) 0);
		}
		
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
		
		try{
			handler.timers[seqno].schedule(new SendTask(seqno), timeoutInterval);			
		}catch (Exception e){
			//FIXME perhaps take this out
			System.out.println("SendTask Exception");
		}

		try {

			Thread.sleep(timeoutInterval / 3);

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
			udpClient.setOutputMessage("Packet " + seqno + " timed out; resending");
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

				while (handler.getPacketsPending() < handler.getWindowSize() &&
						handler.getLastPacketSent() < handler.getBufferSize() - 1) {
					int last = handler.getLastPacketSent();
					sendPacket(last + 1);
					handler.setLastPacketSent(last + 1);
					handler.setPacketsPending(handler.getPacketsPending() + 1);
				}

				Thread.yield();

			}

		}

		catch (Exception ex) {

			System.err.println(ex);

		}

	}

}