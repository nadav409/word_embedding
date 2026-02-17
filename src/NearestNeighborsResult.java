import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NearestNeighborsResult extends OperationResult {

    private final String Key;
    private final int k;
    private final List<Neighbor> neighbors;

    public NearestNeighborsResult(String Key, int k, List<Neighbor> neighbors) {
        super(OperationType.NEIGHBORS);

        if (Key == null || Key.isBlank()) {
            throw new IllegalArgumentException("queryKey cannot be null/blank");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (neighbors == null) {
            throw new IllegalArgumentException("neighbors cannot be null");
        }

        this.Key = Key;
        this.k = k;
        this.neighbors = Collections.unmodifiableList(new ArrayList<>(neighbors));
    }

    public String getKey() {
        return Key;
    }

    public int getK() {
        return k;
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nearest neighbors for '").append(Key).append("' (top ").append(k).append("):\n");

        int i = 1;
        for (Neighbor n : neighbors) {
            sb.append(i++).append(") ")
                    .append(n.getKey())
                    .append("  distance=")
                    .append(n.getDistance())
                    .append("\n");
        }
        return sb.toString();
    }
}
