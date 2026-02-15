import java.util.Arrays;

public class Vector {
    private final double[] values;


    public Vector(double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("dimension must be > 0");
        }
        this.values = Arrays.copyOf(values, values.length);
    }

    public int dimension() {
        return values.length;
    }

    public double get(int i) {
        if (i < 0 || i >= values.length) {
            throw new IndexOutOfBoundsException("index out of bounds: " + i);
        }
        return values[i];
    }

    public double dot(Vector other) {
        requireSameDim(other);
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i] * other.values[i];
        }
        return sum;
    }

    public double norm() {
        return Math.sqrt(this.dot(this));
    }

    public Vector add(Vector other) {
        requireSameDim(other);
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = this.values[i] + other.values[i];
        }
        return new Vector(out);
    }

    public Vector sub(Vector other) {
        requireSameDim(other);
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = this.values[i] - other.values[i];
        }
        return new Vector(out);
    }

    public Vector scale(double alpha) {
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = alpha * this.values[i];
        }
        return new Vector(out);
    }

    private void requireSameDim(Vector other) {
        if (other == null) {
            throw new IllegalArgumentException("other vector is null");
        }
        if (this.values.length != other.values.length) {
            throw new IllegalArgumentException("dimension mismatch");
        }
    }
}
