public class Neighbor {

    private final String key;
    private final double distance;

    public Neighbor(String key, double distance) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null/blank");
        }
        this.key = key;
        this.distance = distance;
    }

    public String getKey() {
        return key;
    }

    public double getDistance() {
        return distance;
    }
}
