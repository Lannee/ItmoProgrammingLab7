package module.connection;

import module.connection.packaging.Packet;
import module.connection.packaging.PacketManager;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

public class ChannelConnection implements IConnection {
    private static final long CONNECTION_TIMEOUT = 5000;

    public static final int STANDARD_PORT = 8787;

    public static final int PACKAGE_SIZE = Packet.PACKAGE_SIZE;

//    private String host;
    private InetAddress host;
    private int port;

    private final DatagramChannel datagramChannel;

    public ChannelConnection() throws UnknownHostException {
        this(STANDARD_PORT);
    }

    public ChannelConnection(int port) throws UnknownHostException {
        this("localhost", port);
    }

    public ChannelConnection(String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            bindChannel(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindChannel(SocketAddress local) throws IOException {
        datagramChannel.bind(local);
    }

    @Override
    public void send(Serializable obj) {
        try {
            Packet[] packets = PacketManager.split(obj);
            ByteBuffer byteBuffer;

            SocketAddress address = new InetSocketAddress(host, port);

            for(int i = 0; i < packets.length; i++) {
                byteBuffer = ByteBuffer.wrap(PacketManager.serialize(packets[i]));
                datagramChannel.send(byteBuffer, address);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    @Override
    public Serializable receive() {
        long timeoutChecker = System.currentTimeMillis() + CONNECTION_TIMEOUT;
        Serializable object;
        ByteBuffer emptyByteBuffer = ByteBuffer.allocate(PACKAGE_SIZE);
        try {
            Packet[] packets = new Packet[0];
            ByteBuffer byteBuffer = ByteBuffer.allocate(PACKAGE_SIZE);

            int counter = 0;
            int packagesAmount = 1;
            do {
                datagramChannel.receive(byteBuffer);
                if (byteBuffer.compareTo(emptyByteBuffer) == 0) {
                    continue;
                }
                byteBuffer = byteBuffer.flip();

                Packet packet = (Packet) PacketManager.deserialize(byteBuffer.array());
                if(counter == 0) {
                    packagesAmount = packet.getPackagesAmount();
                    packets = new Packet[packagesAmount];
                }
                packets[counter] = packet;
                counter++;
            } while (counter != packagesAmount && (timeoutChecker - System.currentTimeMillis()) >= 0);
                if (!Arrays.equals(packets, new Packet[0])) {
                    object = PacketManager.assemble(packets);
                } else {
                    throw new IOException("Server connection timeout!");
                }
        } catch (IOException io) {
            System.out.println(io.getMessage());
            return null;
        }
        return object;
    }
}
