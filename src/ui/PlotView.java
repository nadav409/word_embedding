package ui;

import javafx.scene.layout.Pane;
import model.PlotPoint;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class PlotView extends Pane {

    protected List<PlotPoint> points = List.of();
    protected String selectedKey = null;
    protected Set<String> highlighted = Set.of();
    protected Set<String> groupKeys = Set.of();
    protected Set<String> labels = Set.of();
    protected Consumer<String> clickCallback;

    public void setPoints(List<PlotPoint> pts) {
        this.points = (pts == null) ? List.of() : List.copyOf(pts);
        refreshView();
    }

    public void setSelectedKey(String key) {
        this.selectedKey = key;
        refreshView();
    }

    public void setHighlights(Set<String> keys) {
        this.highlighted = (keys == null) ? Set.of() : Set.copyOf(keys);
        refreshView();
    }

    public void setGroupHighlights(Set<String> keys) {
        this.groupKeys = (keys == null) ? Set.of() : Set.copyOf(keys);
        refreshView();
    }

    public void setLabels(Set<String> keys) {
        this.labels = (keys == null) ? Set.of() : Set.copyOf(keys);
        refreshView();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.clickCallback = handler;
    }

    protected abstract void refreshView();
}