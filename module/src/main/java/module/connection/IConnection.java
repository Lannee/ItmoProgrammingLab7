package module.connection;

import java.io.Serializable;

public interface IConnection {
    void send(Serializable obj);
    byte[] receive();
    Serializable handlingRequest(byte[] byteArray);
}
