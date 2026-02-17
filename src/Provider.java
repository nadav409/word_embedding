public interface Provider {

    EmbeddingSpace getSpace(SpaceId id);

    DistanceStrategy getDistanceStrategy();

    void setDistanceStrategy(DistanceStrategy strategy);
}
