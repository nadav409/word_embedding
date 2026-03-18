package model;

public abstract class Embedding {

    private final String key;
    private final Vector vector;

    protected Embedding(String key, Vector vector) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("key cannot be null or blank");

        if (vector == null)
            throw new IllegalArgumentException("vector cannot be null");

        this.key = key;
        this.vector = vector;
    }

    public String getKey() {
        return key;
    }

    public Vector getVector() {
        return vector;
    }

    public int dimension() {
        return vector.dimension();
    }
}

