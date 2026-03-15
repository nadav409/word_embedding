import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppPresenter {

    private final AppController controller;
    private final NeighborsPane neighborsPane;
    private final PlotPane plotPane;
    private final DistancePane distancePane;
    private final GroupingPane groupingPane;
    private final CustomProjectionPane projectionPane;
    private final VectorArithmeticPane vectorPane;

    private String selectedKey;

    public AppPresenter(AppController controller,
                        NeighborsPane neighborsPane,
                        PlotPane plotPane,
                        DistancePane distancePane,
                        GroupingPane groupingPane,
                        CustomProjectionPane projectionPane,
                        VectorArithmeticPane vectorPane) {
        if (controller == null || neighborsPane == null
                || plotPane == null || distancePane == null
                || groupingPane == null || projectionPane == null
                || vectorPane == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        this.controller = controller;
        this.neighborsPane = neighborsPane;
        this.plotPane = plotPane;
        this.distancePane = distancePane;
        this.groupingPane = groupingPane;
        this.projectionPane = projectionPane;
        this.vectorPane = vectorPane;

        DistanceStrategy initialMetric = controller.getDistanceStrategy();
        neighborsPane.setMetric(initialMetric);
        distancePane.setMetric(initialMetric);
        vectorPane.setMetric(initialMetric);
    }

    public void onItemSelected(String key) {
        if (isBlank(key)) {
            return;
        }

        selectedKey = key;

        neighborsPane.setError("");
        neighborsPane.setStatus("Selected: " + key);
        neighborsPane.setSelectedWord(key);

        distancePane.acceptSelectedKey(key);
        distancePane.setError("");
        distancePane.clearResult();

        groupingPane.acceptSelectedKey(key);
        groupingPane.setError("");

        projectionPane.acceptSelectedKey(key);
        projectionPane.setError("");

        vectorPane.acceptSelectedKey(key);

        plotPane.setSelectedKey(key);

        clearNeighborsOutput();
    }

    public void onOperationSelected(OperationType op) {
        if (op == null) {
            return;
        }

        DistanceStrategy strategy = controller.getDistanceStrategy();
        neighborsPane.setMetric(strategy);
        distancePane.setMetric(strategy);
        vectorPane.setMetric(strategy);
    }

    public void onMetricSelected(MetricType type) {
        if (type == null) {
            return;
        }

        DistanceStrategy strategy;
        if (type == MetricType.COSINE) {
            strategy = new CosineDistance();
        } else {
            strategy = new EuclideanDistance();
        }

        controller.setDistanceStrategy(strategy);

        neighborsPane.setMetric(strategy);
        neighborsPane.setError("");
        neighborsPane.setStatus("Metric: " + strategy.getClass().getSimpleName());

        distancePane.setMetric(strategy);
        distancePane.setError("");
        distancePane.clearResult();

        vectorPane.setMetric(strategy);
    }

    public void onFindNeighborsRequested(int k) {
        if (isBlank(selectedKey)) {
            neighborsPane.setError("No item selected");
            neighborsPane.setStatus("Select an item first");
            return;
        }

        k = normalizeK(k);

        try {
            neighborsPane.setError("");
            neighborsPane.setStatus("Searching neighbors...");

            List<Neighbor> neighbors = controller.nearestNeighbors(selectedKey, k);

            neighborsPane.showResults(neighbors);
            plotPane.setNeighborHighlights(
                    buildHighlightsFromNeighbors(neighbors, Set.of(selectedKey))
            );
            plotPane.clearGroupHighlights();

            neighborsPane.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            clearNeighborsOutput();
            neighborsPane.setError("ERROR: " + ex.getMessage());
            neighborsPane.setStatus("Failed");
        }
    }

    public void onVectorResultRequested(VectorExpression expr, int k) {
        if (expr == null || expr.isEmpty()) {
            return;
        }

        k = normalizeK(k);

        try {
            plotPane.clearGroupHighlights();

            List<Neighbor> neighbors = controller.vectorArithmetic(expr, k);

            vectorPane.showResults(neighbors);
            plotPane.setNeighborHighlights(
                    buildHighlightsFromNeighbors(neighbors, Set.of())
            );

        } catch (Exception ex) {
            vectorPane.clearResults();
            plotPane.clearNeighborHighlights();
        }
    }

    public void onProjectionRequested(String a, String b, int k) {
        if (isBlank(a) || isBlank(b)) {
            return;
        }

        k = normalizeK(k);

        plotPane.clearNeighborHighlights();
        plotPane.clearGroupHighlights();

        projectionPane.setError("");
        projectionPane.setStatus("Projecting...");

        try {
            CustomProjectionResult res = controller.customProjection(a, b, k);

            projectionPane.showResult(res);
            plotPane.setNeighborHighlights(Set.of(a, b));

            projectionPane.setStatus("Done");

        } catch (UnknownWordException ex) {
            projectionPane.clearResult();
            projectionPane.setStatus("");
            projectionPane.setError("Unknown word: " + ex.getMessage());

        } catch (Exception ex) {
            projectionPane.clearResult();
            projectionPane.setStatus("");
            projectionPane.setError("Error: " + ex.getMessage());
        }
    }

    public void onDistanceRequested(String a, String b) {
        if (isBlank(a) || isBlank(b)) {
            distancePane.setError("Select two items");
            distancePane.clearResult();
            plotPane.setNeighborHighlights(Set.of());
            return;
        }

        try {
            plotPane.clearGroupHighlights();

            distancePane.setError("");

            double dist = controller.distanceBetween(a, b);
            distancePane.showDistance(dist);

            plotPane.setNeighborHighlights(Set.of(a, b));

        } catch (Exception ex) {
            distancePane.setError("Error: " + ex.getMessage());
            distancePane.clearResult();
            plotPane.setNeighborHighlights(Set.of());
        }
    }

    public void onGroupingRequested(List<String> keys, int k) {
        if (keys == null || keys.size() < 2) {
            groupingPane.setError("Select at least 2 items");
            groupingPane.setStatus("");
            groupingPane.clearResults();
            plotPane.clearGroupHighlights();
            plotPane.clearNeighborHighlights();
            return;
        }

        k = normalizeK(k);

        try {
            groupingPane.setError("");
            groupingPane.setStatus("Computing centroid...");

            plotPane.setGroupHighlights(new HashSet<>(keys));

            List<Neighbor> neighbors = controller.subspaceGrouping(keys, k);

            groupingPane.showResults(neighbors);

            plotPane.setNeighborHighlights(
                    buildHighlightsFromNeighbors(neighbors, new HashSet<>(keys))
            );

            groupingPane.setStatus("Done");

        } catch (Exception ex) {
            groupingPane.setError("Error: " + ex.getMessage());
            groupingPane.setStatus("");
            groupingPane.clearResults();
            plotPane.clearNeighborHighlights();
        }
    }

    private void clearNeighborsOutput() {
        neighborsPane.clearResults();
        neighborsPane.setStatus("");
        plotPane.clearNeighborHighlights();
    }

    private static int normalizeK(int k) {
        return (k < 1) ? 1 : k;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Set<String> buildHighlightsFromNeighbors(List<Neighbor> neighbors, Set<String> excluded) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (neighbors == null) {
            return out;
        }

        for (Neighbor n : neighbors) {
            if (n == null) {
                continue;
            }

            String key = n.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }

            if (excluded != null && excluded.contains(key)) {
                continue;
            }

            out.add(key);
        }

        return out;
    }
}