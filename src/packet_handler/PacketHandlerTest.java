package packet_handler;

import java.io.File;

import java.io.FileInputStream;

import java.net.DatagramPacket;

public class PacketHandlerTest {

	public static void main(String[] args) {
		
		byte[] test = new byte[] {11, 12, 13, 14, 15, 25, 35, 45, 55};
		
		PacketSender sender = new PacketSender();
		
		sender.setPacketSize(5);
		
		sender.makePackets(test);
		
		Packet next = sender.nextPacket();
		
		System.out.println(next.toString());
		
		DatagramPacket testdg = sender.packetToDGPacket(sender.nextPacket());

		System.out.println(testdg.toString());
		
		Packet testpkt = sender.dgpacketToPacket(testdg);
		
		System.out.println(testpkt.toString());
		
		File testfile = new File("testfile.txt");
		
		PacketSender sender2 = new PacketSender();
		
		sender2.setPacketSize(16);
		
		byte[] testbyte = sender2.convertFile(testfile);
		
		sender2.makePackets(testbyte);


	}

}
