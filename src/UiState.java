import java.util.*;

public class UiState {

    private final List<UiStateListener> listeners = new ArrayList<>();

    private OperationType selectedOperation = OperationType.NEIGHBORS;
    private CustomProjectionResult projectionResult;

    private String selectedKey;
    private DistanceStrategy metric = new CosineDistance();

    private List<Neighbor> primaryResults = List.of();
    private Set<String> highlightedKeys = Set.of();

    // ===== Status / Error =====
    private String status = "";
    private String error = "";

    // =========================================
    // Listener management
    // =========================================

    public void addListener(UiStateListener l) {
        if (l == null) throw new IllegalArgumentException("listener is null");
        listeners.add(l);
    }

    // =========================================
    // Getters
    // =========================================

    public OperationType getSelectedOperation() {
        return selectedOperation;
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public DistanceStrategy getMetric() {
        return metric;
    }

    public List<Neighbor> getPrimaryResults() {
        return primaryResults;
    }

    public Set<String> getHighlightedKeys() {
        return highlightedKeys;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    // =========================================
    // Setters + Notifications
    // =========================================

    public void setSelectedOperation(OperationType op) {
        if (op == null) return;

        this.selectedOperation = op;

        for (UiStateListener l : listeners) {
            l.onOperationChanged(op);
        }
    }

    public void setSelectedKey(String key) {
        this.selectedKey = key;
        for (UiStateListener l : listeners) {
            l.onSelectionChanged(key);
        }
    }

    public void setMetric(DistanceStrategy metric) {
        this.metric = (metric == null) ? new CosineDistance() : metric;
        for (UiStateListener l : listeners) {
            l.onMetricChanged(this.metric);
        }
    }

    public void setPrimaryResults(List<Neighbor> results) {
        this.primaryResults = (results == null) ? List.of() : List.copyOf(results);
        for (UiStateListener l : listeners) {
            l.onPrimaryResultsChanged(this.primaryResults);
        }
    }

    public void setHighlightedKeys(Set<String> keys) {
        this.highlightedKeys = (keys == null) ? Set.of() : Set.copyOf(keys);
        for (UiStateListener l : listeners) {
            l.onHighlightsChanged(this.highlightedKeys);
        }
    }

    public void setStatus(String msg) {
        this.status = (msg == null) ? "" : msg;
        for (UiStateListener l : listeners) {
            l.onStatusChanged(this.status);
        }
    }

    public void setError(String msg) {
        this.error = (msg == null) ? "" : msg;
        for (UiStateListener l : listeners) {
            l.onErrorChanged(this.error);
        }
    }
    public CustomProjectionResult getProjectionResult() {
        return projectionResult;
    }
    public void setProjectionResult(CustomProjectionResult res) {
        this.projectionResult = res;
        for (UiStateListener l : listeners) {
            l.onProjectionResultChanged(this.projectionResult);
        }
    }


}
