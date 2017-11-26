package de.obfusco.fleedroid;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.obfusco.fleedroid.db.AppDatabase;
import de.obfusco.fleedroid.db.TransactionDao;
import de.obfusco.fleedroid.domain.Transaction;
import de.obfusco.fleedroid.net.msg.DataMessage;
import de.obfusco.fleedroid.net.msg.TransactionMessage;
import de.obfusco.fleedroid.net.msg.dto.Category;
import de.obfusco.fleedroid.net.msg.dto.Data;
import de.obfusco.fleedroid.net.msg.dto.Item;
import de.obfusco.fleedroid.net.msg.dto.Reservation;
import de.obfusco.fleedroid.net.msg.dto.StockItem;
import de.obfusco.fleedroid.service.StorageConverter;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TRANSACTION_ID = "3ffc45d1-793b-484f-8c61-a4484850aeff";
    private AppDatabase database;
    private Context appContext;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(appContext, AppDatabase.class).build();
    }

    @Test
    public void storesTransaction() throws IOException {
        TransactionDao transactions = database.transactionDao();
        assertEquals(0, transactions.count());
        String message = readResource("transaction.txt");
        database.store(TransactionMessage.parse(message).getTransaction());
        assertEquals(1, transactions.count());
        Transaction transaction = transactions.get(TRANSACTION_ID);
        assertEquals(TRANSACTION_ID, transaction.id);
        assertEquals(Transaction.Type.PURCHASE, transaction.type);
        assertEquals(1506149926715L, transaction.date.getTime());
        assertEquals(14, transaction.itemCodes.size());
    }

    @Test
    public void ignoresKnownTransaction() throws IOException {
        TransactionDao transactions = database.transactionDao();
        String message = readResource("transaction.txt");
        database.store(TransactionMessage.parse(message).getTransaction());
        assertEquals(1, transactions.count());
        database.store(TransactionMessage.parse(message).getTransaction());
        assertEquals(1, transactions.count());
    }

    @Test
    public void storesData() throws IOException {
        String message = readResource("data.txt");
        DataMessage dataMessage = DataMessage.parse(message);
        Data data = dataMessage.getData();
        assertEquals(1, data.id);
        new StorageConverter(database).store(data);
        assertEquals(0, database.transactionDao().count());
        assertEquals(10814, database.itemDao().count());
        assertEquals(3, database.stockItemDao().count());
    }

    private String readResource(String fileName) throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        return reader.readLine();
    }
}
