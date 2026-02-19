import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppPresenter {

    private final AppController controller;
    private final UiState uiState;

    public AppPresenter(AppController controller, UiState uiState) {
        this.controller = controller;
        this.uiState = uiState;

        DistanceStrategy initialMetric = controller.getDistanceStrategy();
        uiState.setMetric(initialMetric);
    }

    // -------- Events coming from UI --------

    public void onItemSelected(String key) {
        if (key == null || key.isBlank()) return;

        uiState.setError("");
        uiState.setStatus("Selected: " + key);

        uiState.setSelectedKey(key);

        // reset old results until user runs operation
        uiState.setPrimaryResults(List.of());
        uiState.setHighlightedKeys(Set.of());
    }

    private void applyMetric(DistanceStrategy strategy) {
        if (strategy == null) return;

        controller.setDistanceStrategy(strategy);

        uiState.setError("");
        uiState.setMetric(strategy);
        uiState.setStatus("Metric: " + strategy.getClass().getSimpleName());
    }

    public void onMetricSelected(MetricType type) {
        if (type == null) return;

        DistanceStrategy strategy =
                (type == MetricType.COSINE)
                        ? new CosineDistance()
                        : new EuclideanDistance();

        applyMetric(strategy);
    }

    public void onFindNeighborsRequested(int k) {
        String selected = uiState.getSelectedKey();
        if (selected == null || selected.isBlank()) {
            uiState.setError("No item selected");
            uiState.setStatus("Select an item first");
            return;
        }

        if (k < 1) k = 1;

        try {
            uiState.setError("");
            uiState.setStatus("Searching neighbors…");

            List<Neighbor> neighbors = controller.nearestNeighbors(selected, k);

            uiState.setPrimaryResults(neighbors);

            LinkedHashSet<String> highlights = new LinkedHashSet<>();
            for (Neighbor n : neighbors) {
                String nk = n.getKey();
                if (nk != null && !nk.equals(selected)) highlights.add(nk);
            }
            uiState.setHighlightedKeys(highlights);

            uiState.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            uiState.setError("ERROR: " + ex.getMessage());
            uiState.setStatus("Failed");
            ex.printStackTrace();
        }
    }

    public void onOperationSelected(OperationType op) {
        uiState.setSelectedOperation(op);
    }

    // ✅ UPDATED: now receives K from VectorArithmeticPane
    public void onVectorResultRequested(VectorExpression expr, int k) {
        if (expr == null || expr.isEmpty()) return;
        if (k < 1) k = 1;

        uiState.setError("");
        uiState.setStatus("Computing vector expression...");

        try {
            // metric already controlled by onMetricSelected → controller.setDistanceStrategy(...)
            List<Neighbor> neighbors = controller.vectorArithmetic(expr, k);

            uiState.setPrimaryResults(neighbors);

            uiState.setHighlightedKeys(
                    neighbors.stream()
                            .map(Neighbor::getKey)
                            .filter(s -> s != null && !s.isBlank())
                            .collect(java.util.stream.Collectors.toSet())
            );

            uiState.setStatus("Done");

        } catch (UnknownWordException ex) {
            uiState.setPrimaryResults(List.of());
            uiState.setHighlightedKeys(Set.of());
            uiState.setStatus("");
            uiState.setError("Unknown word: " + ex.getMessage());
        } catch (Exception ex) {
            uiState.setPrimaryResults(List.of());
            uiState.setHighlightedKeys(Set.of());
            uiState.setStatus("");
            uiState.setError("Error: " + ex.getMessage());
        }
    }
    public void onProjectionRequested(String a, String b, int k) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) return;
        if (k < 1) k = 1;

        uiState.setError("");
        uiState.setStatus("Projecting...");

        try {
            CustomProjectionResult res = controller.customProjection(a, b, k);

            // ✅ זה ה-OOP הנכון: Presenter מעדכן state
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

        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            uiState.setError("Select two items");
            return;
        }

        try {
            uiState.setError("");

            double dist = controller.distanceBetween(a, b);

            uiState.setStatus("Distance = " +
                    String.format(java.util.Locale.ROOT, "%.6f", dist));

            // 🔥 כאן הקסם
            uiState.setHighlightedKeys(Set.of(a, b));

        } catch (Exception ex) {
            uiState.setError("Error: " + ex.getMessage());
            uiState.setHighlightedKeys(Set.of());
        }
    }



}
