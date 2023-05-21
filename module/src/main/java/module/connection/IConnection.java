package module.connection;

import java.io.Serializable;

public interface IConnection {
    void send(Serializable obj);
    Serializable receive();
}
