import javafx.scene.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PlotPane implements UiStateListener {

    private final PlotView plotView;

    // state מקומי לציור
    private String selectedKey = null;
    private Set<String> highlightedKeys = Set.of();

    // callback החוצה (ל-Presenter)
    private Consumer<String> onItemClicked;

    public PlotPane(PlotView plotView) {
        if (plotView == null) throw new IllegalArgumentException("plotView is null");
        this.plotView = plotView;

        // Delegate: כשהמשתמש לוחץ על נקודה בגרף -> נודיע למי שמחוץ ל-PlotPane
        this.plotView.setOnItemClicked(key -> {
            if (onItemClicked != null) onItemClicked.accept(key);
        });
    }

    public Node getNode() {
        return plotView.getNode();
    }

    // FxApp עדיין ישלח נקודות אחרי redraw
    public void setPoints(List<PlotPoint> points) {
        plotView.setPoints(points);
        refreshLabels();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.onItemClicked = handler;
    }

    // ===== UiStateListener implementation =====

    @Override
    public void onSelectionChanged(String selectedKey) {
        this.selectedKey = selectedKey;
        plotView.setSelectedKey(selectedKey);
        refreshLabels();
    }

    @Override
    public void onHighlightsChanged(Set<String> highlightedKeys) {
        this.highlightedKeys = (highlightedKeys == null) ? Set.of() : Set.copyOf(highlightedKeys);
        plotView.setHighlights(this.highlightedKeys);
        refreshLabels();
    }


    // כרגע PlotPane לא חייב להגיב לתוצאות/metric/status/error
    @Override
    public void onMetricChanged(DistanceStrategy metric) { }

    @Override
    public void onPrimaryResultsChanged(List<Neighbor> results) { }

    @Override
    public void onStatusChanged(String message) { }

    @Override
    public void onErrorChanged(String message) { }

    // ===== helper =====
    private void refreshLabels() {
        Set<String> labels = new HashSet<>();
        if (selectedKey != null && !selectedKey.isBlank()) labels.add(selectedKey);
        if (highlightedKeys != null) labels.addAll(highlightedKeys);
        plotView.setLabels(labels);
    }
}

