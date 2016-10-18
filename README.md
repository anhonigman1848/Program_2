# Program_2

Packet class as defined in project description: byte array of data, sequence number, ack number, length, checksum

Two constructors:

- Data Packets have all fields

- Ack Packets have no byte array or sequence number

PacketSender class takes byte array of arbitrary size, breaks it into Packets, and stores in ArrayBlockingQueue

Use method setPacketSize(int) to set packet size

Use method makePackets(byte[]) to pass data

Use method nextPacket() to remove next packet in queue
