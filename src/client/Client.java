package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws Exception {
        String serverIP = "127.0.0.1";
        int serverPort = 12345;
        DatagramSocket socket = new DatagramSocket();
        String message = "Hello,I am Server";
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverIP), serverPort);
        socket.send(packet);

        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("Received from server: " + response);
        socket.close();
    }
}
