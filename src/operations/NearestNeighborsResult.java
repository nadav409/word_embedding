package operations;

import model.Neighbor;

import java.util.List;

public class NearestNeighborsResult implements OperationResult {

    private String key;
    private int k;
    private List<Neighbor> neighbors;

    public NearestNeighborsResult(String key, int k, List<Neighbor> neighbors) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be empty");
        }

        if (neighbors == null) {
            throw new IllegalArgumentException("neighbors cannot be null");
        }

        this.key = key;
        this.k = k;
        this.neighbors = neighbors;
    }

    public String getKey() {
        return key;
    }

    public int getK() {
        return k;
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }
}