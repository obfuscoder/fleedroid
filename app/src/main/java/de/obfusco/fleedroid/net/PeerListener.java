package de.obfusco.fleedroid.net;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerListener extends Thread implements Closeable {

    private final static String TAG = "PeerListener";

    private ServerSocket serverSocket;

    private ConnectionObserver observer;

    public PeerListener(int port, ConnectionObserver observer) throws IOException {
        this.observer = observer;
        serverSocket = new ServerSocket(port);
        Log.i(TAG, "Listening on port " + port);
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                observer.connectionEstablished(socket);
            } catch (IOException e) {
                Log.e(TAG, "Communication error", e);
            }
        }
    }

    @Override
    public void close() {
        Log.i(TAG, "Closing server socket");
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close server socket", e);
        }
    }
}
