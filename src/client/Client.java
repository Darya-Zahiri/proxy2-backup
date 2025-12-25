package client;

import common.Checksum;
import common.Packet;

import java.io.ByteArrayOutputStream;
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

            String response = new String(
                    responsePacket.getData(),
                    0,
                    responsePacket.getLength()
            );
            System.out.println("Received from server: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
    }
}
