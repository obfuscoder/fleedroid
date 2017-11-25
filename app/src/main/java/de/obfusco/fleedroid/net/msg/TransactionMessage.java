package de.obfusco.fleedroid.net.msg;

import java.util.Arrays;
import java.util.Date;

import de.obfusco.fleedroid.domain.Transaction;

public class TransactionMessage extends Message {
    private Transaction transaction;

    @Override
    public String toString() {
        return "Transaktion: " + getTransaction();
    }

    private TransactionMessage(Transaction transaction) {
        this.transaction = transaction;
    }

    public static TransactionMessage parse(String data) {
        String[] parts = data.split(";");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Message does not contain 5 segments separated by ';'");
        }
        String id = parts[0];
        Transaction.Type type = Transaction.Type.valueOf(parts[1]);
        Date date = new Date(Long.parseLong(parts[2]));
        String zipCode = parts[3];
        String[] itemCodes = parts[4].split(",");
        return new TransactionMessage(new Transaction(id, type, date, Arrays.asList(itemCodes), zipCode));
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
