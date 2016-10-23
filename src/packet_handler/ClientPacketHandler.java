package packet_handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.nio.ByteBuffer;
import java.net.*;

public class ClientPacketHandler {
	
	UDPClient udpClient;
	
	// store packets in BlockingQueue for Thread support
	private BlockingQueue<Packet> buffer = new ArrayBlockingQueue<Packet>(1024);

	// store sent packets in Packet array
	private Packet[] window;

	private volatile int lastAckReceived;

	private int packet_size;
	
	// probability that cksum of any Packet will be corrupted
	private double corruption_prob;
	
	// probability that a Packet will fail on sending
	private double failure_prob;

	public ClientPacketHandler(int packet_size, double corruption_prob, double failure_prob, UDPClient udpClient) {
		
		this.packet_size = packet_size;
		
		this.corruption_prob = corruption_prob;
		
		this.failure_prob = failure_prob;

		this.window = new Packet[1];
		
		this.lastAckReceived = 1;
	
		this.udpClient = udpClient;

	}

	public int getPacketSize() {

		return (packet_size);

	}

	public synchronized void setLastAckReceived(int ackno) {

		this.lastAckReceived = ackno;

	}

	public synchronized int getLastAckReceived() {

		return (lastAckReceived);

	}
	
	public boolean failureCheck() {
		
		if (Math.random() < failure_prob) {
			
			//System.out.println("Failed to send packet!");
			udpClient.setOutputMessage("Failed to send packet!");

			return(true);
			
		}
		
		else {
			
			return(false);
			
		}
		
	}

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

	public void makePackets(byte[] data) {

		int seqno = 1;

		try {

			for (int i = 0; i < data.length; i += packet_size) {

				int current_size = packet_size;

				if (data.length - i < packet_size) {

					current_size = data.length - i;

				}

				byte[] packet_data = new byte[current_size];

				System.arraycopy(data, i, packet_data, 0, current_size);

				Packet new_packet = new Packet(seqno, packet_data);

				buffer.put(new_packet);

				// System.out.println("Added " + new_packet.toString());

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

	public int getBufferSize() {

		return (buffer.size());

	}

	public Packet nextPacket() {

		try {

			// check whether last Packet sent has been acked
			if (getNextPacketSeqno() > lastAckReceived) {

				return (window[0]);

			}

			else {

				Packet nextPacket = buffer.take();

				// save "clean" copy of nextPacket in window
				Packet tempPacket = new Packet(nextPacket.getSeqno(),
						nextPacket.getData());
				
				window[0] = tempPacket;
				
				if (Math.random() < corruption_prob) {
					
					nextPacket.setCksum((short) 1);
					
					//System.out.println("Sending Corrupted packet! " + nextPacket.getSeqno());
					udpClient.setOutputMessage("Sending Corrupted packet! " + nextPacket.getSeqno());
					
					
				}

				return (nextPacket);

			}

		}

		catch (Exception ex) {

			System.out.println("Error in nextPacket " + ex);

			return (null);

		}

	}

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

	public DatagramPacket packetToDGPacket(Packet newPacket,
			InetAddress server, int port) {

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

		DatagramPacket output_dg = new DatagramPacket(temp, temp.length,
				server, port);

		return (output_dg);

	}

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

	public Packet dgpacketToPacket(DatagramPacket dgPacket) {

		DatagramPacket input_dg = dgPacket;

		byte[] temp = input_dg.getData();

		ByteBuffer buf = ByteBuffer.wrap(temp);

		short cksum = buf.getShort();

		short length = buf.getShort();

		int ackno = buf.getInt();

		if (length == 8) {

			Packet output_p = new Packet(ackno);

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