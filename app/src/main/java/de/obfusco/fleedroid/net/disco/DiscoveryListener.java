package de.obfusco.fleedroid.net.disco;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

class DiscoveryListener extends Thread implements Closeable {

    private final static String TAG = "DiscoveryListener";

    private volatile MulticastSocket socket;
    private DiscoveryObserver observer;

    public DiscoveryListener(MulticastSocket socket, DiscoveryObserver observer) throws SocketException {
        this.socket = socket;
        this.observer = observer;
    }

    @Override
    public void run() {
        int counter = 0;
        while (true) {
            if (socket == null) {
                Log.i(TAG, "Terminating listener. Received " + counter + " packets");
                return;
            }
            try {
                byte[] buffer = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);
                counter++;
                observer.peerDiscovered(datagramPacket.getAddress());
            } catch (IOException ex) {
                Log.e(TAG,"Receive failed", ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
