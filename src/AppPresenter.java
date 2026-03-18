import model.Neighbor;
import operations.CosineDistance;
import operations.DistanceStrategy;
import operations.EuclideanDistance;

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
    private final VectorArithmeticPane vectorArithmeticPane;

    private String selectedKey;

    public AppPresenter(AppController controller, NeighborsPane neighborsPane, PlotPane plotPane, DistancePane distancePane, GroupingPane groupingPane, CustomProjectionPane projectionPane, VectorArithmeticPane vectorPane) {

        if (controller == null || neighborsPane == null || plotPane == null || distancePane == null || groupingPane == null || projectionPane == null || vectorPane == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        this.controller = controller;
        this.neighborsPane = neighborsPane;
        this.plotPane = plotPane;
        this.distancePane = distancePane;
        this.groupingPane = groupingPane;
        this.projectionPane = projectionPane;
        this.vectorArithmeticPane = vectorPane;

        DistanceStrategy metric = controller.getDistanceStrategy();
        neighborsPane.setMetric(metric);
        distancePane.setMetric(metric);
        vectorPane.setMetric(metric);
    }

    public void onItemSelected(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        selectedKey = key;

        neighborsPane.setError("");
        neighborsPane.setStatus("Selected: " + key);
        neighborsPane.setSelectedWord(key);
        neighborsPane.clearResults();

        distancePane.acceptSelectedKey(key);
        distancePane.setError("");
        distancePane.clearResult();

        groupingPane.acceptSelectedKey(key);
        groupingPane.setError("");

        projectionPane.acceptSelectedKey(key);
        projectionPane.setError("");

        vectorArithmeticPane.acceptSelectedKey(key);

        plotPane.setSelectedKey(key);
        plotPane.clearNeighborHighlights();
    }

    public void onOperationSelected(OperationType op) {
        if (op == null) {
            return;
        }

        resetSelection();
        resetAllOperations();
        cleanPlot();

        DistanceStrategy metric = controller.getDistanceStrategy();
        neighborsPane.setMetric(metric);
        distancePane.setMetric(metric);
        vectorArithmeticPane.setMetric(metric);
    }

    public void onMetricSelected(MetricType type) {
        if (type == null) {
            return;
        }

        DistanceStrategy metric;
        if (type == MetricType.COSINE) {
            metric = new CosineDistance();
        } else {
            metric = new EuclideanDistance();
        }

        controller.setDistanceStrategy(metric);

        neighborsPane.setMetric(metric);
        neighborsPane.setError("");
        neighborsPane.setStatus("Metric: " + metric.getClass().getSimpleName());

        distancePane.setMetric(metric);
        distancePane.setError("");
        distancePane.clearResult();

        vectorArithmeticPane.setMetric(metric);
    }

    public void onFindNeighborsRequested(int k) {
        if (selectedKey == null || selectedKey.isBlank()) {
            neighborsPane.setError("No item selected");
            neighborsPane.setStatus("Select an item first");
            return;
        }

        if (k < 1) {
            k = 1;
        }

        try {
            neighborsPane.setError("");
            neighborsPane.setStatus("Searching neighbors...");

            List<Neighbor> neighbors = controller.nearestNeighbors(selectedKey, k);

            neighborsPane.showResults(neighbors);
            plotPane.setNeighborHighlights(buildNeighborKeySet(neighbors, Set.of(selectedKey)));
            plotPane.clearGroupHighlights();

            neighborsPane.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            cleanNeighbors();
            neighborsPane.setError("ERROR: " + ex.getMessage());
            neighborsPane.setStatus("Failed");
        }
    }

    public void onVectorResultRequested(VectorExpression expr, int k) {
        if (expr == null || expr.isEmpty()) {
            return;
        }

        if (k < 1) {
            k = 1;
        }

        try {
            plotPane.clearGroupHighlights();

            List<Neighbor> neighbors = controller.vectorArithmetic(expr, k);

            vectorArithmeticPane.showResults(neighbors);
            plotPane.setNeighborHighlights(buildNeighborKeySet(neighbors, Set.of()));

        } catch (Exception ex) {
            cleanVectorArithmetic();
            plotPane.clearNeighborHighlights();
        }
    }

    public void onProjectionRequested(String a, String b) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            return;
        }

        plotPane.clearNeighborHighlights();
        plotPane.clearGroupHighlights();

        projectionPane.setError("");
        projectionPane.setStatus("Projecting...");

        try {
            CustomProjectionResult result = controller.customProjection(a, b);

            projectionPane.showResult(result);
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
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            distancePane.setError("Select two items");
            distancePane.clearResult();
            plotPane.clearNeighborHighlights();
            return;
        }

        try {
            plotPane.clearGroupHighlights();

            distancePane.setError("");

            double distance = controller.distanceBetween(a, b);
            distancePane.showDistance(distance);

            plotPane.setNeighborHighlights(Set.of(a, b));

        } catch (Exception ex) {
            cleanDistance();
            distancePane.setError("Error: " + ex.getMessage());
            plotPane.clearNeighborHighlights();
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

        if (k < 1) {
            k = 1;
        }

        try {
            groupingPane.setError("");
            groupingPane.setStatus("Computing centroid...");

            plotPane.setGroupHighlights(new HashSet<>(keys));

            List<Neighbor> neighbors = controller.subspaceGrouping(keys, k);

            groupingPane.showResults(neighbors);
            plotPane.setNeighborHighlights(buildNeighborKeySet(neighbors, new HashSet<>(keys)));

            groupingPane.setStatus("Done");

        } catch (Exception ex) {
            cleanGrouping();
            groupingPane.setError("Error: " + ex.getMessage());
            plotPane.clearNeighborHighlights();
        }
    }

    private void resetSelection() {
        selectedKey = null;
        plotPane.setSelectedKey(null);
        neighborsPane.setSelectedWord("");
    }

    private void cleanNeighbors() {
        neighborsPane.clearResults();
        neighborsPane.setError("");
        neighborsPane.setStatus("");
    }

    private void cleanDistance() {
        distancePane.clearResult();
        distancePane.setError("");
    }

    private void cleanGrouping() {
        groupingPane.clearResults();
        groupingPane.setError("");
        groupingPane.setStatus("");
        plotPane.clearGroupHighlights();
    }

    private void cleanProjection() {
        projectionPane.clearResult();
        projectionPane.setError("");
        projectionPane.setStatus("");
    }

    private void cleanVectorArithmetic() {
        vectorArithmeticPane.clearResults();
    }

    private void resetAllOperations() {
        neighborsPane.resetPane();
        distancePane.resetPane();
        groupingPane.resetPane();
        projectionPane.resetPane();
        vectorArithmeticPane.resetPane();
    }

    private void cleanPlot() {
        plotPane.clearNeighborHighlights();
        plotPane.clearGroupHighlights();
    }

    private Set<String> buildNeighborKeySet(List<Neighbor> neighbors, Set<String> NotToHighlight) {
        Set<String> result = new LinkedHashSet<>();

        if (neighbors == null) {
            return result;
        }

        for (Neighbor neighbor : neighbors) {
            if (neighbor == null) {
                continue;
            }

            String key = neighbor.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }

            if (NotToHighlight != null && NotToHighlight.contains(key)) {
                continue;
            }

            result.add(key);
        }

        return result;
    }
}