package common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

public class UnreliableChannel {
    private final double pDrop;
    private final double pCorrupt;
    private final double pDuplicate;
    private final Random rand = new Random();

    public UnreliableChannel(double pDrop, double pCorrupt, double pDuplicate) {
        this.pDrop = pDrop;
        this.pCorrupt = pCorrupt;
        this.pDuplicate = pDuplicate;
    }

    public void send(DatagramSocket socket, DatagramPacket packet) throws Exception {

        double r = rand.nextDouble();

        if (r < pDrop) {
            System.out.println("[CHANNEL] Packet DROPPED");
            return;
        }
        if (r < pDrop + pCorrupt) {
            System.out.println("[CHANNEL] Packet CORRUPTED");
            byte[] data = packet.getData();
            data[0] ^= 0xFF; // خراب کردن بایت اول
        }

        socket.send(packet);
        System.out.println("[CHANNEL] Packet SENT");

        if (rand.nextDouble() < pDuplicate) {
            System.out.println("[CHANNEL] Packet DUPLICATED");
            socket.send(packet);
        }
    }
}
