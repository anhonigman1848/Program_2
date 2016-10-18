package packet_handler;

public class Packet {
  
  short cksum;
  
  short length;
  
  int ackno;
  
  int seqno;
  
  byte[] data;
  
  // constructor for Data packet
	public Packet(int passedSeqno, byte[] passedData) {
	  
	  cksum = 0;
	  
	  seqno = passedSeqno;
	  
	  ackno = seqno + 1;

	  data = passedData;
		
	  length = (short) (12 + data.length);
	  
	}

  // constructor for Ack packet
	public Packet(int passedAckno) {
	  
	  cksum = 0;
	  
	  length = (short) 8;
	  
	  ackno = passedAckno;
	  
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