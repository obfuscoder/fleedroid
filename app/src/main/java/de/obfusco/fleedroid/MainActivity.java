package de.obfusco.fleedroid;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.obfusco.fleedroid.db.AppDatabase;
import de.obfusco.fleedroid.db.TransactionDao;
import de.obfusco.fleedroid.domain.Data;
import de.obfusco.fleedroid.domain.Item;
import de.obfusco.fleedroid.domain.Transaction;
import de.obfusco.fleedroid.net.MessageBroker;
import de.obfusco.fleedroid.net.Network;
import de.obfusco.fleedroid.net.Peer;
import de.obfusco.fleedroid.net.msg.DataMessage;
import de.obfusco.fleedroid.net.msg.HelpMessage;
import de.obfusco.fleedroid.net.msg.Message;
import de.obfusco.fleedroid.net.msg.TransactionMessage;
import de.obfusco.fleedroid.service.StorageConverter;

public class MainActivity extends AppCompatActivity implements MessageBroker {
    private static final String TAG = "MainActivity";
    private Network network;
    AppDatabase database;
    List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initNetwork();
        initDatabase();

        addTestData();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView itemListView = findViewById(R.id.itemListView);
        final ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, items);
        itemListView.setAdapter(adapter);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView codeText = findViewById(R.id.codeText);
                Item item = database.itemDao().get(String.valueOf(codeText.getText()));
                if (item == null) {
                    showMessageBox("Unbekannter Artikelcode");
                } else {
                    if (item.sold != null) {
                        showMessageBox("Artikel wurde bereits verkauft");
                    } else {
                        boolean isAlreadyInList = false;
                        for(Item enteredItem : items) {
                            if (enteredItem.code.equals(item.code)) {
                                isAlreadyInList = true;
                                break;
                            }
                        }
                        if (isAlreadyInList) {
                            showMessageBox("Artikel wurde bereits erfasst");
                        } else {
                            items.add(item);
                            adapter.notifyDataSetChanged();
                            itemListView.smoothScrollToPosition(items.size());
                            updateCountAndSum();
                        }
                    }
                }
                codeText.setText("");
            }
        });

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Transaction transaction = Transaction.create(Transaction.Type.PURCHASE, items);
                database.store(transaction);
                network.send(StorageConverter.convert(transaction));
                items.clear();
                adapter.notifyDataSetChanged();
                updateCountAndSum();
            }
        });
    }

    private void addTestData() {
        Data data = new Data();
        data.addItem("123", 2, 10, "Kleid", "Jeanskleid blau", "98/105", 5.5, null);
        data.addItem("234", 10, 13, "Hose", "Kurt grün", "105", 13.2, null);
        data.addItem("345", 11, 23, "Spielzeug", "Kurt grün", "105", 55.5, null);
        data.addItem("456", 12, 12, "Jacke", "Kurt grün", "105", 10.9, null);
        data.addItem("567", 13, 133, "Schuhe", "Kurt grün", "105", 8.4, null);
        data.addItem("678", 14, 53, "Roller", "Kurt grün", "105", 5.8, null);
        data.addItem("789", 15, 67, "Fahrrad", "Kurt grün", "105", 3.2, null);
        data.addItem("888", 3, 1, "Hose", "beige", null, 2.1, Calendar.getInstance().getTime());
        database.store(data);
    }

    private void updateCountAndSum() {
        TextView countView = findViewById(R.id.countView);
        countView.setText(String.valueOf(items.size()));

        double sum = 0;
        for (Item item : items) {
            sum += item.price;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        TextView sumView = findViewById(R.id.sumView);
        sumView.setText(currency.format(sum));
    }

    private void showMessageBox(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.create().show();
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
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "flohmarkthelfer").fallbackToDestructiveMigration().allowMainThreadQueries().build();
    }

    @Override
    public void messageReceived(Peer peer, TransactionMessage message) {
        database.store(message.getTransaction());
        Log.i(TAG, "Transactions: " + database.transactionDao().count());
    }

    @Override
    public void messageReceived(Peer peer, DataMessage message) {
        Log.i(TAG, "DATA: " + message.getData().name);
        new StorageConverter(database).store(message.getData());
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
