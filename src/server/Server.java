package server;

import common.Checksum;
import common.Packet;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(12345);
        System.out.println("Server running on port 12345");

        int expectedSeq = 0;
        Packet lastAck = null;

        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            socket.receive(dp);

            Packet pkt = deserialize(dp);

            if (!Checksum.verify(pkt.data, pkt.checksum)) {
                System.out.println("[RDT] CORRUPTED packet dropped");
                continue;
            }

            if (pkt.seq == expectedSeq) {
                System.out.println("[RDT] Delivered seq=" + pkt.seq +
                        " data=" + pkt.data);

                Packet ack = new Packet(pkt.seq, true, "ACK");
                ack.checksum = Checksum.calculate(ack.data);
                lastAck = ack;

                sendAck(socket, dp, ack);
                expectedSeq = 1 - expectedSeq;

            } else {
                System.out.println("[RDT] Duplicate packet seq=" + pkt.seq);
                if (lastAck != null) {
                    sendAck(socket, dp, lastAck);
                }
            }
        }
    }

    static void sendAck(DatagramSocket socket, DatagramPacket dp, Packet ack)
            throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ack);
        byte[] data = bos.toByteArray();

        DatagramPacket ackDp =
                new DatagramPacket(data, data.length,
                        dp.getAddress(), dp.getPort());
        socket.send(ackDp);
    }

    static Packet deserialize(DatagramPacket dp) throws Exception {
        ByteArrayInputStream bis =
                new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (Packet) ois.readObject();
    }
}
