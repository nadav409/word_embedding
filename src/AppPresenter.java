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

    /** Called when the user clicks an item on the plot (or picks from search/autocomplete). */
    public void onItemSelected(String key) {
        if (key == null || key.isBlank()) return;

        uiState.setError("");
        uiState.setStatus("Selected: " + key);

        uiState.setSelectedKey(key);

        // Reset old neighbors display until user clicks Find
        uiState.setPrimaryResults(List.of());
        uiState.setHighlightedKeys(Set.of());
    }

    /** Called when user changes distance metric in NeighborsPane. */
    public void onMetricSelected(DistanceStrategy strategy) {
        if (strategy == null) return;

        controller.setDistanceStrategy(strategy);

        uiState.setError("");
        uiState.setMetric(strategy);
        uiState.setStatus("Metric: " + strategy.getClass().getSimpleName());
    }

    /** Called when user clicks 'Find neighbors' with some K. */
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

            // update results list
            uiState.setPrimaryResults(neighbors);

            // update highlights set for the plot
            LinkedHashSet<String> highlights = new LinkedHashSet<>();
            for (Neighbor n : neighbors) {
                String key = n.getKey();
                if (key != null && !key.equals(selected)) highlights.add(key);
            }
            uiState.setHighlightedKeys(highlights);

            uiState.setStatus("Found " + neighbors.size() + " neighbors");

        } catch (Exception ex) {
            uiState.setError("ERROR: " + ex.getMessage());
            uiState.setStatus("Failed");
            ex.printStackTrace();
        }
    }
}
