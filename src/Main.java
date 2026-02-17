import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // שנה לנתיב של הקובץ שהמרצה נתן
        Path path = Path.of("full_vectors.json");


        // ===== שלב 1: טעינת JSON =====
        EmbeddingLoader loader = new JsonEmbeddingLoader(path);
        List<RawEmbedding> rawEmbeddings = loader.load();

        System.out.println("Loaded raw embeddings: " + rawEmbeddings.size());

        // ===== שלב 2: המרה ל-Embedding =====
        List<Embedding> embeddings = new ArrayList<>(rawEmbeddings.size());

        for (RawEmbedding raw : rawEmbeddings) {
            String key = raw.getKey();
            double[] values = raw.getValuesCopy();

            Vector v = new Vector(values);
            embeddings.add(new WordEmbedding(key, v));
        }

        // ===== שלב 3: בניית EmbeddingSpace =====
        EmbeddingSpace space = new EmbeddingSpace(embeddings);

        System.out.println("Space size: " + space.size());
        System.out.println("Vector dimension: " + space.dimension());

        // ===== שלב 4: Provider =====
        Provider provider = new SimpleProvider(space, new CosineDistance());

        // ===== שלב 5: בדיקה =====
        // ניקח מילה שבטוח קיימת (הראשונה בקובץ)
        String keyToTest = embeddings.get(0).getKey();

        System.out.println("\nTesting word: " + keyToTest);

        OperationResult result =
                new NearestNeighborsOperation(provider, SpaceId.FULL, keyToTest, 10).execute();

        System.out.println(result);
    }
}
