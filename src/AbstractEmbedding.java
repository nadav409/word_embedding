import java.util.Objects;

public abstract class AbstractEmbedding {

    private final String text;
    private final Vector vector;

    protected AbstractEmbedding(String text, Vector vector) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be null/blank");
        }
        this.text = text;
        if (vector == null) {
            throw new IllegalArgumentException("vector cannot be null");
        }
        this.vector = vector;
    }

    public String getText() {
        return text;
    }

    public Vector getVector() {
        return vector;
    }
}
