import model.Neighbor;

import java.util.List;

public class SubspaceGroupingResult implements OperationResult {

    private List<Neighbor> neighbors;

    public SubspaceGroupingResult(List<Neighbor> neighbors) {

        if (neighbors == null) {
            this.neighbors = List.of();
        } else {
            this.neighbors = neighbors;
        }
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }
}