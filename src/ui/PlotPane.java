package ui;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import model.PlotPoint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PlotPane extends StackPane {

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

        setMode(false);
    }

    public void setMode(boolean is3D) {
        PlotView newView = is3D ? view3D : view2D;

        if (activeView == newView) {
            return;
        }

        activeView = newView;

        getChildren().clear();
        getChildren().add(activeView.getNode());

        activeView.setOnItemClicked(key -> {
            if (onItemClicked != null) {
                onItemClicked.accept(key);
            }
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

    // ===== direct calls from presenter =====

    public void setSelectedKey(String key) {
        this.selectedKey = key;
        refreshState();
    }

    public void setNeighborHighlights(Set<String> keys) {
        this.resultKeys = (keys == null) ? Set.of() : Set.copyOf(keys);
        refreshState();
    }

    public void clearNeighborHighlights() {
        this.resultKeys = Set.of();
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

        if (selectedKey != null && !selectedKey.isBlank()) {
            labels.add(selectedKey);
        }

        labels.addAll(groupKeys);
        labels.addAll(resultKeys);

        view2D.setLabels(labels);
        view3D.setLabels(labels);
    }

    public void setGroupHighlights(Set<String> keys) {
        this.groupKeys = (keys == null) ? Set.of() : Set.copyOf(keys);
        refreshState();
    }

    public void clearGroupHighlights() {
        this.groupKeys = Set.of();
        refreshState();
    }

}