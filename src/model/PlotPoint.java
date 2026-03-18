package model;

public class PlotPoint {

    private final String key;
    private final double x;
    private final double y;
    private final Double z; // null אם זה 2D

    // ===== 2D constructor =====
    public PlotPoint(String key, double x, double y) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("key cannot be null/blank");

        this.key = key;
        this.x = x;
        this.y = y;
        this.z = null;
    }

    // ===== 3D constructor =====
    public PlotPoint(String key, double x, double y, double z) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("key cannot be null/blank");

        this.key = key;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getKey() { return key; }
    public double getX() { return x; }
    public double getY() { return y; }

    public Double getZ() { return z; }

    public boolean is3D() {
        return z != null;
    }
}
