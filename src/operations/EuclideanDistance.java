package operations;

import model.Vector;

public class EuclideanDistance implements DistanceStrategy {
    @Override
    public double compute(Vector a, Vector b) {
        return a.sub(b).norm();
    }
}

