package de.obfusco.fleedroid.net.disco;

import java.net.InetAddress;

public interface DiscoveryObserver {
    void peerDiscovered(InetAddress hostAddress);
}
