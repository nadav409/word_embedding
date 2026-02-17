public class SimpleProvider implements Provider {

    private final EmbeddingSpace space;
    private final DistanceStrategy strategy;

    public SimpleProvider(EmbeddingSpace space, DistanceStrategy strategy) {
        if (space == null) {
            throw new IllegalArgumentException("space cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }
        this.space = space;
        this.strategy = strategy;
    }

    @Override
    public EmbeddingSpace getSpace(SpaceId id) {
        // לצורך בדיקה: לא משנה איזה SpaceId ביקשו
        // תמיד נחזיר את אותו מרחב שטענו מה-JSON
        return space;
    }

    @Override
    public DistanceStrategy getDistanceStrategy() {
        return strategy;
    }
}

