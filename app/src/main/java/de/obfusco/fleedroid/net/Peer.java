package de.obfusco.fleedroid.net;

import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Peer extends Thread implements Closeable {

    private final static String TAG = "Peer";

    private final PrintWriter sender;
    private final BufferedReader receiver;
    private Socket socket;
    private PeerObserver peerObserver;
    private Ping ping;
    private long timeDiff;
    private String localName;
    private String peerName;

    public Peer(Socket socket, PeerObserver peerObserver, String localName) throws IOException {
        this.socket = socket;
        this.peerObserver = peerObserver;
        this.localName = localName;
        sender = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        receiver = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Peer starting");
            ping = new Ping(this);
            ping.start();
            while (isConnected()) {
                try {
                    socket.setSoTimeout(30000);
                    String line = receiver.readLine();
                    if (line == null) {
                        Log.i(TAG, "Connection closed by peer.");
                        peerObserver.disconnected(this);
                        return;
                    }
                    Log.d(TAG, "Received from peer " + socket.getInetAddress().getHostAddress() + ": " + line);
                    if (line.startsWith("PING")) {
                        if (line.length() > 4) pingReceived(line.substring(5));
                    } else {
                        peerObserver.messageReceived(this, line);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error while communication with peer " + socket.getInetAddress().getHostAddress(), e);
                    peerObserver.errorOccurred(this);
                }
            }
        }
        catch(Exception ex) {
            Log.e(TAG, "Exception!", ex);
            peerObserver.errorOccurred(this);
        }
    }

    private void pingReceived(String message) {
        if (message.isEmpty()) return;
        String[] pingParts = message.split(";");
        long peerTime = Long.parseLong(pingParts[0]);
        timeDiff = new Date().getTime() - peerTime;
        peerName = pingParts[1];
        Log.d(TAG, "Peer " + getAddress() + " info update: tdiff=" + timeDiff + ", name=" + peerName);
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    @Override
    public void close() {
        Log.i(TAG, "Closing connection");
        ping.quit();
        sender.close();
        try {
            receiver.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close receiver", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close socket", e);
        }
    }

    public String getHostAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public String getHostName() {
        return socket.getInetAddress().getHostName();
    }

    public String getAddress() {
        return String.format("%s(%s)", getHostAddress(), getHostName());
    }

    public String getPeerName() {
        return peerName;
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public synchronized void send(String message) {
        if (isConnected()) {
            Log.d(TAG, "Sending to peer " + getAddress() + ": " + message);
            sender.println(message);
        } else {
            Log.w(TAG, "Not connected to " + getAddress() + " - not sending " + message);
        }
    }

    public String getLocalName() {
        return localName;
    }

    private class Ping extends Thread {
        private Peer peer;
        private volatile boolean running;

        public Ping(Peer peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            Log.i(TAG, "Ping running");
            running = true;
            while (running && peer.isConnected()) {
                try {
                    synchronized (this) {
                        wait(5000);
                    }
                } catch (InterruptedException ex) {
                    Log.e(TAG, "Caught exception while waiting for next ping", ex);
                    return;
                }
                if (!running) break;
                peer.send(String.format("PING %d;%s", new Date().getTime(), peer.getLocalName()));
            }
        }

        public void quit() {
            running = false;
        }
    }
}
