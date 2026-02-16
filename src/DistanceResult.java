public class DistanceResult extends OperationResult {

    private final String keyA;
    private final String keyB;
    private final double distance;

    public DistanceResult(String keyA, String keyB, double distance) {
        super(OperationType.DISTANCE);
        this.keyA = keyA;
        this.keyB = keyB;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "Distance between '" + keyA + "' and '" + keyB + "' = " + distance;
    }
}

