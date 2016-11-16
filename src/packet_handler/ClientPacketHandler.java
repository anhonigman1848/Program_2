package packet_handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.nio.ByteBuffer;
import java.net.*;

public class ClientPacketHandler {

	UDPClient udpClient;

	// store packets in BlockingQueue for Thread support
	private BlockingQueue<Packet> buffer;
	
	// keep track of how many bytes sent
	private int bytesSent;
	
	//Size of the sliding window
	private int windowSize;
	
	// store packets in Packet array
	private Packet[] window;
	
	// timers for packet being sent
	protected volatile Timer[] timers;
	
	private volatile int lastAckReceived;
	
	// seqno of last element of window
	private volatile int lastPacketSent;

	// number of packets sent and unacked
	private volatile int packetsPending;

	private int packetSize;

	// probability that cksum of any Packet will be corrupted
	private double corruptionProb;

	// probability that a Packet will fail on sending
	private double failureProb;


	/**
	 * Constructor
	 * @param packetSize
	 * @param corruptionProb
	 * @param failureProb
	 * @param udpClient
	 */
	public ClientPacketHandler(int packetSize, int windowSize, double corruptionProb, double failureProb, UDPClient udpClient) {

		this.packetSize = packetSize;
		
		this.windowSize = windowSize;

		this.corruptionProb = corruptionProb;

		this.failureProb = failureProb;

		this.lastAckReceived = 0;
		
		this.lastPacketSent = -1;
		
		this.packetsPending = 0;

		this.udpClient = udpClient;

		this.bytesSent = 0;

	}

	/**
	 * @return packetSize
	 */
	public int getPacketSize() {

		return (packetSize);

	}

	/**
	 * @return bytesSent
	 */
	public int getBytesSent() {

		return (bytesSent);

	}

	/**
	 * Set the Acknowledgment
	 * @param ackno
	 */
	public synchronized void setLastAckReceived(int ackno) {

		this.lastAckReceived = ackno;

	}

	/**
	 * @return laskAckReceived
	 */
	public synchronized int getLastAckReceived() {

		return (lastAckReceived);

	}
	
	public synchronized Timer getTimer(int seqno) {
		return (timers[seqno]);
	}
	
	public synchronized void startTimer(int seqno) {
		timers[seqno] = new Timer();
	}
	
	public synchronized void stopTimer(int seqno) {
		if (timers[seqno] != null) {
			timers[seqno].cancel();
		}
	}

	public synchronized int getLastPacketSent() {
		return lastPacketSent;
	}

	public synchronized void setLastPacketSent(int lastPacketSent) {
		this.lastPacketSent = lastPacketSent;
	}

	public synchronized int getPacketsPending() {
		return packetsPending;
	}

	public synchronized void setPacketsPending(int packetsPending) {
		this.packetsPending = packetsPending;
	}

	public synchronized int getWindowSize() {
		return windowSize;
	}
	
	public Packet getPacket(int seqno) {
		return (window[seqno]);
	}

	/**
	 * Method to simulate packet corruption
	 * @return boolean
	 */
	public boolean corruptionCheck(int seqno) {

		if (Math.random() < corruptionProb) {

			udpClient.setOutputMessage("Corrupted packet " + seqno);

			return (true);

		}

		else {

			return (false);

		}

	}

	/**
	 * Method to simulate failures
	 * @return boolean
	 */
	public boolean failureCheck(int seqno) {

		if (Math.random() < failureProb) {

			udpClient.setOutputMessage("Failed to send packet no " + seqno);

			return (true);

		}

		else {

			return (false);

		}

	}

	/**
	 * Converts file to byte array
	 * @param passedFile
	 * @return data
	 */
	public byte[] convertFile(File passedFile) {

		FileInputStream input = null;

		File file = passedFile;

		byte[] data = new byte[(int) file.length()];

		try {

			input = new FileInputStream(file);

			input.read(data);

			input.close();

			return (data);

		}

		catch (Exception ex) {

			System.out.println("Error in convertFile: " + ex);

			return (null);

		}

	}

