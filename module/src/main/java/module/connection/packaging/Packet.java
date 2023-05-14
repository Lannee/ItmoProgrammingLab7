package module.connection.packaging;

import java.io.Serializable;
import java.util.Arrays;

public class Packet implements Serializable {

    public static final int DATA_SIZE = 1024;  // объем передаваемых данных
    public static final int PACKAGE_SIZE = DATA_SIZE + 130; // такое количество байт занимает данный объект в сериализованном виде

    private final int serialNumber;
    private final int packagesAmount;

    private byte[] data = new byte[DATA_SIZE];

    public Packet(int serialNumber, int packagesAmount) {
        this.serialNumber = serialNumber;
        this.packagesAmount = packagesAmount;
    }

    public Packet(byte[] data, int serialNumber, int packagesAmount) {
        this.data = Arrays.copyOf(data, DATA_SIZE);
        this.serialNumber = serialNumber;
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
}
