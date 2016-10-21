package packet_handler;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.util.concurrent.BlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;

import java.nio.ByteBuffer;

import java.net.*;

public class PacketSender {
	
	// store packets in BlockingQueue for Thread support
	private BlockingQueue<Packet> buffer = new ArrayBlockingQueue<Packet>(1024);
	
	private int packet_size;
	
	public PacketSender() {
		
	}
	
	public void setPacketSize(int packetSize) {
	  
	  packet_size = packetSize;
	  
	}
	
	public int getPacketSize() {
	  
	  return(packet_size);
	  
	}
	
	public byte[] convertFile(File passedFile) {
	  
	  FileInputStream input = null;
	  
	  File file = passedFile;
	  
	  byte[] data = new byte[(int) file.length()];
	  
	  try {
	    
	    input = new FileInputStream(file);
	    
	    input.read(data);
	    
	    input.close();
	    
	    return(data);
	    
	  }
	  
	  catch(Exception ex) {
		  
		    System.out.println("Error in convertFile: " + ex);
		    
		    return(null);
		  
	  }
	  	  
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
			
			// final Packet with null data to mark end of file
			
			byte[] nodata = new byte[0];
			
			Packet last_packet = new Packet(-1, nodata);
			
			buffer.put(last_packet);
			
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
	
	public int getNextPacketSeqno() {
		
		try {
			
			Packet nextPacket = buffer.peek();
			
			if(nextPacket == null) {
			  
				return(0);
			  
			}
			
			return(nextPacket.getSeqno());
				
		}
		
		catch(Exception ex) {
			
			System.out.println("Error in getNextPacketSeqno " + ex);
			
			return(0);
			
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
	  
	  if(input_p.getLength() > 8) {
	    
	    buf.putInt(input_p.getSeqno());
	    
	    buf.put(input_p.getData());
	    
	  }
	  
	  DatagramPacket output_dg = new DatagramPacket(temp, temp.length,
			  server, port);
	  
	  return(output_dg);
	  
	}
	
	public DatagramPacket packetToDGPacket(Packet newPacket) {
	  
	  Packet input_p = newPacket;
	  
	  byte[] temp = new byte[input_p.getLength()];
	  
	  ByteBuffer buf = ByteBuffer.wrap(temp);
	  
	  buf.putShort(input_p.getCksum());
	  
	  buf.putShort(input_p.getLength());
	  
	  buf.putInt(input_p.getAckno());
	  
	  if(input_p.getLength() > 8) {
	    
	    buf.putInt(input_p.getSeqno());
	    
	    buf.put(input_p.getData());
	    
	  }
	  
	  DatagramPacket output_dg = new DatagramPacket(temp, temp.length);
	  
	  return(output_dg);
	  
	}
	
	public Packet dgpacketToPacket(DatagramPacket dgPacket) {
	  
	  DatagramPacket input_dg = dgPacket;
	  
	  byte[] temp = input_dg.getData();
	  
	  ByteBuffer buf = ByteBuffer.wrap(temp);
	  
	  short cksum = buf.getShort();
	  
	  // check for corrupted packet
	  if(cksum > 0) {
	    
	    return(null);
	    
	  }
	  
	  short length = buf.getShort();
	  
	  int ackno = buf.getInt();
	  
	  if(length == 8) {
	    
	    Packet output_p = new Packet(ackno);
	    
	    return(output_p);

	  } else {
	    
	    int seqno = buf.getInt();
	    
	    byte[] data = new byte[buf.remaining()];
	    
	    buf.get(data);
	    
	    Packet output_p = new Packet(seqno, data);
	    
	    return output_p;
	    
	  }
	  
	}
	
}