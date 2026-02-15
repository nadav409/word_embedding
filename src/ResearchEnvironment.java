public class ResearchEnvironment implements EmbeddingProvider {

    private final EmbeddingSpace fullSpace;
    private final EmbeddingSpace pcaSpace;
    private DistanceStrategy metric;

    public ResearchEnvironment(EmbeddingSpace fullSpace, EmbeddingSpace pcaSpace, DistanceStrategy metric) {
        if (fullSpace == null || pcaSpace == null || metric == null) {
            throw new IllegalArgumentException("environment arguments cannot be null");
        }
        this.fullSpace = fullSpace;
        this.pcaSpace = pcaSpace;
        this.metric = metric;
    }

    @Override
    public EmbeddingSpace getFullSpace() {
        return fullSpace;
    }

    @Override
    public EmbeddingSpace getPcaSpace() {
        return pcaSpace;
    }

    @Override
    public DistanceStrategy getMetric() {
        return metric;
    }

    public void setMetric(DistanceStrategy metric) {
        if (metric == null) {
            throw new IllegalArgumentException("metric cannot be null");
        }
        this.metric = metric;
    }
}

