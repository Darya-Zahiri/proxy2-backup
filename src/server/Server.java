package server;
import common.Packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

                System.out.println(
                        "[RDT] Got seq=" + pkt.seq +
                                " data=" + pkt.data
                );
                String response = "ACK";
                DatagramPacket responsePacket =
                        new DatagramPacket(
                                response.getBytes(),
                                response.length(),
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
