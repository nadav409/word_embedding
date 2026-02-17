import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class ResearchEnvironment implements Provider {

    private final Map<SpaceId, EmbeddingSpace> spaces;
    private DistanceStrategy distanceStrategy;

    public ResearchEnvironment(Map<SpaceId, EmbeddingSpace> spaces, DistanceStrategy initialStrategy) {

        if (spaces == null) {
            throw new IllegalArgumentException("spaces cannot be null");
        }
        if (spaces.isEmpty()) {
            throw new IllegalArgumentException("spaces cannot be empty");
        }
        if (initialStrategy == null) {
            throw new IllegalArgumentException("initialStrategy cannot be null");
        }

        this.spaces = new EnumMap<>(SpaceId.class);
        this.spaces.putAll(spaces);

        if (!this.spaces.containsKey(SpaceId.FULL)) {
            throw new IllegalArgumentException("FULL space is required");
        }

        this.distanceStrategy = initialStrategy;
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
        return id != null && spaces.containsKey(id);
    }

    public Collection<SpaceId> availableSpaces() {
        return spaces.keySet();
    }
}
