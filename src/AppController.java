import java.util.List;

public class AppController {

    private final Provider provider;
    private final AxisSelection axes;

    public AppController(Provider provider) {
        if (provider == null)
            throw new IllegalArgumentException("provider is null");

        this.provider = provider;

        // תצוגה מתחילה ב-PCA על שני הרכיבים הראשונים
        this.axes = new AxisSelection(0, 1);
    }

    public void setAxes2D(int x, int y) {
        axes.set2D(x, y);
    }

    public void setAxes3D(int x, int y, int z) {
        axes.set3D(x, y, z);
    }

    public AxisSelection getAxes() {
        return axes;
    }

    // ✅ allow FX to change metric through controller only
    public void setDistanceStrategy(DistanceStrategy strategy) {
        provider.setDistanceStrategy(strategy);
    }

    public DistanceStrategy getDistanceStrategy() {
        return provider.getDistanceStrategy();
    }

    public List<Neighbor> nearestNeighbors(String word, int k) {
        NearestNeighborsOperation op = new NearestNeighborsOperation(provider, SpaceId.FULL, word, k);
        NearestNeighborsResult result = (NearestNeighborsResult) op.execute();
        return result.getNeighbors();
    }

    public double[] getPoint(String word) {

        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);
        Embedding e = pcaSpace.get(word);

        if (e == null)
            throw new UnknownWordException(word);

        Vector v = e.getVector();

        double x = v.get(axes.getXIndex());
        double y = v.get(axes.getYIndex());

        if (!axes.is3D()) {
            return new double[]{x, y};
        }

        double z = v.get(axes.getZIndex());
        return new double[]{x, y, z};
    }

    public List<PlotPoint> getAllPcaPoints2D() {

        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);

        List<PlotPoint> points = new java.util.ArrayList<>(pcaSpace.size());

        for (Embedding e : pcaSpace.getAll()) {

            Vector v = e.getVector();

            double x = v.get(axes.getXIndex());
            double y = v.get(axes.getYIndex());

            points.add(new PlotPoint(e.getKey(), x, y));
        }

        return points;
    }

    public List<PlotPoint> getAllPcaPoints3D() {

        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);

        List<PlotPoint> points = new java.util.ArrayList<>(pcaSpace.size());

        for (Embedding e : pcaSpace.getAll()) {

            Vector v = e.getVector();

            double x = v.get(axes.getXIndex());
            double y = v.get(axes.getYIndex());
            double z = v.get(axes.getZIndex());

            points.add(new PlotPoint(e.getKey(), x, y, z));
        }

        return points;
    }


    public int getPcaDimension() {
        return provider.getSpace(SpaceId.PCA).dimension();
    }

    public CustomProjectionResult customProjection(String a, String b, int k) {
        CustomProjectionOperation op =
                new CustomProjectionOperation(provider, SpaceId.FULL, a, b, k);

        return (CustomProjectionResult) op.execute();
    }
    public List<Neighbor> vectorArithmetic(VectorExpression expr, int k) {
        VectorArithmeticOperation op = new VectorArithmeticOperation(provider, SpaceId.FULL, expr, k);
        VectorArithmeticResult result = (VectorArithmeticResult) op.execute();
        return result.getTopK();
    }

    public double distanceBetween(String word1, String word2) {

        if (word1 == null || word1.isBlank() ||
                word2 == null || word2.isBlank()) {
            throw new IllegalArgumentException("Words cannot be empty");
        }

        EmbeddingSpace space = provider.getSpace(SpaceId.FULL);

        Embedding e1 = space.get(word1);
        Embedding e2 = space.get(word2);

        if (e1 == null) throw new UnknownWordException(word1);
        if (e2 == null) throw new UnknownWordException(word2);

        DistanceStrategy strategy = provider.getDistanceStrategy();

        return strategy.compute(e1.getVector(), e2.getVector());
    }

    public List<Neighbor> subspaceGrouping(List<String> keys, int k) {

        SubspaceGroupingOperation op =
                new SubspaceGroupingOperation(provider, SpaceId.FULL, keys, k);

        SubspaceGroupingResult result =
                (SubspaceGroupingResult) op.execute();

        return result.getNeighbors();
    }






}
