import java.util.*;

public class EmbeddingSpace {

    private final Map<String, AbstractEmbedding> items;
    private final int dimension;


    public EmbeddingSpace(Collection<? extends AbstractEmbedding> embeddings) {
        if (embeddings == null) {
            throw new IllegalArgumentException("embeddings cannot be null");
        }
        if (embeddings.isEmpty()) {
            throw new IllegalArgumentException("embeddings cannot be empty");
        }

        Map<String, AbstractEmbedding> tmp = new HashMap<>();
        int dim = -1;

        for (AbstractEmbedding e : embeddings) {
            if (e == null) {
                throw new IllegalArgumentException("embedding cannot be null");
            }

            String key = e.getText();
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("embedding text cannot be null/blank");
            }

            Vector v = e.getVector();
            if (v == null) {
                throw new IllegalArgumentException("embedding vector cannot be null");
            }

            if (dim == -1) {
                dim = v.dimension();
            } else if (dim != v.dimension()) {
                throw new IllegalArgumentException(
                        "all embeddings must have same dimension: expected " + dim + " got " + v.dimension()
                );
            }

            if (tmp.containsKey(key)) {
                throw new IllegalArgumentException("duplicate embedding for text: " + key);
            }
            tmp.put(key, e);
        }

        this.dimension = dim;
        this.items = Map.copyOf(tmp); // הופך ל-Immutable Map (Java 10+)
    }

    public int dimension() {
        return dimension;
    }

    public int size() {
        return items.size();
    }

    public boolean contains(String text) {
        if (text == null) return false;
        return items.containsKey(text);
    }

    public AbstractEmbedding get(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be null/blank");
        }
        AbstractEmbedding e = items.get(text);
        if (e == null) {
            throw new UnknownWordException("unknown text: " + text);
        }
        return e;
    }

    public Collection<AbstractEmbedding> all() {
        return items.values(); // כבר לא ניתן לשינוי כי items הוא Map.copyOf(...)
    }
}
