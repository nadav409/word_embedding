import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResearchEnvironment implements Provider {

    private final Map<SpaceId, EmbeddingSpace> spaces;
    private DistanceStrategy distanceStrategy;

    public ResearchEnvironment(Map<SpaceId, EmbeddingSpace> spaces, DistanceStrategy defaultStrategy) {

        if (spaces == null) {
            throw new IllegalArgumentException("spaces cannot be null");
        }

        if (spaces.isEmpty()) {
            throw new IllegalArgumentException("spaces cannot be empty");
        }

        if (defaultStrategy == null) {
            throw new IllegalArgumentException("defaultStrategy cannot be null");
        }

        this.spaces = new HashMap<>();

        for (Map.Entry<SpaceId, EmbeddingSpace> entry : spaces.entrySet()) {
            SpaceId id = entry.getKey();
            EmbeddingSpace space = entry.getValue();

            if (id == null) {
                throw new IllegalArgumentException("space id cannot be null");
            }

            if (space == null) {
                throw new IllegalArgumentException("embedding space cannot be null");
            }

            this.spaces.put(id, space);
        }

        // require FULL space
        if (!this.spaces.containsKey(SpaceId.FULL)) {
            throw new IllegalArgumentException("FULL space is required");
        }

        this.distanceStrategy = defaultStrategy;
    }

    @Override
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

    @Override
    public DistanceStrategy getDistanceStrategy() {
        return distanceStrategy;
    }

    @Override
    public void setDistanceStrategy(DistanceStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }

        this.distanceStrategy = strategy;
    }

    public boolean hasSpace(SpaceId id) {
        if (id == null) {
            return false;
        }
        return spaces.containsKey(id);
    }

    public Collection<SpaceId> availableSpaces() {
        return spaces.keySet();
    }
}