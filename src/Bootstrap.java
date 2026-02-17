import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Bootstrap {

    public static Provider buildProvider() throws Exception {

        // תעדכן את השמות אם אצלך נקראים אחרת
        List<RawEmbedding> rawFull = new JsonEmbeddingLoader(Path.of("full_vectors.json")).load();
        List<RawEmbedding> rawPca  = new JsonEmbeddingLoader(Path.of("pca_vectors.json")).load();

        EmbeddingSpace fullSpace = new EmbeddingSpace(toWordEmbeddings(rawFull));
        EmbeddingSpace pcaSpace  = new EmbeddingSpace(toWordEmbeddings(rawPca));

        Map<SpaceId, EmbeddingSpace> spaces = new EnumMap<>(SpaceId.class);
        spaces.put(SpaceId.FULL, fullSpace);
        spaces.put(SpaceId.PCA,  pcaSpace);

        // שים לב: אתה כבר השתמשת גם ב-EuclideanDistance לפעמים.
        // לתוצאות שכנים: זה לא משנה לציור, אבל נשאיר CosineDistance.
        return new ResearchEnvironment(spaces, new CosineDistance());
    }

    private static List<Embedding> toWordEmbeddings(List<RawEmbedding> raw) {
        List<Embedding> out = new ArrayList<>(raw.size());
        for (RawEmbedding r : raw) {
            out.add(new WordEmbedding(r.getKey(), new Vector(r.getValuesCopy())));
        }
        return out;
    }
}

