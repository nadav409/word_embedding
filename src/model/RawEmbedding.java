package model;

public class RawEmbedding {

    private final String key;
    private final double[] values;

    public RawEmbedding(String key, double[] values) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("model.Vector cannot be null or empty");
        }

        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns a copy of the vector values.
     * The internal array is never exposed to keep the object immutable.
     */
    public double[] getValuesCopy() {
        return values.clone();
    }

    public int dimension() {
        return values.length;
    }
}