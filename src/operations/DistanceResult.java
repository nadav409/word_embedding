package operations;

public class DistanceResult implements OperationResult {

    private String keyA;
    private String keyB;
    private double distance;

    public DistanceResult(String keyA, String keyB, double distance) {
        this.keyA = keyA;
        this.keyB = keyB;
        this.distance = distance;
    }

    public String getKeyA() {
        return keyA;
    }

    public String getKeyB() {
        return keyB;
    }

    public double getDistance() {
        return distance;
    }
}