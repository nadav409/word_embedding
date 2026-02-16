import com.google.gson.*;
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

        List<RawEmbedding> result = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(path)) {

            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                String key = obj.get("word").getAsString();

                JsonArray vecArray = obj.getAsJsonArray("vector");
                double[] values = new double[vecArray.size()];

                for (int i = 0; i < vecArray.size(); i++) {
                    values[i] = vecArray.get(i).getAsDouble();
                }

                result.add(new RawEmbedding(key, values));
            }
        }

        return result;
    }
}

