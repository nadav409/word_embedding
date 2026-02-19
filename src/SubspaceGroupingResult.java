import java.util.List;

public class SubspaceGroupingResult extends OperationResult {

    private final List<Neighbor> neighbors;

    public SubspaceGroupingResult(List<Neighbor> neighbors) {
        super(OperationType.GROUPING);
        this.neighbors = (neighbors == null)
                ? List.of()
                : List.copyOf(neighbors);
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

    @Override
    public String toString() {
        return "SubspaceGroupingResult{" +
                "neighbors=" + neighbors +
                '}';
    }
}
