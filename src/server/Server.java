package server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 12345;
        DatagramSocket socket = new DatagramSocket(port);

        System.out.println("Server running on port " + port);
        byte[] buffer = new byte[1024];
        while (true){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received from client: " + message);
            String response = "ACK: " + message;
            DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(),
                    packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
        }
    }
}
