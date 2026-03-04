import java.util.List;

public class VectorArithmeticResult implements OperationResult {

    private String expressionPreview;
    private int k;
    private List<Neighbor> neighbors;

    public VectorArithmeticResult(String expressionPreview, int k, List<Neighbor> neighbors) {

        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        if (neighbors == null) {
            throw new IllegalArgumentException("neighbors cannot be null");
        }

        if (expressionPreview == null) {
            this.expressionPreview = "";
        } else {
            this.expressionPreview = expressionPreview;
        }

        this.k = k;
        this.neighbors = neighbors;
    }

    public String getExpressionPreview() {
        return expressionPreview;
    }

    public int getK() {
        return k;
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

    public Neighbor getBest() {
        if (neighbors.isEmpty()) {
            return null;
        }
        return neighbors.get(0);
    }
}