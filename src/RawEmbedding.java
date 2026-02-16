public class RawEmbedding {

    private final String key;
    private final double[] values;

    public RawEmbedding(String key, double[] values) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("Key cannot be null or blank");
        if (values == null || values.length == 0)
            throw new IllegalArgumentException("Vector cannot be empty");

        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public double[] getValuesCopy() {
        return values.clone();
    }

    public int dimension() {
        return values.length;
    }
}
