import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VectorArithmeticOperation extends ResearchOperation {

    private final VectorExpression expression;
    private final int k;

    public VectorArithmeticOperation(Provider provider, SpaceId spaceId, VectorExpression expression, int k) {
        super(provider, spaceId);

        if (expression == null) throw new IllegalArgumentException("expression cannot be null");
        if (expression.isEmpty()) throw new IllegalArgumentException("expression cannot be empty");
        if (k <= 0) throw new IllegalArgumentException("k must be positive");

        this.expression = expression;
        this.k = k;
    }

    // ברירת מחדל: Top 5
    public VectorArithmeticOperation(Provider provider, SpaceId spaceId, VectorExpression expression) {
        this(provider, spaceId, expression, 5);
    }

    public VectorArithmeticOperation(Provider provider, VectorExpression expression, int k) {
        this(provider, SpaceId.FULL, expression, k);
    }

    public VectorArithmeticOperation(Provider provider, VectorExpression expression) {
        this(provider, SpaceId.FULL, expression, 5);
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        Vector result = null;
        Set<String> excluded = new HashSet<>();

        for (ExpressionStep step : expression.getSteps()) {

            String key = step.getKey();
            excluded.add(key);

            Embedding e = space.get(key);
            if (e == null) throw new UnknownWordException(key);

            Vector signed = e.getVector().scale(step.getOp().sign());

            result = (result == null) ? signed : result.add(signed);
        }

        List<Neighbor> top = NeighborFinder.findNearestByVector(space, result, k, excluded, metric());


        // אופציונלי: תיאור קצר בשביל UI/debug
        String preview = buildPreview();

        return new VectorArithmeticResult(preview, k, top);
    }

    private String buildPreview() {
        // רק תיאור, לא חובה
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ExpressionStep s : expression.getSteps()) {
            if (first) {
                // מילה ראשונה: נציג בלי +
                if (s.getOp() == CombineOp.MINUS) sb.append("-");
                sb.append(s.getKey());
                first = false;
            } else {
                sb.append(s.getOp() == CombineOp.PLUS ? " + " : " - ");
                sb.append(s.getKey());
            }
        }
        return sb.toString();
    }
}
