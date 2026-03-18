package operations;

import app.Provider;
import app.SpaceId;
import model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VectorArithmeticOperation extends ResearchOperation {

    private final VectorExpression expression;
    private final int k;

    public VectorArithmeticOperation(Provider provider, SpaceId spaceId, VectorExpression expression, int k) {
        super(provider, spaceId);

        if (expression == null)
            throw new IllegalArgumentException("expression cannot be null");

        if (expression.isEmpty())
            throw new IllegalArgumentException("expression cannot be empty");

        if (k <= 0)
            throw new IllegalArgumentException("k must be positive");

        this.expression = expression;
        this.k = k;
    }

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

        // 1. חישוב הוקטור הסופי של הביטוי
        for (ExpressionStep step : expression.getSteps()) {

            String key = step.getKey();
            excluded.add(key);

            Embedding e = space.get(key);
            if (e == null)
                throw new UnknownWordException(key);

            Vector signed = e.getVector().scale(step.getOp().sign());

            if (result == null) {
                result = signed;
            } else {
                result = result.add(signed);
            }
        }

        // 2. מציאת הקרובים לוקטור החדש
        List<Neighbor> top = space.findNearest(
                result,
                k,
                excluded,
                metric()
        );

        // 3. תיאור לביטוי (לא חובה, רק UI)
        String preview = buildPreview();

        return new VectorArithmeticResult(preview, k, top);
    }

    private String buildPreview() {

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (ExpressionStep s : expression.getSteps()) {

            if (first) {
                if (s.getOp() == CombineOp.MINUS)
                    sb.append("-");
                sb.append(s.getKey());
                first = false;
            } else {
                if (s.getOp() == CombineOp.PLUS)
                    sb.append(" + ");
                else
                    sb.append(" - ");

                sb.append(s.getKey());
            }
        }

        return sb.toString();
    }
}