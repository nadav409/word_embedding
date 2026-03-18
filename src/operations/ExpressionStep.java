package operations;

public class ExpressionStep {

    private final CombineOp op;
    private final String key;

    public ExpressionStep(CombineOp op, String key) {
        if (op == null) throw new IllegalArgumentException("op cannot be null");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key cannot be null/blank");
        this.op = op;
        this.key = key;
    }

    public CombineOp getOp() {
        return op;
    }

    public String getKey() {
        return key;
    }
}


