package de.obfusco.fleedroid;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.obfusco.fleedroid.db.AppDatabase;
import de.obfusco.fleedroid.domain.BaseItem;
import de.obfusco.fleedroid.domain.Data;
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
    List<BaseItem> items = new ArrayList<>();

    private static int SCAN_CODE = 1;
    private Menu menu;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                TextView codeText = findViewById(R.id.codeText);
                codeText.setText(data.getDataString());
                addItem(null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        initNetwork();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_item:
                deleteSelectedItems();
                break;
            case R.id.connection_count:
                showStats();
                break;
        }
        return true;
    }

    private void showStats() {
        StringBuilder sb = new StringBuilder();
        for(Peer peer : network.getPeers()) {
            sb.append(String.format("%s - %s, dT=%.2f s\n",
                    peer.getPeerName(), peer.getHostName(), ((double)peer.getTimeDiff())/1000.0));
        }
        String connections = "\nConnections:\n" + sb.toString();
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        double sales = database.itemDao().soldSum() + database.stockItemDao().soldSum();
        String stats = String.format("Artikel: %d\nStammartikel: %d\nTransaktionen: %d\nverkaufte Artikel: %d\nVerkaufte Stammartikel: %d\nUmsatz: %s\n",
                database.itemDao().count(),
                database.stockItemDao().count(),
                database.transactionDao().count(),
                database.itemDao().soldCount(),
                database.stockItemDao().soldCount(),
                currency.format(sales));
        if(network.getNumberOfPeers() != 0) stats += connections;
        showMessageBox(stats);
    }

    private void deleteSelectedItems() {
        MenuItem deleteItem = menu.findItem(R.id.delete_item);
        deleteItem.setEnabled(false);
        deleteItem.setVisible(false);

        ListView itemListView = findViewById(R.id.itemListView);
        for (int i=items.size()-1; i>=0; i--) {
            if (itemListView.isItemChecked(i)) items.remove(i);
        }
        itemListView.clearChoices();
        itemListChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        initDatabase();

        //addTestData();

        ListView itemListView = findViewById(R.id.itemListView);
        final ArrayAdapter<BaseItem> adapter = new ArrayAdapter<>(this, R.layout.item_list_row, items);
        itemListView.setAdapter(adapter);
        itemListView.setOnItemClickListener((parent, view, position, id) -> {
            MenuItem deleteItem = menu.findItem(R.id.delete_item);
            deleteItem.setEnabled(true);
            deleteItem.setVisible(true);
        });

        EditText codeEdit = findViewById(R.id.codeText);
        codeEdit.setOnEditorActionListener((v, actionId, event) -> { addItem(v); return true; });
    }

    public void checkout(View v) {
        if (items.isEmpty()) return;
        Transaction transaction = Transaction.create(Transaction.Type.PURCHASE, items);
        database.store(transaction);
        send(StorageConverter.convert(transaction));
        items.clear();
        itemListChanged();
        updateCountAndSum();
    }

    private void itemListChanged() {
        ListView itemListView = findViewById(R.id.itemListView);
        ((ArrayAdapter<BaseItem>) itemListView.getAdapter()).notifyDataSetChanged();
    }

    public void startScan(View v) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivityForResult(intent, SCAN_CODE);
    }

    public void addItem(View v) {
        TextView codeText = findViewById(R.id.codeText);
        if (codeText.getText().length() == 0) return;
        BaseItem item = database.itemDao().get(String.valueOf(codeText.getText()));
        if (item == null) {
            item = database.stockItemDao().get(String.valueOf(codeText.getText()));
        }
        if (item == null) {
            showMessageBox("Unbekannter Artikelcode");
        } else {
            if (!item.isSellable()) {
                showMessageBox("Artikel wurde bereits verkauft");
            } else {
                if (verifyUniqueness(item)) {
                    items.add(item);
                    ListView itemListView = findViewById(R.id.itemListView);
                    itemListView.smoothScrollToPosition(items.size());
                    itemListChanged();
                    updateCountAndSum();
                } else {
                    showMessageBox("Artikel wurde bereits erfasst");
                }
            }
        }
        codeText.setText("");
    }

    private boolean verifyUniqueness(BaseItem item) {
        if (!item.isUnique()) return true;

        for (BaseItem enteredItem : items) {
            if (enteredItem.code.equals(item.code)) {
                return false;
            }
        }
        return true;
    }

    private void send(final String message) {
        new Thread(() -> network.send(message)).start();
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
        data.addStockItem("11", "Tüte", 1, 0);
        data.addStockItem("22", "Kaffee", 1, 3);
        database.store(data);
    }

    private void updateCountAndSum() {
        TextView countView = findViewById(R.id.countView);
        countView.setText(String.valueOf(items.size()));

        double sum = 0;
        for (BaseItem item : items) {
            sum += item.price;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        TextView sumView = findViewById(R.id.sumView);
        sumView.setText(currency.format(sum));
    }

    private void showMessageBox(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    private void initNetwork() {
        checkOrRequestNetworkAccess();
        try {
            network = new Network(31454, this, "fleedroid");
            network.start();
        } catch (IOException e) {
            Log.e(TAG, "Could not init network!", e);
        }
    }

    private void checkOrRequestNetworkAccess() {
        String[] requiredPermissions = new String[] {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
        };
        if (!permissionsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 1);
        }
    }

    private boolean permissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
        try {
            new StorageConverter(database).store(message.getData());
            runOnUiThread(() ->
                showMessageBox(String.format("Daten des Termins %s erfolgreich übernommen.", message.getData().name))
            );
        } catch (Exception e) {
            Log.e(TAG, "Could not receive and store data!", e);
            runOnUiThread(() ->
                showMessageBox(String.format("Fehler beim Empfang und Speichern der Daten des Termins %s", message.getData().name))
            );
        }
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
        new Thread(() -> {
            for (Transaction transaction : database.transactionDao().findAll()) {
                Log.i(TAG, "Syncing transaction " + transaction.id + " with peer " + peer.getHostName());
                peer.send(StorageConverter.convert(transaction));
            }
        }).start();
    }

    @Override
    public void disconnected() {
        updateNetworkStatus();
        Log.i(TAG,"DISCONNECTED");
    }

    private void updateNetworkStatus() {
        Log.i(TAG, "Verbunden mit " + network.getNumberOfPeers() + "System(en)");
        runOnUiThread(() -> {
            if (menu == null) return;
            MenuItem connectionCountItem = menu.findItem(R.id.connection_count);
            connectionCountItem.setTitle(String.format("%d Verbindung%s",
                    network.getNumberOfPeers(), network.getNumberOfPeers() == 1 ? "" : "en"));
        });
    }
}
