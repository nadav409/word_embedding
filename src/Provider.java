import model.EmbeddingSpace;
import operations.DistanceStrategy;

public interface Provider {

    /**
     * Returns the embedding space associated with the given id
     * (for example FULL or PCA).
     */
    EmbeddingSpace getSpace(SpaceId id);

    /**
     * Returns the currently active distance metric.
     */
    DistanceStrategy getDistanceStrategy();

    /**
     * Changes the active distance metric.
     */
    void setDistanceStrategy(DistanceStrategy strategy);
}