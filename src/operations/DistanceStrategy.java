package operations;

import model.Vector;

public interface DistanceStrategy {
    /**
     * Computes distance (or dissimilarity) between two vectors.
     * Smaller value = closer (for both Euclidean and cosine distance in our design).
     */
    double compute(Vector a, Vector b);
}
