package common;

public class Checksum {
    public static int calculate(String data) {
        int sum = 0;
        for (char c : data.toCharArray()) {
            sum += c;
        }
        return sum;
    }

    public static boolean verify(String data, int checksum) {
        return calculate(data) == checksum;
    }
}
