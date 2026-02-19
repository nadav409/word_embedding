public class AxisSelection {

    private int xIndex;
    private int yIndex;
    private Integer zIndex; // null = 2D

    public AxisSelection(int xIndex, int yIndex) {
        set2D(xIndex, yIndex);
    }

    public void set2D(int xIndex, int yIndex) {
        if (xIndex < 0 || yIndex < 0)
            throw new IllegalArgumentException("index must be >= 0");

        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.zIndex = null;
    }

    public void set3D(int xIndex, int yIndex, int zIndex) {
        if (xIndex < 0 || yIndex < 0 || zIndex < 0)
            throw new IllegalArgumentException("indices must be >= 0");

        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.zIndex = zIndex;
    }

    public int getXIndex() {
        return xIndex;
    }
    public int getYIndex() {
        return yIndex;
    }
    public Integer getZIndex() {
        return zIndex;
    }

    public boolean is3D() {
        return zIndex != null;
    }
}
