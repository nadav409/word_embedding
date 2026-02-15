public interface EmbeddingProvider {
    EmbeddingSpace getFullSpace();

    EmbeddingSpace getPcaSpace();

    DistanceStrategy getMetric();
}
