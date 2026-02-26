import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlotPane extends StackPane implements UiStateListener {

    private final PlotView view2D;
    private final PlotView view3D;
    private PlotView activeView;

    private String selectedKey = null;
    private Set<String> resultKeys = Set.of();
    private Set<String> groupKeys = Set.of();

    private Consumer<String> onItemClicked;

    public PlotPane(PlotView view2D, PlotView view3D) {
        this.view2D = view2D;
        this.view3D = view3D;

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        setPickOnBounds(true);

        setMode(false); // default 2D
    }

    public void setMode(boolean is3D) {

        PlotView newView = is3D ? view3D : view2D;

        if (activeView == newView)
            return;

        activeView = newView;

        getChildren().clear();
        getChildren().add(activeView.getNode());

        activeView.setOnItemClicked(key -> {
            if (onItemClicked != null)
                onItemClicked.accept(key);
        });

        refreshState();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.onItemClicked = handler;
    }

    public void setPoints(List<PlotPoint> points) {
        view2D.setPoints(points);
        view3D.setPoints(points);
        refreshLabels();
    }

    // =========================
    // UiStateListener
    // =========================

    @Override
    public void onSelectionChanged(String key) {
        this.selectedKey = key;
        refreshState();
    }

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

        refreshState();
    }

    @Override
    public void onHighlightsChanged(Set<String> highlightedKeys) {

        groupKeys = highlightedKeys == null
                ? Set.of()
                : Set.copyOf(highlightedKeys);

        refreshState();
    }

    private void refreshState() {
        view2D.setSelectedKey(selectedKey);
        view3D.setSelectedKey(selectedKey);

        view2D.setHighlights(resultKeys);
        view3D.setHighlights(resultKeys);

        view2D.setGroupHighlights(groupKeys);
        view3D.setGroupHighlights(groupKeys);

        refreshLabels();
    }

    private void refreshLabels() {

        Set<String> labels = new HashSet<>();

        if (selectedKey != null && !selectedKey.isBlank())
            labels.add(selectedKey);

        labels.addAll(groupKeys);
        labels.addAll(resultKeys);

        view2D.setLabels(labels);
        view3D.setLabels(labels);
    }

    @Override public void onMetricChanged(DistanceStrategy metric) {}
    @Override public void onStatusChanged(String message) {}
    @Override public void onErrorChanged(String message) {}
    @Override public void onOperationChanged(OperationType type) {}
    @Override public void onProjectionResultChanged(CustomProjectionResult res) {}
}
