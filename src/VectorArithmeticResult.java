import java.util.ArrayList;
import java.util.List;

public class VectorArithmeticResult extends OperationResult {

    private final String expressionPreview; // אופציונלי (ל-debug/UI)
    private final int k;
    private final List<Neighbor> topK;

    public VectorArithmeticResult(String expressionPreview, int k, List<Neighbor> topK) {
        super(OperationType.ARITHMETIC);

        if (k <= 0) throw new IllegalArgumentException("k must be positive");
        if (topK == null) throw new IllegalArgumentException("topK cannot be null");

        this.expressionPreview = (expressionPreview == null) ? "" : expressionPreview;
        this.k = k;
        this.topK = new ArrayList<>(topK);
    }

    public int getK() {
        return k;
    }

    public List<Neighbor> getTopK() {
        return new ArrayList<>(topK);
    }

    // הכי קרוב (Top-1)
    public Neighbor getBest() {
        return topK.isEmpty() ? null : topK.get(0);
    }

    @Override
    public String toString() {
        Neighbor best = getBest();
        if (best == null) return "VectorArithmeticResult{k=" + k + ", empty}";
        return "VectorArithmeticResult{k=" + k + ", best=" + best.getKey() + ", dist=" + best.getDistance() + "}";
    }

    public List<Neighbor> getNeighbors() {
        return getTopK();
    }
}
