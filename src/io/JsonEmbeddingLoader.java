package io;

import com.google.gson.*;
import model.RawEmbedding;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonEmbeddingLoader extends FileEmbeddingLoader {

    public JsonEmbeddingLoader(Path path) {
        super(path);
    }

    @Override
    public List<RawEmbedding> load() throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonArray array = readJsonArray(reader);
            return parseEmbeddings(array);
        }
    }

    private JsonArray readJsonArray(Reader reader) {
        JsonElement root = JsonParser.parseReader(reader);
        return root.getAsJsonArray();
    }

    private List<RawEmbedding> parseEmbeddings(JsonArray array) {
        List<RawEmbedding> result = new ArrayList<>();

        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            RawEmbedding embedding = parseSingleEmbedding(obj);
            result.add(embedding);
        }

        return result;
    }

    private RawEmbedding parseSingleEmbedding(JsonObject obj) {
        String word = readWord(obj);
        double[] vector = readVector(obj);
        return new RawEmbedding(word, vector);
    }

    private String readWord(JsonObject obj) {
        return obj.get("word").getAsString();
    }

    private double[] readVector(JsonObject obj) {
        JsonArray vecArray = obj.getAsJsonArray("vector");
        double[] values = new double[vecArray.size()];

        for (int i = 0; i < vecArray.size(); i++) {
            values[i] = vecArray.get(i).getAsDouble();
        }

        return values;
    }
}