public abstract class OperationResult {

    private final OperationType type;

    protected OperationResult(OperationType type) {
        if (type == null) {
            throw new IllegalArgumentException("operation type cannot be null");
        }
        this.type = type;
    }

    public OperationType getType() {
        return type;
    }

    @Override
    public abstract String toString();
}