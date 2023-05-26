package src.connection;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;
import module.connection.packaging.Packet;
import module.connection.packaging.PacketManager;

import static java.lang.Math.ceil;

public class ObjectDevider extends RecursiveTask<Packet[]> {

    private byte[] byteArrayToSending;

    public ObjectDevider(byte[] aByteArrayToSending) {
        byteArrayToSending = aByteArrayToSending;
    }

    @Override
    protected Packet[] compute() {
        Packet[] packets;
        int packagesAmount = (int) ceil((double) byteArrayToSending.length / (double) Packet.DATA_SIZE);
        byte[] dataToSend = Arrays.copyOf(byteArrayToSending, Packet.DATA_SIZE * packagesAmount);
        
        packets = new Packet[packagesAmount];
        for (int i = 0; i < packagesAmount; i++) {
            packets[i] = new Packet(
                    Arrays.copyOfRange(
                            dataToSend,
                            Packet.DATA_SIZE * i,
                            Packet.DATA_SIZE * (i + 1)),
                    packagesAmount);
        }

        return packets;
        // int packagesAmount = (int) ceil((double) byteArrayToSending.length / (double) Packet.DATA_SIZE);
        // if (PackageAmountStorage.maxPackagesAmount == 0) {
        //     PackageAmountStorage.setMaxPackagesAmount(packagesAmount);
        // }
        // if (packagesAmount <= 2) {
        //     byte[] dataToSend = Arrays.copyOf(byteArrayToSending, Packet.DATA_SIZE * packagesAmount);
        //     packets = new Packet[packagesAmount];
        //     for (int i = 0; i < packagesAmount; i++) {
        //         packets[i] = new Packet(
        //                 Arrays.copyOfRange(dataToSend,
        //                         Packet.DATA_SIZE * i,
        //                         Packet.DATA_SIZE * (i + 1)),
        //                 PackageAmountStorage.maxPackagesAmount);
        //     }
        //     return packets;
        // }

        // // deviding byte array into two byte arrays
        // ObjectDevider firstHalfByteArray = new ObjectDevider(
        //         Arrays.copyOfRange(byteArrayToSending, 0, byteArrayToSending.length / 2));
        // ObjectDevider secondHalfByteArray = new ObjectDevider(
        //         Arrays.copyOfRange(byteArrayToSending, byteArrayToSending.length / 2, byteArrayToSending.length));

        // // executingRecursiveTask.fork()
        // firstHalfByteArray.fork();
        // secondHalfByteArray.fork();

        // // combining results
        // Packet[] firstHalfResult = firstHalfByteArray.join();
        // Packet[] secondHalfResult = secondHalfByteArray.join();

        // Packet[] result = Arrays.copyOf(firstHalfResult, firstHalfResult.length + secondHalfResult.length);
        // System.arraycopy(secondHalfResult, 0, result, firstHalfResult.length, secondHalfResult.length);
        // return result;
    }

    public byte[] getByteArrayToSending() {
        return byteArrayToSending;
    }
}
