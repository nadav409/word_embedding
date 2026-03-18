package operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VectorExpression {

    private final List<ExpressionStep> steps = new ArrayList<>();

    public void add(CombineOp op, String key) {
        steps.add(new ExpressionStep(op, key));
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public List<ExpressionStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void clear() { steps.clear(); }

}

