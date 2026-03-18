package operations;

import app.Provider;
import app.SpaceId;
import model.EmbeddingSpace;

public abstract class ResearchOperation {

    protected Provider provider;
    protected SpaceId spaceId;

    public ResearchOperation(Provider provider, SpaceId spaceId) {

        if (provider == null || spaceId == null) {
            throw new IllegalArgumentException("provider and spaceId cannot be null");
        }

        this.provider = provider;
        this.spaceId = spaceId;
    }

    public OperationResult execute() {

        EmbeddingSpace space = provider.getSpace(spaceId);

        return run(space);
    }

    protected abstract OperationResult run(EmbeddingSpace space);

    protected DistanceStrategy metric() {
        return provider.getDistanceStrategy();
    }
}