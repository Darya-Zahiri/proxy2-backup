package client;

import common.Checksum;
import common.Packet;

import java.io.*;
import java.net.*;

public class Client {

    static final int TIMEOUT = 2000;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT);

        InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
        int serverPort = 12345;

        int seq = 0;
        boolean acked = false;

        Packet pkt = new Packet(seq, false, "Hello, I am Client");
        pkt.checksum = Checksum.calculate(pkt.data);

        byte[] sendData = serialize(pkt);

        while (!acked) {
            DatagramPacket sendPkt =
                    new DatagramPacket(sendData, sendData.length, serverAddr, serverPort);
            socket.send(sendPkt);
            System.out.println("[CLIENT] Packet sent seq=" + seq);

            try {
                byte[] buffer = new byte[1024];
                DatagramPacket recvPkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(recvPkt);

                Packet ackPkt = deserialize(recvPkt);

                if (ackPkt.ack &&
                        ackPkt.seq == seq &&
                        Checksum.verify(ackPkt.data, ackPkt.checksum)) {

                    System.out.println("[CLIENT] ACK received seq=" + seq);
                    acked = true;
                }

            } catch (SocketTimeoutException e) {
                System.out.println("[CLIENT] Timeout, resending seq=" + seq);
            }
        }

        socket.close();
    }

    static byte[] serialize(Packet pkt) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(pkt);
        return bos.toByteArray();
    }

    static Packet deserialize(DatagramPacket dp) throws Exception {
        ByteArrayInputStream bis =
                new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (Packet) ois.readObject();
    }
}
