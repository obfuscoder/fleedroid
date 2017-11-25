package de.obfusco.fleedroid.net.msg;

import android.util.Base64;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class JsonDataConverter {
    public Data parseBase64Compressed(String string) throws IOException {
        byte[] compressedData = Base64.decode(string.getBytes(), Base64.DEFAULT);
        return parseCompressedStream(new ByteArrayInputStream(compressedData));
    }

    public Data parse(InputStream inputStream) {
        Gson gson = createGson();
        return gson.fromJson(new InputStreamReader(inputStream), Data.class);
    }

    private Gson createGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
    }

    public Data parseCompressedStream(InputStream compressedStream) throws IOException {
        GZIPInputStream uncompressedStream = new GZIPInputStream(compressedStream);
        return parse(uncompressedStream);
    }
}
