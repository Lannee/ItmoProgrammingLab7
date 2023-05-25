package module.connection;

import java.io.Serializable;
import java.net.InetAddress;

public interface IConnection {
    void send(InetAddress host, int port, Serializable obj);
    byte[] receive();
    Serializable packetConsumer();
    InetAddress getRecipientHost();
    int getRecipientPort();
}
