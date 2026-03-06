import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppPresenter {

    private final AppController controller;
    private final UiState uiState;

    public AppPresenter(AppController controller, UiState uiState) {
        if (controller == null || uiState == null) {
            throw new IllegalArgumentException("controller/uiState cannot be null");
        }
        this.controller = controller;
        this.uiState = uiState;

        uiState.setMetric(controller.getDistanceStrategy());
    }

    public void onItemSelected(String key) {
        if (isBlank(key)) {
            return;
        }

        uiState.setError("");
        uiState.setStatus("Selected: " + key);
        uiState.setSelectedKey(key);

        clearResults();
    }

    public void onOperationSelected(OperationType op) {
        if (op == null) return;
        uiState.setSelectedOperation(op);
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

        uiState.setError("");
        uiState.setMetric(strategy);
        uiState.setStatus("Metric: " + strategy.getClass().getSimpleName());
    }

    public void onFindNeighborsRequested(int k) {
        String selected = uiState.getSelectedKey();
        if (isBlank(selected)) {
            uiState.setError("No item selected");
            uiState.setStatus("Select an item first");
            return;
        }

        k = normalizeK(k);

        try {
            uiState.setError("");
            uiState.setStatus("Searching neighbors…");

            List<Neighbor> neighbors = controller.nearestNeighbors(selected, k);
            uiState.setPrimaryResults(neighbors);
            uiState.setHighlightedKeys(buildHighlightsFromNeighbors(neighbors, Set.of(selected)));

            uiState.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            uiState.setError("ERROR: " + ex.getMessage());
            uiState.setStatus("Failed");
        }
    }

    public void onVectorResultRequested(VectorExpression expr, int k) {
        if (expr == null || expr.isEmpty()) return;

        k = normalizeK(k);

        uiState.setError("");
        uiState.setStatus("Computing vector expression...");

        try {
            List<Neighbor> neighbors = controller.vectorArithmetic(expr, k);

            uiState.setPrimaryResults(neighbors);
            uiState.setHighlightedKeys(buildHighlightsFromNeighbors(neighbors, Set.of()));

            uiState.setStatus("Done");

        } catch (UnknownWordException ex) {
            clearResults();
            uiState.setError("Unknown word: " + ex.getMessage());

        } catch (Exception ex) {
            clearResults();
            uiState.setError("Error: " + ex.getMessage());
        }
    }

    public void onProjectionRequested(String a, String b, int k) {
        if (isBlank(a) || isBlank(b)) return;

        k = normalizeK(k);

        uiState.setError("");
        uiState.setStatus("Projecting...");

        try {
            CustomProjectionResult res = controller.customProjection(a, b, k);
            uiState.setProjectionResult(res);
            uiState.setStatus("Done");

        } catch (UnknownWordException ex) {
            uiState.setProjectionResult(null);
            uiState.setStatus("");
            uiState.setError("Unknown word: " + ex.getMessage());

        } catch (Exception ex) {
            uiState.setProjectionResult(null);
            uiState.setStatus("");
            uiState.setError("Error: " + ex.getMessage());
        }
    }

    public void onDistanceRequested(String a, String b) {
        if (isBlank(a) || isBlank(b)) {
            uiState.setError("Select two items");
            return;
        }

        try {
            uiState.setError("");

            double dist = controller.distanceBetween(a, b);
            String msg = "Distance = " + String.format(java.util.Locale.ROOT, "%.6f", dist);

            uiState.setStatus(msg);
            uiState.setHighlightedKeys(Set.of(a, b));

        } catch (Exception ex) {
            uiState.setError("Error: " + ex.getMessage());
            uiState.setHighlightedKeys(Set.of());
        }
    }

    public void onGroupingRequested(List<String> keys, int k) {
        if (keys == null || keys.size() < 2) {
            uiState.setError("Select at least 2 items");
            return;
        }

        k = normalizeK(k);

        try {
            uiState.setError("");
            uiState.setStatus("Computing centroid...");

            uiState.setHighlightedKeys(new HashSet<>(keys));

            List<Neighbor> neighbors = controller.subspaceGrouping(keys, k);
            uiState.setPrimaryResults(neighbors);

            uiState.setStatus("Done");

        } catch (Exception ex) {
            uiState.setError("Error: " + ex.getMessage());
            uiState.setStatus("");
        }
    }

    private void clearResults() {
        uiState.setPrimaryResults(List.of());
        uiState.setHighlightedKeys(Set.of());
        uiState.setStatus("");
    }

    private static int normalizeK(int k) {
        return (k < 1) ? 1 : k;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Set<String> buildHighlightsFromNeighbors(List<Neighbor> neighbors, Set<String> excluded) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (neighbors == null) return out;

        for (Neighbor n : neighbors) {
            if (n == null) continue;
            String key = n.getKey();
            if (key == null || key.isBlank()) continue;
            if (excluded != null && excluded.contains(key)) continue;
            out.add(key);
        }
        return out;
    }
}