package de.obfusco.fleedroid.net.msg;

import java.io.IOException;

import de.obfusco.fleedroid.net.msg.dto.Data;

public class DataMessage extends Message {
    private Data data;

    private DataMessage(Data data) {
        this.data = data;
    }

    public static DataMessage parse(String message) throws IOException {
        JsonDataConverter converter = new JsonDataConverter();
        Data data = converter.parseBase64Compressed(message.substring(4));
        return new DataMessage(data);
    }

    @Override
    public String toString() {
        return "Daten: " + getData();
    }

    public Data getData() {
        return data;
    }
}