	/**
	 * Makes byte array into packets
	 * @param data
	 * @param first_packet
	 */
	public void makePackets(byte[] data, Packet first_packet) {

		int seqno = 1;

		int queue_size = data.length / packetSize + 3;

		buffer = new ArrayBlockingQueue<Packet>(queue_size);
		
		timers = new Timer[queue_size];

		try {
			buffer.put(first_packet);
		} catch (InterruptedException ex) {

		}

		try {

			for (int i = 0; i < data.length; i += packetSize) {

				int current_size = packetSize;

				if (data.length - i < packetSize) {

					current_size = data.length % packetSize;

				}

				byte[] packet_data = new byte[current_size];

				System.arraycopy(data, i, packet_data, 0, current_size);

				Packet new_packet = new Packet(seqno, packet_data);

				buffer.put(new_packet);

				seqno++;

			}

			// final Packet with null data to mark end of file

			byte[] nodata = new byte[0];

			Packet last_packet = new Packet(-1, nodata);

			buffer.put(last_packet);
			
			window = buffer.toArray(new Packet[0]);
			
		} catch (Exception ex) {

			System.out.println("Error in makePackets: " + ex);

		}

	}
	
	/**
	 * @return buffer size
	 */
	public int getBufferSize() {

		return (buffer.size());

	}
	
	/**
	 * Converts Packet to Datagram Packet
	 * @param newPacket
	 * @param server
	 * @param port
	 * @return output datagram
	 */
	public DatagramPacket packetToDGPacket(Packet newPacket, InetAddress server, int port) {

		Packet input_p = newPacket;

		byte[] temp = new byte[input_p.getLength()];

		ByteBuffer buf = ByteBuffer.wrap(temp);

		buf.putShort(input_p.getCksum());

		buf.putShort(input_p.getLength());

		buf.putInt(input_p.getAckno());

		if (input_p.getLength() > 8) {

			buf.putInt(input_p.getSeqno());

			buf.put(input_p.getData());

		}

		DatagramPacket output_dg = new DatagramPacket(temp, temp.length, server, port);
		return (output_dg);

	}

	/**
	 * Converts Packet to Datagram Packet
	 * @param newPacket
	 * @return output datagram
	 */
	public DatagramPacket packetToDGPacket(Packet newPacket) {

		Packet input_p = newPacket;

		byte[] temp = new byte[input_p.getLength()];

		ByteBuffer buf = ByteBuffer.wrap(temp);

		buf.putShort(input_p.getCksum());

		buf.putShort(input_p.getLength());

		buf.putInt(input_p.getAckno());

		if (input_p.getLength() > 8) {

			buf.putInt(input_p.getSeqno());

			buf.put(input_p.getData());

		}

		DatagramPacket output_dg = new DatagramPacket(temp, temp.length);

		return (output_dg);

	}

	@Override
	public String toString() {
		return "ClientPacketHandler [packet size=" + packetSize + ", corruption prob=" + corruptionProb
				+ ", failure prob=" + failureProb + "]";
	}

	/**
	 * Converts Datagram packet to Packet
	 * @param dgPacket
	 * @return output packet
	 */
	public Packet dgpacketToPacket(DatagramPacket dgPacket) {

		DatagramPacket input_dg = dgPacket;

		byte[] temp = input_dg.getData();

		ByteBuffer buf = ByteBuffer.wrap(temp);

		short cksum = buf.getShort();

		short length = buf.getShort();

		int ackno = buf.getInt();

		if (length == 8) {

			Packet output_p = new Packet(cksum, ackno);

			return (output_p);

		} else {

			int seqno = buf.getInt();

			byte[] data = new byte[buf.remaining()];

			buf.get(data);

			Packet output_p = new Packet(seqno, data);

			// check for corrupted packet
			if (cksum > 0) {

				output_p.setCksum((short) 1);

			}

			return output_p;

		}

	}

}