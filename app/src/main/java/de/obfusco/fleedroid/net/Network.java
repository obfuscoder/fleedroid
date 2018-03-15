package de.obfusco.fleedroid.net;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.obfusco.fleedroid.net.disco.Discovery;
import de.obfusco.fleedroid.net.disco.DiscoveryObserver;
import de.obfusco.fleedroid.net.msg.Message;

public class Network implements Closeable,DiscoveryObserver,PeerObserver, ConnectionObserver {

    private static final String TAG = "Network";

    private Discovery discovery;
    private PeerListener server;
    private int port;
    private Map<String,Peer> peers = new HashMap<>();
    private List<String> localHostAddresses;
    private MessageBroker broker;
    private String name;
    private NetworkInterface networkInterface;

    public Network(int port, MessageBroker broker, String name) throws IOException {
        this.broker = broker;
        this.port = port;
        this.name = name;
        localHostAddresses = getLocalHostAddresses();
        discovery = new Discovery(port, this, name, networkInterface);
        server = new PeerListener(port, this);
    }
    public void start() {
        discovery.start();
        server.start();
    }

    @Override
    public void close() throws IOException {
        discovery.close();
        server.close();
        closePeers();
    }

    private void closePeers() {
        for(Iterator<Map.Entry<String,Peer>> it = peers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String,Peer> entry = it.next();
            entry.getValue().close();
            it.remove();
        }
    }

    public void peerDiscovered(InetAddress address) {
        Log.d(TAG, "Announcement received from " + address.getHostAddress() + "(" + address.getHostName() + ")");
        if (!isLocalAddress(address.getHostAddress()) && !isPeered(address.getHostAddress())) {
            Log.i(TAG, "Accepted announcement from " + address);
            try {
                createPeer(new Socket(address, port), address.getHostAddress());
            } catch (IOException e) {
                Log.e(TAG, "Could not create peer client to host " + address, e);
            }
        }
    }

    private boolean isPeered(String hostAddress) {
        return peers.containsKey(hostAddress);
    }

    public void send(String message) {
        Log.d(TAG, "Sending message: " + message);
        for(Peer peer : peers.values()) {
            peer.send(message);
        }
    }

    public boolean isLocalAddress(String hostAddress) {
        return localHostAddresses.contains(hostAddress);
    }

    private List<String> getLocalHostAddresses() throws SocketException {
        List<String> hostAddresses = new ArrayList<>();
        for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
            NetworkInterface networkInterface = interfaces.nextElement();
            for (Enumeration<InetAddress> addresses = networkInterface.getInetAddresses(); addresses.hasMoreElements(); ) {
                InetAddress address = addresses.nextElement();
                hostAddresses.add(address.getHostAddress());
            }
            if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                Log.i(TAG, "Using network interface " + networkInterface.getName());
                this.networkInterface = networkInterface;
            }
        }
        return hostAddresses;
    }

    @Override
    public void connected(Peer peer) {
        broker.connected(peer);
    }

    @Override
    public void disconnected(Peer peer) {
        Log.i(TAG, "Peer disconnected: " + peer.getAddress());
        removeAndClosePeer(peer);
    }

    @Override
    public void errorOccurred(Peer peer) {
        Log.e(TAG, "Error with peer " + peer.getAddress());
        removeAndClosePeer(peer);
    }

    private void removeAndClosePeer(Peer peer) {
        peers.remove(peer.getHostAddress());
        peer.close();
        broker.disconnected();
    }

    @Override
    public void messageReceived(Peer peer, String data) {
        Message.parseAndSignal(data, peer, broker);
    }

    @Override
    public void connectionEstablished(Socket socket) {
        String hostAddress = socket.getInetAddress().getHostAddress();
        Log.i(TAG,"Peering request from " + hostAddress);
        if (isPeered(hostAddress)) {
            try {
                Log.w(TAG, "Already peered with this host");
                socket.getOutputStream().write("ERROR! Already peered. Closing connection.\n".getBytes());
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not communicate/close (with) host " + hostAddress, e);
            }
        } else {
            try {
                createPeer(socket, hostAddress);
            } catch (IOException e) {
                Log.e(TAG, "Error while peering with " + hostAddress, e);
            }
        }
    }

    private void createPeer(Socket socket, String hostAddress) throws IOException {
        Peer peer = new Peer(socket, this, name);
        peer.start();
        peers.put(hostAddress, peer);
        connected(peer);
    }

    public int getNumberOfPeers() {
        return peers.size();
    }

    public Collection<Peer> getPeers() {
        return peers.values();
    }
}
