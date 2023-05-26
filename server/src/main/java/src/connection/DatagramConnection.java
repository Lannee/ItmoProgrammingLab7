package src.connection;

import module.connection.IConnection;
import module.connection.packaging.Packet;
import module.connection.packaging.PacketManager;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.concurrent.ForkJoinPool;

public class DatagramConnection implements IConnection {

    public static final int STANDARD_PORT = 8787;

    public static final int PACKAGE_SIZE = Packet.PACKAGE_SIZE;

    // private String host;
    private InetAddress host;

    private Integer port;

    private InetAddress clientHost = null;
    private Integer clientPort = null;

    private boolean isListeningPort;

    private final DatagramSocket socket;

    private static final Logger logger = LoggerFactory.getLogger(DatagramConnection.class);

    public DatagramConnection() throws SocketException, UnknownHostException {
        this(false);
    }

    public DatagramConnection(boolean isListeningPort) throws SocketException, UnknownHostException {
        this(STANDARD_PORT, isListeningPort);
    }

    public DatagramConnection(int port) throws SocketException, UnknownHostException {
        this(port, false);
    }

    public DatagramConnection(int port, boolean isListeningPort) throws SocketException, UnknownHostException {
        this("localhost", port, isListeningPort);
    }

    public DatagramConnection(String host, int port) throws SocketException, UnknownHostException {
        this(host, port, false);
    }

    public DatagramConnection(String host, int port, boolean isListeningPort)
            throws SocketException, UnknownHostException {
        try {
            this.host = InetAddress.getByName(host);
            this.port = port;
            this.isListeningPort = isListeningPort;

            if (isListeningPort) {
                socket = new DatagramSocket(port);
            } else
                socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new SocketException();
        } catch (UnknownHostException e) {
            throw new UnknownHostException();
        }
    }

    public InetAddress getClientHost() {
        return clientHost;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientHost(InetAddress clientHost) {
        this.clientHost = clientHost;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    @Override
    public void send(InetAddress host, int port, Serializable object) {
        try {
            ObjectDevider objectDevider = new ObjectDevider(PacketManager.getBytesFromObj(object));
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            Packet[] packets = forkJoinPool.invoke(objectDevider);
            
            for (Packet packet : packets) {
                DatagramPacket datagramPacket = new DatagramPacket(
                        PacketManager.serialize(packet),
                        PACKAGE_SIZE,
                        host,
                        port);
                socket.send(datagramPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized byte[] receive() {
        byte[] bytes = new byte[PACKAGE_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(bytes, PACKAGE_SIZE);
        try {
            socket.receive(datagramPacket);
            clientHost = datagramPacket.getAddress();
            clientPort = datagramPacket.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public Serializable packetConsumer() {
        Serializable object;
        Packet[] packets = new Packet[0];
        
        int counter = 0;
        int packagesAmount = 1;
        do {
            byte[] bytes = receive();
            logger.info("Client host: {}, client port: {}", clientHost, clientPort);

            Packet packet = (Packet) PacketManager.deserialize(bytes);

            if (counter == 0) {
                packagesAmount = packet.getPackagesAmount();
                packets = new Packet[packagesAmount];

                // Needed to parallel
                // if (isListeningPort && (clientHost == null || clientPort == null)) {
                // clientHost = packetHost;
                // clientPort = packetPort;
                // } else if (!clientHost.equals(packetHost) || !clientPort.equals(packetPort))
                // {
                // send(new CommandResponse("", ResponseStatus.CONNECTION_REJECTED));
                // return receive();
                // }
            }
            packets[counter] = packet;
        } while (++counter != packagesAmount);

        object = PacketManager.assemble(packets);
        return object;
    }

    @Override
    public InetAddress getRecipientHost() {
        return clientHost;
    }

    @Override
    public int getRecipientPort() {
        return clientPort;
    }
}
