package controller;

import app.Provider;
import model.SpaceId;
import model.*;
import operations.*;

import java.util.ArrayList;
import java.util.List;

public class AppController {

    private final Provider provider;
    private final AxisSelection axes;

    public AppController(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider is null");
        }
        this.provider = provider;

        this.axes = new AxisSelection(0, 1);
    }

    public void setAxes2D(int x, int y) {
        axes.set2D(x, y);
    }

    public void setAxes3D(int x, int y, int z) {
        axes.set3D(x, y, z);
    }


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

    public CustomProjectionResult customProjection(String a, String b) {
        CustomProjectionOperation op = new CustomProjectionOperation(provider, SpaceId.FULL, a, b);
        return (CustomProjectionResult) op.execute();
    }

    public List<Neighbor> vectorArithmetic(VectorExpression expr, int k) {
        VectorArithmeticOperation op = new VectorArithmeticOperation(provider, SpaceId.FULL, expr, k);
        VectorArithmeticResult result = (VectorArithmeticResult) op.execute();
        return result.getNeighbors();
    }

    public List<Neighbor> subspaceGrouping(List<String> keys, int k) {
        SubspaceGroupingOperation op = new SubspaceGroupingOperation(provider, SpaceId.FULL, keys, k);
        SubspaceGroupingResult result = (SubspaceGroupingResult) op.execute();
        return result.getNeighbors();
    }


    public List<PlotPoint> getAllPcaPoints2D() {

        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);

        List<PlotPoint> points = new ArrayList<>(pcaSpace.size());

        for (Embedding embedding : pcaSpace.getAll()) {

            Vector v = embedding.getVector();

            int xIndex = axes.getXIndex();
            int yIndex = axes.getYIndex();

            double x = v.get(xIndex);
            double y = v.get(yIndex);

            points.add(new PlotPoint(embedding.getKey(), x, y));
        }

        return points;
    }

    public List<PlotPoint> getAllPcaPoints3D() {

        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);

        List<PlotPoint> points = new ArrayList<>(pcaSpace.size());

        for (Embedding embedding : pcaSpace.getAll()) {

            Vector v = embedding.getVector();

            int xIndex = axes.getXIndex();
            int yIndex = axes.getYIndex();
            int zIndex = axes.getZIndex();

            double x = v.get(xIndex);
            double y = v.get(yIndex);
            double z = v.get(zIndex);

            points.add(new PlotPoint(embedding.getKey(), x, y, z));
        }

        return points;
    }

    public int getPcaDimension() {
        EmbeddingSpace pcaSpace = provider.getSpace(SpaceId.PCA);
        return pcaSpace.dimension();
    }

    public double distanceBetween(String word1, String word2) {

        if (word1 == null || word1.isBlank() || word2 == null || word2.isBlank()) {
            throw new IllegalArgumentException("Words cannot be empty");
        }

        EmbeddingSpace space = provider.getSpace(SpaceId.FULL);

        Embedding e1 = space.get(word1);
        if (e1 == null) throw new UnknownWordException(word1);

        Embedding e2 = space.get(word2);
        if (e2 == null) throw new UnknownWordException(word2);

        return provider.getDistanceStrategy().compute(e1.getVector(), e2.getVector());
    }
}