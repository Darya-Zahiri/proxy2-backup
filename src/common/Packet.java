package common;

import java.io.Serializable;

public class Packet implements Serializable {
    public int seq;
    public boolean ack;
    public int checksum;
    public String data;

    public Packet(int seq, boolean ack, String data) {
        this.seq = seq;
        this.ack = ack;
        this.data = data;
        this.checksum = 0;
    }

    @Override
    public String toString() {
        return "[seq=" + seq +
                ", ack=" + ack +
                ", checksum=" + checksum +
                ", data=" + data + "]";
    }
}
