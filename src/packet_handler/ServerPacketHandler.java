package packet_handler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.net.*;

public class ServerPacketHandler {

	UDPServer udpServer;

	// store incoming packets in ArrayList
	private ArrayList<byte[]> buffer;

	// store name of file to write
	private String file_name;

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

	public ServerPacketHandler(int packet_size, double failure_prob, double corruption_prob, UDPServer udpServer) {

		this.packet_size = packet_size;

		this.corruption_prob = corruption_prob;

		this.failure_prob = failure_prob;

		this.window = new Packet[1];

		this.lastPacketReceived = -1;

		this.udpServer = udpServer;

		this.buffer = new ArrayList<byte[]>();

		this.bytes_stored = 0;

	}

	public int getPacketSize() {
		return (packet_size);
	}

	public void setLastPacketReceived(int seqno) {
		this.lastPacketReceived = seqno;
	}

	public synchronized int getLastPacketReceived() {
		return (lastPacketReceived);
	}

	// check for failure to send ack Packet
	public boolean failureCheck() {
		if (Math.random() < failure_prob) {
			return (true);
		} else {
			return (false);
		}

	}

	public int getBufferSize() {
		return (buffer.size());
	}

	public String setFile_name(Packet packet) {

		setLastPacketReceived(packet.getSeqno());
		byte[] name_in_bytes = packet.getData();
		String name = new String(name_in_bytes);
		this.file_name = "COPY_OF_" + name;
		return (name);

	}

	// put received Packet data into buffer
	public void addToBuffer(Packet packet) {

		if (packet.getSeqno() > lastPacketReceived) {

			setLastPacketReceived(packet.getSeqno());
			byte[] data = packet.getData();
			buffer.add(data);
			bytes_stored += data.length;

		}

	}

	// convert a Packet to a DatagramPacket for sending over UDP
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

	public int getBytes_stored() {
		return bytes_stored;
	}

	// convert Packet to DatagramPacket, without IP address or port
	// not sure if this method is needed
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

	// convert DatagramPacket to Packet, to read fields
	public Packet dgpacketToPacket(DatagramPacket dgPacket) {

		DatagramPacket input_dg = dgPacket;

		byte[] temp = input_dg.getData();

		ByteBuffer buf = ByteBuffer.wrap(temp);

		short cksum = buf.getShort();

		short length = buf.getShort();

		int ackno = buf.getInt();

		// if length 8, this is an ack Packet, so don't worry about data
		// Server shouldn't be getting ack Packets - this block may not be
		// needed
		if (length == 8) {

			Packet output_p = new Packet(ackno);

			return (output_p);

		}

		// otherwise, this is a data Packet, so get seqno and data
		// Server should only be getting data packets
		else {

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

	// toString method, for testing
	@Override
	public String toString() {
		return "ServerPacketHandler [packet_size=" + packet_size + ", corruption_prob=" + corruption_prob
				+ ", failure_prob=" + failure_prob + "]";
	}

}