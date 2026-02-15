public class CosineDistance implements DistanceStrategy {
    @Override
    public double compute(Vector a, Vector b) {
        double normA = a.norm();
        double normB = b.norm();

        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException("cosine distance undefined for zero vector");
        }

        double cos = a.dot(b) / (normA * normB);
        return 1 - cos;
    }
}
