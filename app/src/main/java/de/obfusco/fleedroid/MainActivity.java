package de.obfusco.fleedroid;

import android.arch.persistence.room.Room;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import de.obfusco.fleedroid.db.AppDatabase;
import de.obfusco.fleedroid.db.TransactionDao;
import de.obfusco.fleedroid.domain.Transaction;
import de.obfusco.fleedroid.net.MessageBroker;
import de.obfusco.fleedroid.net.Network;
import de.obfusco.fleedroid.net.Peer;
import de.obfusco.fleedroid.net.msg.DataMessage;
import de.obfusco.fleedroid.net.msg.HelpMessage;
import de.obfusco.fleedroid.net.msg.Message;
import de.obfusco.fleedroid.net.msg.TransactionMessage;

public class MainActivity extends AppCompatActivity implements MessageBroker {
    private static final String TAG = "MainActivity";
    private Network network;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNetwork();
        initDatabase();
    }

    private void initNetwork() {
        try {
            network = new Network(31454, this, "fleedroid");
            network.start();
        } catch (IOException e) {
            Log.e(TAG, "Could not init network!", e);
        }
    }

    private void initDatabase() {
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "flohmarkthelfer").build();
    }

    @Override
    public void messageReceived(Peer peer, TransactionMessage message) {
        TransactionDao transactions = database.transactionDao();
        Transaction transaction = message.getTransaction();
        if (transactions.get(transaction.id) == null) {
            transactions.insert(message.getTransaction());
        }
        Log.i(TAG, "Transactions: " + transactions.count());
    }

    @Override
    public void messageReceived(Peer peer, DataMessage message) {
        Log.i(TAG, "DATA: " + message.getData());
        database.transactionDao().deleteAll();
    }

    @Override
    public void messageReceived(Peer peer, HelpMessage message) {
        Log.i(TAG, "HELP: " + message);
    }

    @Override
    public void messageReceived(Peer peer, Message parse) {
        Log.w(TAG, "UNKONW MESSAGE");
    }

    @Override
    public void connected(Peer peer) {
        updateNetworkStatus();
        Log.i(TAG,"CONNECTED");
    }

    @Override
    public void disconnected() {
        updateNetworkStatus();
        Log.i(TAG,"DISCONNECTED");
    }

    private void updateNetworkStatus() {
        Log.i(TAG, "Verbunden mit " + network.getNumberOfPeers() + "System(en)");
    }
}
