import java.util.Arrays;

public class RawEmbedding {
    private final String text;
    private final double[] values;

    public RawEmbedding(String text, double[] values) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be null/blank");
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values cannot be null/empty");
        }
        this.text = text;
        this.values = Arrays.copyOf(values, values.length); // defensive copy
    }

    public String getText() {
        return text;
    }

    public double[] getValuesCopy() {
        return Arrays.copyOf(values, values.length);
    }

    public int dimension() {
        return values.length;
    }
}
