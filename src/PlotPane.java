import javafx.scene.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlotPane implements UiStateListener {

    private final PlotView plotView;

    // ===== Local drawing state =====
    private String selectedKey = null;

    // 🟠 תוצאות (קרובים ל-centroid)
    private Set<String> resultKeys = Set.of();

    // 🟢 קבוצה
    private Set<String> groupKeys = Set.of();

    // callback החוצה (ל-Presenter)
    private Consumer<String> onItemClicked;

    public PlotPane(PlotView plotView) {
        if (plotView == null) throw new IllegalArgumentException("plotView is null");
        this.plotView = plotView;

        this.plotView.setOnItemClicked(key -> {
            if (onItemClicked != null) onItemClicked.accept(key);
        });
    }

    public Node getNode() {
        return plotView.getNode();
    }

    public void setPoints(List<PlotPoint> points) {
        plotView.setPoints(points);
        refreshLabels();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.onItemClicked = handler;
    }

    // =========================================================
    // UiStateListener
    // =========================================================

    @Override
    public void onSelectionChanged(String selectedKey) {
        this.selectedKey = selectedKey;
        plotView.setSelectedKey(selectedKey);
        refreshLabels();
    }

    // 🟠 תוצאות מגיעות דרך primaryResults
    @Override
    public void onPrimaryResultsChanged(List<Neighbor> results) {

        if (results == null || results.isEmpty()) {
            resultKeys = Set.of();
        } else {
            resultKeys = results.stream()
                    .map(Neighbor::getKey)
                    .filter(k -> k != null && !k.isBlank())
                    .collect(Collectors.toSet());
        }

        plotView.setHighlights(resultKeys);
        refreshLabels();
    }

    // 🟢 קבוצה מגיעה דרך highlightedKeys
    @Override
    public void onHighlightsChanged(Set<String> highlightedKeys) {

        this.groupKeys = (highlightedKeys == null)
                ? Set.of()
                : Set.copyOf(highlightedKeys);

        plotView.setGroupHighlights(groupKeys);
        refreshLabels();
    }

    @Override public void onMetricChanged(DistanceStrategy metric) { }
    @Override public void onStatusChanged(String message) { }
    @Override public void onErrorChanged(String message) { }
    @Override public void onOperationChanged(OperationType type) { }
    @Override public void onProjectionResultChanged(CustomProjectionResult res) { }

    // =========================================================
    // Labels
    // =========================================================

    private void refreshLabels() {

        Set<String> labels = new HashSet<>();

        if (selectedKey != null && !selectedKey.isBlank())
            labels.add(selectedKey);

        labels.addAll(groupKeys);
        labels.addAll(resultKeys);

        plotView.setLabels(labels);
    }

    public void setGroupHighlights(Set<String> keys) {
        if (plotView instanceof PcaPlotView2D pca) {
            pca.setGroupHighlights(keys);
        }
    }

}
