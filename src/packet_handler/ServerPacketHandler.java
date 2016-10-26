package packet_handler;

import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;

public class ServerPacketHandler {

	UDPServer udpServer;

	// store incoming packets in ArrayList
	private byte[] buffer;

	// store name of file to write
	private String file_name;

	// store length of file to write
	private int file_length;

	// keep track of how many bytes are in buffer
	private int bytes_stored;

	// store sent packets in Packet array
	// this isn't used now; may be needed for Program 3 iteration
	private Packet[] window;

	// Server needs to know seqno of last good packet
	private int lastPacketReceived;

	// packet size, should be the same for all classes
	private int packet_size;

	// probability that cksum of any Packet will be corrupted
	private double corruption_prob;

	// probability that a Packet will fail on sending
	private double failure_prob;

	/**
	 * Constructor
	 * @param packet_size
	 * @param failure_prob
	 * @param corruption_prob
	 * @param udpServer
	 */
	public ServerPacketHandler(int packet_size, double failure_prob, double corruption_prob, UDPServer udpServer) {

		this.packet_size = packet_size;

		this.corruption_prob = corruption_prob;

		this.failure_prob = failure_prob;

		this.window = new Packet[1];

		this.lastPacketReceived = -1;

		this.udpServer = udpServer;

		this.bytes_stored = 0;

	}

	/**
	 * @return packet_size
	 */
	public int getPacketSize() {
		return (packet_size);
	}

	/**
	 * @param seqno
	 */
	public void setLastPacketReceived(int seqno) {
		this.lastPacketReceived = seqno;
	}

	/**
	 * @return lastPacketReceived
	 */
	public synchronized int getLastPacketReceived() {
		return (lastPacketReceived);
	}


	/**
	 * Check for failure to send ack Packet
	 * @return boolean
	 */
	public boolean failureCheck() {
		if (Math.random() < failure_prob) {
			return (true);
		} else {
			return (false);
		}

	}

	/**
	 * @return buffer.length
	 */
	public int getBufferLength() {
		return (buffer.length);
	}

	/**
	 * @param packet
	 */
	public void setFile_name(Packet packet) {

		setLastPacketReceived(packet.getSeqno());
		byte[] name_in_bytes = packet.getData();
		String name = new String(name_in_bytes);
		this.file_name = "COPY_OF_" + name;

	}

	/**
	 * @param length
	 */
	public void setFile_length(int length) {
		this.file_length = length;
		System.out.println("Buffer length " + file_length);
		buffer = new byte[file_length];
	}

	/**
	 * Put received Packet data into buffer
	 * @param packet
	 */
	public void addToBuffer(Packet packet) {

		if (packet.getSeqno() > lastPacketReceived) {

			setLastPacketReceived(packet.getSeqno());
			byte[] data = packet.getData();
			System.arraycopy(data, 0, buffer, bytes_stored, data.length);
			bytes_stored += data.length;

		}

	}
	
	/**
	 * Write to new file 
	 */
	public void outputFile() {
		
		try {
			File file = new File(file_name);
			if (!file.exists()){
				file.createNewFile();
			}
			FileOutputStream output_f = new FileOutputStream(file);
			output_f.write(buffer);
			output_f.flush();
			output_f.close();
			

		} catch (Exception ex) {
			System.out.println("Error writing file");

		}
		
	
	}


	/**
	 * Convert a Packet to a DatagramPacket for sending over UDP
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
	 * @return bytes_stored
	 */
	public int getBytes_stored() {
		return bytes_stored;
	}


	/**
	 * Convert Packet to DatagramPacket, without IP address or port
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

	/**
	 * Convert DatagramPacket to Packet, to read fields
	 * @param dgPacket
	 * @return output packet
	 */
	public Packet dgpacketToPacket(DatagramPacket dgPacket) {

		DatagramPacket input_dg = dgPacket;

		int data_length = input_dg.getLength() - 12;

		byte[] temp = input_dg.getData();

		ByteBuffer buf = ByteBuffer.wrap(temp);

		short cksum = buf.getShort();

		short length = buf.getShort();

		int ackno = buf.getInt();
		
		// if length 8, this is an ack Packet, so don't worry about data
		// Server shouldn't be getting ack Packets
		if (length == 8) {

			Packet output_p = new Packet(ackno);

			return (output_p);

		}

		// otherwise, this is a data Packet, so get seqno and data
		// Server should only be getting data packets
		else {

			int seqno = buf.getInt();

			byte[] data = new byte[data_length];

			buf.get(data);

			Packet output_p = new Packet(seqno, ackno, data);

			// check for corrupted packet
			if (cksum > 0) {

				output_p.setCksum((short) 1);

			}

			return output_p;

		}

	}

	// toString method, for testing
	@Override
	public String toString() {
		return "ServerPacketHandler [packet_size=" + packet_size + ", corruption_prob=" + corruption_prob
				+ ", failure_prob=" + failure_prob + "]";
	}

}