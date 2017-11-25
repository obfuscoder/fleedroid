package de.obfusco.fleedroid.net;

import java.net.Socket;

public interface ConnectionObserver {
    void connectionEstablished(Socket socket);
}
