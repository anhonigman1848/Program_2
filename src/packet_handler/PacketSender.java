package packet_handler;

import java.util.concurrent.BlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;

public class PacketSender {
	
	// store packets in BlockingQueue for Thread support
	private BlockingQueue<Packet> buffer = new ArrayBlockingQueue<Packet>(10);
	
	private int packet_size;
	
	public PacketSender() {
		
	}
	
	public void setPacketSize(int packetSize) {
	  
	  packet_size = packetSize;
	  
	}
	
	public int getPacketSize() {
	  
	  return(packet_size);
	  
	}
	
	public void makePackets(byte[] data) {
	  
	  int seqno = 1;
		
		try {
			
			for(int i = 0; i < data.length; i += packet_size) {
				
				int current_size = packet_size;
				
				if(data.length - i < packet_size) {
					
					current_size = data.length - i;
										
				}
				byte[] packet_data = new byte[current_size];
				
				System.arraycopy(data, i, packet_data, 0, current_size);
				
				Packet new_packet = new Packet(seqno, packet_data);
				
				buffer.put(new_packet);
				
				System.out.println("Added " + new_packet.toString());
				
				seqno++;
				
			}
			
		} catch(Exception ex) {
		  
		    System.out.println("Error in makePackets: " + ex);
		  
		}
		
	}
	
	public int getBufferSize() {
		
		return(buffer.size());
		
	}
	
	public Packet nextPacket() {
		
		try {
			
			Packet nextPacket = buffer.take();
			
			return(nextPacket);
				
		}
		
		catch(Exception ex) {
			
			System.out.println("Error in nextPacket " + ex);
			
			return(null);
			
		}
		
	}
	
}