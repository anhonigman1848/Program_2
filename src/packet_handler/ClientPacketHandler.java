package packet_handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.nio.ByteBuffer;
import java.net.*;

public class ClientPacketHandler {

	UDPClient udpClient;

	// store packets in BlockingQueue for Thread support
	private BlockingQueue<Packet> buffer;

	// keep track of how many bytes sent
	private int bytes_sent;
	
	//Size of the sliding window
	private int window_size;
	
	// store sent packets in Packet array
	private Packet[] window;
	
	private volatile int lastAckReceived;

	private int packet_size;

	// probability that cksum of any Packet will be corrupted
	private double corruption_prob;

	// probability that a Packet will fail on sending
	private double failure_prob;


	/**
	 * Constructor
	 * @param packet_size
	 * @param corruption_prob
	 * @param failure_prob
	 * @param udpClient
	 */
	public ClientPacketHandler(int packet_size, int window_size, double corruption_prob, double failure_prob, UDPClient udpClient) {

		this.packet_size = packet_size;
		
		//System.out.println("ClientPacketHandler: Setting window_size to: " + window_size);
		this.window_size = window_size;

		this.corruption_prob = corruption_prob;

		this.failure_prob = failure_prob;

		this.window = new Packet[window_size];

		this.lastAckReceived = 0;

		this.udpClient = udpClient;

		this.bytes_sent = 0;

	}

	/**
	 * @return packet_size
	 */
	public int getPacketSize() {

		return (packet_size);

	}

	/**
	 * @return bytes_sent
	 */
	public int getBytesSent() {

		return (bytes_sent);

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

	/**
	 * Method to simulate failures
	 * @return boolean
	 */
	public boolean failureCheck() {

		if (Math.random() < failure_prob) {

			// System.out.println("Failed to send packet!");
			udpClient.setOutputMessage("Failed to send packet, no response from Server!");

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

		int queue_size = data.length / packet_size + 3;

		buffer = new ArrayBlockingQueue<Packet>(queue_size);

		try {
			buffer.put(first_packet);
		} catch (InterruptedException ex) {

		}

		try {

			for (int i = 0; i < data.length; i += packet_size) {

				int current_size = packet_size;

				if (data.length - i < packet_size) {

					current_size = data.length % packet_size;

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
	 * @return nextPacket
	 */
	public Packet nextPacket() {

		try {

			// check whether last Packet sent has been acked
			if (getNextPacketSeqno() > lastAckReceived) {

				return (window[0]);

			// check if buffer is empty
			} else if (buffer.peek() == null) {
				return (window[0]);
			}

			else {

				Packet nextPacket = buffer.take();

				bytes_sent += nextPacket.getData().length;

				// save "clean" copy of nextPacket in window
				Packet tempPacket = new Packet(nextPacket.getSeqno(),
						nextPacket.getAckno(),nextPacket.getData());

				window[0] = tempPacket;

				if (Math.random() < corruption_prob && (tempPacket.getSeqno() > 0)) {

					nextPacket.setCksum((short) 1);

					udpClient.setOutputMessage("Corrupted packet! " + nextPacket.getSeqno());

				}

				return (nextPacket);

			}

		}

		catch (Exception ex) {

			System.out.println("Error in nextPacket " + ex);

			return (null);

		}

	}

	/**
	 * @return nextPacket Sequence number
	 */
	public int getNextPacketSeqno() {

		try {

			Packet nextPacket = buffer.peek();

			if (nextPacket == null) {

				return (0);

			}

			return (nextPacket.getSeqno());

		}

		catch (Exception ex) {

			System.out.println("Error in getNextPacketSeqno " + ex);

			return (0);

		}

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
		return "ClientPacketHandler [packet_size=" + packet_size + ", corruption_prob=" + corruption_prob
				+ ", failure_prob=" + failure_prob + "]";
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