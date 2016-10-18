package packet_handler;

public class PacketHandlerTest {

	public static void main(String[] args) {
		
		byte[] test = new byte[] {11, 12, 13, 14, 15, 25, 35, 45, 55};
		
		PacketSender sender = new PacketSender();
		
		sender.setPacketSize(5);
		
		sender.makePackets(test);
		
		Packet next = sender.nextPacket();
		
		System.out.println(next.toString());

	}

}
