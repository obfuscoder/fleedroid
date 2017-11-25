package de.obfusco.fleedroid.net.msg;

import android.util.Log;

import java.io.IOException;

import de.obfusco.fleedroid.net.MessageBroker;
import de.obfusco.fleedroid.net.Peer;

public class Message {
    private static final String TAG = "Message";

    @Override
    public String toString() {
        return "???";
    }

    public static void parseAndSignal(String data, Peer peer, MessageBroker broker) {
        if(data.startsWith("HELP")) {
            broker.messageReceived(peer, HelpMessage.parse(data));
        } else if (data.startsWith("DATA")) {
            try {
                broker.messageReceived(peer, DataMessage.parse(data));
            } catch (IOException e) {
                Log.e(TAG, "Could not parse data message", e);
            }
        } else {
            broker.messageReceived(peer, TransactionMessage.parse(data));
        }
    }
}
