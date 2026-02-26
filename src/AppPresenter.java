import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppPresenter {

    private final AppController controller;
    private final UiState uiState;

    public AppPresenter(AppController controller, UiState uiState) {
        this.controller = controller;
        this.uiState = uiState;

        // Initialize UI with the controller's current metric
        DistanceStrategy initialMetric = controller.getDistanceStrategy();
        uiState.setMetric(initialMetric);
    }

    // =========================
    // Events coming from UI
    // =========================

    public void onItemSelected(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        // Clear previous error and update status
        uiState.setError("");
        uiState.setStatus("Selected: " + key);

        // Save selection in state
        uiState.setSelectedKey(key);

        // Reset old results until user runs an operation
        uiState.setPrimaryResults(List.of());
        uiState.setHighlightedKeys(Set.of());
    }

    public void onOperationSelected(OperationType op) {
        uiState.setSelectedOperation(op);
    }

    // =========================
    // Metric selection
    // =========================

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

        applyMetric(strategy);
    }

    private void applyMetric(DistanceStrategy strategy) {
        if (strategy == null) {
            return;
        }

        // Update controller (source of truth for metric)
        controller.setDistanceStrategy(strategy);

        // Update UI state
        uiState.setError("");
        uiState.setMetric(strategy);
        uiState.setStatus("Metric: " + strategy.getClass().getSimpleName());
    }

    // =========================
    // Neighbors
    // =========================

    public void onFindNeighborsRequested(int k) {

        String selected = uiState.getSelectedKey();
        if (selected == null || selected.isBlank()) {
            uiState.setError("No item selected");
            uiState.setStatus("Select an item first");
            return;
        }

        if (k < 1) {
            k = 1;
        }

        try {
            uiState.setError("");
            uiState.setStatus("Searching neighbors…");

            List<Neighbor> neighbors = controller.nearestNeighbors(selected, k);

            uiState.setPrimaryResults(neighbors);

            // Build highlight set from results (excluding the selected word)
            LinkedHashSet<String> highlights = new LinkedHashSet<>();
            for (Neighbor n : neighbors) {
                String nk = n.getKey();
                if (nk != null && !nk.isBlank() && !nk.equals(selected)) {
                    highlights.add(nk);
                }
            }
            uiState.setHighlightedKeys(highlights);

            uiState.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            uiState.setError("ERROR: " + ex.getMessage());
            uiState.setStatus("Failed");
            ex.printStackTrace();
        }
    }

    // =========================
    // Vector arithmetic
    // =========================

    public void onVectorResultRequested(VectorExpression expr, int k) {
        if (expr == null || expr.isEmpty()) {
            return;
        }
        if (k < 1) {
            k = 1;
        }

        uiState.setError("");
        uiState.setStatus("Computing vector expression...");

        try {
            // Metric is already controlled by onMetricSelected -> controller.setDistanceStrategy(...)
            List<Neighbor> neighbors = controller.vectorArithmetic(expr, k);

            uiState.setPrimaryResults(neighbors);

            Set<String> highlights = new HashSet<>();
            for (Neighbor n : neighbors) {
                String key = n.getKey();
                if (key != null && !key.isBlank()) {
                    highlights.add(key);
                }
            }
            uiState.setHighlightedKeys(highlights);

            uiState.setStatus("Done");

        } catch (UnknownWordException ex) {
            clearResults();
            uiState.setError("Unknown word: " + ex.getMessage());

        } catch (Exception ex) {
            clearResults();
            uiState.setError("Error: " + ex.getMessage());
        }
    }

    // =========================
    // Custom projection
    // =========================

    public void onProjectionRequested(String a, String b, int k) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            return;
        }
        if (k < 1) {
            k = 1;
        }

        uiState.setError("");
        uiState.setStatus("Projecting...");

        try {
            CustomProjectionResult res = controller.customProjection(a, b, k);

            // Presenter updates state; UI listens and renders
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

    // =========================
    // Distance between two words
    // =========================

    public void onDistanceRequested(String a, String b) {

        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            uiState.setError("Select two items");
            return;
        }

        try {
            uiState.setError("");

            double dist = controller.distanceBetween(a, b);

            String msg = "Distance = " + String.format(java.util.Locale.ROOT, "%.6f", dist);
            uiState.setStatus(msg);

            // Highlight both words
            uiState.setHighlightedKeys(Set.of(a, b));

        } catch (Exception ex) {
            uiState.setError("Error: " + ex.getMessage());
            uiState.setHighlightedKeys(Set.of());
        }
    }

    // =========================
    // Grouping / centroid neighbors
    // =========================

    public void onGroupingRequested(List<String> keys, int k) {

        if (keys == null || keys.size() < 2) {
            uiState.setError("Select at least 2 items");
            return;
        }

        try {
            uiState.setError("");
            uiState.setStatus("Computing centroid...");

            // First: highlight the selected group
            uiState.setHighlightedKeys(new HashSet<>(keys));

            // Then: compute neighbors around the centroid
            List<Neighbor> neighbors = controller.subspaceGrouping(keys, k);

            uiState.setPrimaryResults(neighbors);

            uiState.setStatus("Done");

        } catch (Exception ex) {
            uiState.setError("Error: " + ex.getMessage());
            uiState.setStatus("");
        }
    }

    // =========================
    // Helpers
    // =========================

    private void clearResults() {
        uiState.setPrimaryResults(List.of());
        uiState.setHighlightedKeys(Set.of());
        uiState.setStatus("");
    }
}