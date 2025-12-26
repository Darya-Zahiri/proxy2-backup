package client;

import common.Checksum;
import common.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = null;

        try {
            String serverIP = "127.0.0.1";
            int serverPort = 12345;
            socket = new DatagramSocket();

            // ساخت Packet
            Packet pkt = new Packet(0, false, "Hello, I am Client");
            pkt.checksum = Checksum.calculate(pkt.data);
            pkt.data = "CORRUPTED DATA";



            // serialize Packet -> byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(pkt);
            byte[] data = bos.toByteArray();

            // ارسال Packet
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName(serverIP),
                    serverPort
            );
            socket.send(packet);


            // دریافت پاسخ (فعلاً String یا Packet، مهم نیست)
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket =
                    new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);


// deserialize ACK Packet
            ByteArrayInputStream bis =
                    new ByteArrayInputStream(
                            responsePacket.getData(),
                            0,
                            responsePacket.getLength()
                    );
            ObjectInputStream ois =
                    new ObjectInputStream(bis);

            Packet ackPkt = (Packet) ois.readObject();

// بررسی ACK
            if (ackPkt.ack && Checksum.verify(ackPkt.data, ackPkt.checksum)) {
                System.out.println(
                        "[CLIENT] ACK received for seq=" + ackPkt.seq
                );
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
    }
}
