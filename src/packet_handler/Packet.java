package packet_handler;

public class Packet {

	short cksum;

	short length;

	int ackno;

	int seqno;

	byte[] data;


	/**
	 * Constructor
	 * @param seqno
	 * @param data
	 */
	public Packet(int seqno, byte[] data) {

		this.cksum = 0;

		this.seqno = seqno;

		this.ackno = seqno + 1;

		this.data = data;

		this.length = (short) (12 + data.length);

	}


	/**
	 * Constructor for first packet
	 * @param seqno
	 * @param file_length
	 * @param data
	 */
	public Packet(int seqno, int file_length, byte[] data) {

		this.cksum = 0;

		this.seqno = seqno;

		this.ackno = file_length;

		this.data = data;

		this.length = (short) (12 + data.length);

	}

	/**
	 * Constructor for Ack Packet
	 * @param ackno
	 */
	public Packet(int ackno) {

		this.cksum = 0;

		this.length = 8;

		this.ackno = ackno;

	}

	
	/**
	 * @return cksum
	 */
	public short getCksum() {

		return cksum;

	}


	/**
	 * @return length
	 */
	public short getLength() {

		return length;

	}


	/**
	 * Get sequence number
	 * @return seqno
	 */
	public int getSeqno() {

		return seqno;

	}


	/**
	 * Get Acknowledgment number
	 * @return ackno
	 */
	public int getAckno() {

		return ackno;

	}

	
	/**
	 * @return data
	 */
	public byte[] getData() {

		return data;

	}

	/**
	 * @param newCksum
	 */
	public void setCksum(short newCksum) {

		cksum = newCksum;

	}

	// toString method
	public String toString() {
		return "Packet number " + seqno + " [data=" + (data != null ? arrayToString(data, data.length) : null)
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