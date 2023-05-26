package module.connection.packaging;

import java.io.Serializable;
import java.util.Arrays;

public class Packet implements Serializable, Comparable<Packet> {
    private static final long serialVersionUID = 6529685098267717622L;

    public static final int DATA_SIZE = 1024;  // объем передаваемых данных
    public static final int PACKAGE_SIZE = DATA_SIZE + 130; // такое количество байт занимает данный объект в сериализованном виде

    private static int counter = 0;
    
    private final int serialNumber;
    private int packagesAmount;

    public void setPackagesAmount(int packagesAmount) {
        this.packagesAmount = packagesAmount;
    }

    private byte[] data = new byte[DATA_SIZE];

    public Packet(int serialNumber, int packagesAmount) {
        this.serialNumber = serialNumber;
        this.packagesAmount = packagesAmount;
    }

    public Packet(byte[] data, int packagesAmount) {
        this.data = Arrays.copyOf(data, DATA_SIZE);
        // this.serialNumber = serialNumber;
        this.serialNumber = counter++;
        this.packagesAmount = packagesAmount;
    }

    public byte[] getData() {
        return data;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public int getPackagesAmount() {
        return packagesAmount;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "serialNumber=" + serialNumber +
                ", packagesAmount=" + packagesAmount +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int newCounterVal) {
        counter = newCounterVal;
    }

    @Override
    public int compareTo(Packet arg0) {
        return serialNumber - arg0.serialNumber;
    }
}
