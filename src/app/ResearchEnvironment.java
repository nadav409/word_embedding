package app;

import model.EmbeddingSpace;
import model.SpaceId;
import operations.DistanceStrategy;

import java.util.HashMap;
import java.util.Map;

public class ResearchEnvironment implements Provider {

    private Map<SpaceId, EmbeddingSpace> spaces;
    private DistanceStrategy distanceStrategy;

    public ResearchEnvironment(Map<SpaceId, EmbeddingSpace> spaces, DistanceStrategy strategy) {

        if (spaces == null || spaces.isEmpty()) {
            throw new IllegalArgumentException("spaces cannot be null or empty");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("distance strategy cannot be null");
        }

        this.spaces = new HashMap<>(spaces);

        if (!this.spaces.containsKey(SpaceId.FULL)) {
            throw new IllegalArgumentException("FULL space must exist");
        }

        this.distanceStrategy = strategy;
    }

    public EmbeddingSpace getSpace(SpaceId id) {

        if (id == null) {
            throw new IllegalArgumentException("space id cannot be null");
        }

        EmbeddingSpace space = spaces.get(id);

        if (space == null) {
            throw new IllegalArgumentException("space not found: " + id);
        }

        return space;
    }

    public DistanceStrategy getDistanceStrategy() {
        return distanceStrategy;
    }

    public void setDistanceStrategy(DistanceStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }

        distanceStrategy = strategy;
    }

}