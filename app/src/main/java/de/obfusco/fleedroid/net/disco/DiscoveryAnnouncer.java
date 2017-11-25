package de.obfusco.fleedroid.net.disco;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Random;

class DiscoveryAnnouncer extends Thread implements Closeable {

    private final static String TAG = "DiscoveryAnnouncer";

    private volatile MulticastSocket socket;
    private InetAddress multicastAddress;
    private String name;

    public DiscoveryAnnouncer(MulticastSocket socket, InetAddress multicastAddress, String name) throws SocketException {
        this.socket = socket;
        this.multicastAddress = multicastAddress;
        this.name = name;
    }

    @Override
    public void run() {
        Log.i(TAG, "Starting announcements");
        Random random = new Random();
        int counter = 0;
        while (true) {
            if (socket == null) {
                Log.i(TAG, "Terminating announcement. Sent " + counter + " packets");
                return;
            }
            try {
                sendAnnouncement();
                counter++;
            } catch (IOException ex) {
                Log.e(TAG, "Failed to send announcement", ex);
            }
            try {
                sleep(random.nextInt(10000)+8000);
            } catch (InterruptedException ex) {
                Log.w(TAG, "Interrupted", ex);
                return;
            }
        }
    }

    private void sendAnnouncement() throws IOException {
        byte[] buffer = (String.format("HELLO %d;%s", new Date().getTime(), name)).getBytes();
        DatagramPacket datagramPacket;
        datagramPacket = new DatagramPacket(buffer, buffer.length, multicastAddress, socket.getLocalPort());
        Log.d(TAG, "Sending announcement on interface " + socket.getInterface());
        socket.send(datagramPacket);
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
