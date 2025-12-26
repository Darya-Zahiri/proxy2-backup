package server;

import common.Checksum;
import common.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(12345);
        System.out.println("Server running on port 12345");

        int expectedSeq = 0;
        Packet lastAck = null;

        byte[] buffer = new byte[2048];

        while (true) {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            socket.receive(dp);

            byte mode = dp.getData()[0];

            // ---------- Plain UDP ----------
            if (mode == 1) {
                String msg = new String(dp.getData(), 1, dp.getLength() - 1);
                System.out.println("[PLAIN] " + msg);
                continue;
            }

            // ---------- RDT ----------
            if (mode != 2) continue;

            ByteArrayInputStream bis =
                    new ByteArrayInputStream(dp.getData(), 1, dp.getLength() - 1);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Packet pkt = (Packet) ois.readObject();

            if (!Checksum.verify(pkt.data, pkt.checksum)) {
                System.out.println("[RDT] CORRUPTED packet dropped");
                continue;
            }

            if (pkt.ack) continue;

            if (pkt.seq == expectedSeq) {
                System.out.println("[RDT] Delivered: " + pkt.data);

                Packet ack = new Packet(pkt.seq, true, "ACK");
                ack.checksum = Checksum.calculate(ack.data);
                lastAck = ack;

                sendAck(socket, dp, ack);
                expectedSeq = 1 - expectedSeq;

            } else {
                System.out.println("[RDT] Duplicate packet");
                if (lastAck != null)
                    sendAck(socket, dp, lastAck);
            }
        }
    }

    static void sendAck(DatagramSocket socket, DatagramPacket dp, Packet ack)
            throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(2); // mode
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ack);

        DatagramPacket ackDp =
                new DatagramPacket(
                        bos.toByteArray(),
                        bos.size(),
                        dp.getAddress(),
                        dp.getPort());

        socket.send(ackDp);
    }
}
