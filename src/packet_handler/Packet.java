package packet_handler;

public class Packet {
  
  short cksum;
  
  short length;
  
  int ackno;
  
  int seqno;
  
  byte[] data;
  
  // constructor for Data packet
	public Packet(int seqno, byte[] data) {
	  
	  this.cksum = 0;
	  
	  this.seqno = seqno;
	  
	  this.ackno = seqno + 1;

	  this.data = data;
		
	  this.length = (short) (12 + data.length);
	  
	}

	  // constructor for first packet
		public Packet(int seqno, int file_length, byte[] data) {
		  
		  this.cksum = 0;
		  
		  this.seqno = seqno;
		  
		  this.ackno = file_length;

		  this.data = data;
			
		  this.length = (short) (12 + data.length);
		  
		}

  // constructor for Ack packet
	public Packet(int ackno) {
	  
	  this.cksum = 0;
	  
	  this.length = 8;
	  
	  this.ackno = ackno;
	  
	}

	// getter for cksum
	public short getCksum() {
	  
		return cksum;
		
	}

	// getter for length
	public short getLength() {
	  
		return length;
		
	}

	// getter for seqno
	public int getSeqno() {
	  
		return seqno;
		
	}

	// getter for ackno
	public int getAckno() {
	  
		return ackno;
		
	}

	// getter for data
	public byte[] getData() {
	  
		return data;
		
	}
	
	// setter for chksum
	public void setCksum(short newCksum) {
	  
	  cksum = newCksum;
	  
	}

	// toString method
	public String toString() {
		return "Packet number " + seqno + " [data="
				+ (data != null ? arrayToString(data, data.length) : null)
				+ "] ackno " + ackno + " cksum " + cksum + " length " + length;
	}

	private String arrayToString(Object array, int len) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (int i = 0; i < len; i++) {
			if (i > 0)
				buffer.append(", ");
			if (array instanceof byte[])
				buffer.append(((byte[]) array)[i]);
		}
		buffer.append("]");
		return buffer.toString();
	}
	
}