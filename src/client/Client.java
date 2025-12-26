package client;

import common.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    static final int TIMEOUT = 2000;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("P_drop: ");
        double pDrop = sc.nextDouble();
        System.out.print("P_corrupt: ");
        double pCorrupt = sc.nextDouble();
        System.out.print("P_duplicate: ");
        double pDuplicate = sc.nextDouble();
        sc.nextLine();

        UnreliableChannel channel =
                new UnreliableChannel(pDrop, pCorrupt, pDuplicate);

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT);

        InetAddress addr = InetAddress.getByName("127.0.0.1");
        int port = 12345;

        System.out.println("Select mode:");
        System.out.println("1) Plain UDP");
        System.out.println("2) RDT 3.0");
        System.out.print("Choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) {
            plainUDP(sc, socket, addr, port, channel);
        } else {
            rdt(sc, socket, addr, port, channel);
        }

        socket.close();
        sc.close();
    }

    // ---------- Plain UDP ----------
    static void plainUDP(Scanner sc, DatagramSocket socket,
                         InetAddress addr, int port,
                         UnreliableChannel channel) throws Exception {

        while (true) {
            System.out.print("Message> ");
            String msg = sc.nextLine();
            if (msg.equals("quit")) break;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(1); // mode
            bos.write(msg.getBytes());

            DatagramPacket dp =
                    new DatagramPacket(bos.toByteArray(), bos.size(), addr, port);
            channel.send(socket, dp);
        }
    }

    // ---------- RDT ----------
    static void rdt(Scanner sc, DatagramSocket socket,
                    InetAddress addr, int port,
                    UnreliableChannel channel) throws Exception {

        int seq = 0;

        while (true) {
            System.out.print("Message> ");
            String msg = sc.nextLine();
            if (msg.equals("quit")) break;

            Packet pkt = new Packet(seq, false, msg);
            pkt.checksum = Checksum.calculate(pkt.data);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(2); // mode
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(pkt);

            byte[] data = bos.toByteArray();
            boolean acked = false;

            while (!acked) {
                channel.send(socket,
                        new DatagramPacket(data, data.length, addr, port));

                try {
                    byte[] buf = new byte[2048];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    socket.receive(dp);

                    ByteArrayInputStream bis =
                            new ByteArrayInputStream(dp.getData(), 1, dp.getLength() - 1);
                    Packet ack =
                            (Packet) new ObjectInputStream(bis).readObject();

                    if (ack.ack && ack.seq == seq &&
                            Checksum.verify(ack.data, ack.checksum)) {
                        System.out.println("[CLIENT] ACK " + seq);
                        acked = true;
                        seq = 1 - seq;
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("[CLIENT] Timeout, resend");
                }
            }
        }
    }
}
