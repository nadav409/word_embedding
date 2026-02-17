public class PlotPoint {

    private final String key;
    private final double x;
    private final double y;

    public PlotPoint(String key, double x, double y) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("key cannot be null/blank");

        this.key = key;
        this.x = x;
        this.y = y;
    }

    public String getKey() { return key; }
    public double getX() { return x; }
    public double getY() { return y; }
}
