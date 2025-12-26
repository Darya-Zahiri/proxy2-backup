package server;
import common.Checksum;
import common.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = null;

        try {
            int port = 12345;
            socket = new DatagramSocket(port);
            System.out.println("Server running on port " + port);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // deserialize Packet
                ByteArrayInputStream bis =
                        new ByteArrayInputStream(
                                packet.getData(),
                                0,
                                packet.getLength()
                        );
                ObjectInputStream ois =
                        new ObjectInputStream(bis);

                Packet pkt = (Packet) ois.readObject();
                boolean ok = Checksum.verify(pkt.data, pkt.checksum);

                if (!ok) {
                    System.out.println("[RDT] CORRUPTED packet dropped");
                    continue; // ACK نفرست
                }else{
                    System.out.println(
                            "[RDT] OK seq=" + pkt.seq +
                                    " data=" + pkt.data
                    );

                }

                // ساخت ACK Packet
                Packet ackPkt = new Packet(pkt.seq, true, "ACK");
                ackPkt.checksum = Checksum.calculate(ackPkt.data);

// serialize ACK Packet
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(ackPkt);
                byte[] ackData = bos.toByteArray();

                DatagramPacket responsePacket =
                        new DatagramPacket(
                                ackData,
                                ackData.length,
                                packet.getAddress(),
                                packet.getPort()
                        );

                socket.send(responsePacket);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
    }
}
