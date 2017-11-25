package de.obfusco.fleedroid.net;

import de.obfusco.fleedroid.net.msg.DataMessage;
import de.obfusco.fleedroid.net.msg.HelpMessage;
import de.obfusco.fleedroid.net.msg.Message;
import de.obfusco.fleedroid.net.msg.TransactionMessage;

public interface MessageBroker {
    void messageReceived(Peer peer, TransactionMessage message);
    void messageReceived(Peer peer, DataMessage message);
    void messageReceived(Peer peer, HelpMessage message);
    void messageReceived(Peer peer, Message parse);
    void connected(Peer peer);
    void disconnected();

}
