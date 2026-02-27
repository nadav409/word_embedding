public abstract class ResearchOperation {

    protected final Provider provider;
    protected final SpaceId spaceId;

    protected ResearchOperation(Provider provider, SpaceId spaceId) {
        if (provider == null || spaceId == null) {
            throw new IllegalArgumentException("provider and spaceId must not be null");
        }
        this.provider = provider;
        this.spaceId = spaceId;
    }

    public final OperationResult execute() {
        EmbeddingSpace space = provider.getSpace(spaceId);
        return run(space);
    }

    protected abstract OperationResult run(EmbeddingSpace space);

    protected final DistanceStrategy metric() {
        return provider.getDistanceStrategy();
    }
}