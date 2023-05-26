package module.connection.packaging;

import java.io.*;
import java.util.Arrays;

import static java.lang.Math.*;

public class PacketManager {

    public static Packet[] split(Serializable object) {
        Packet[] packets;
        byte[] data = getBytesFromObj(object);

        int packagesAmount = (int) ceil((double) data.length / (double) Packet.DATA_SIZE);
        byte[] dataToSend = Arrays.copyOf(data, Packet.DATA_SIZE * packagesAmount);

        packets = new Packet[packagesAmount];

        for (int i = 0; i < packagesAmount; i++) {
            packets[i] = new Packet(
                    Arrays.copyOfRange(
                            dataToSend,
                            Packet.DATA_SIZE * i,
                            Packet.DATA_SIZE * (i + 1)),
                    i,
                    packagesAmount);
        }

        return packets;
    }

    public static byte[] serialize(Object packet) {
        byte[] dataToSend;
        try {
            ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            ObjectOutputStream objOS = new ObjectOutputStream(byteOS);
            objOS.writeObject(packet);
            dataToSend = byteOS.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataToSend;
    }

    public static Serializable deserialize(byte[] byteObject) {
        Serializable out;
        try {
            ByteArrayInputStream byteOS = new ByteArrayInputStream(byteObject);
            ObjectInputStream objIS = new ObjectInputStream(byteOS);
            out = (Serializable) objIS.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    public static Serializable assemble(Packet[] packets) {
        Arrays.sort(packets);
        byte[] byteObject = new byte[Packet.DATA_SIZE * packets.length];

        for (int i = 0; i < packets.length; i++) {
            byte[] packetData = packets[i].getData();

            for (int j = 0; j < Packet.DATA_SIZE; j++) {
                byteObject[i * Packet.DATA_SIZE + j] = packetData[j];
            }
        }

        return deserialize(byteObject);
    }

    public static byte[] getBytesFromObj(Serializable object) {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        ObjectOutputStream objOS;
        byte[] data = new byte[0];
        try {
            objOS = new ObjectOutputStream(byteOS);

            objOS.writeObject(object);
            data = byteOS.toByteArray();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
}
