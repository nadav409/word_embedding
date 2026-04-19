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
    private boolean pointsDirty;
    private boolean styleDirty;

    public void setPoints(List<PlotPoint> pts) {
        List<PlotPoint> next = (pts == null) ? List.of() : List.copyOf(pts);
        if (!this.points.equals(next)) {
            this.points = next;
            pointsDirty = true;
            applyPendingRefresh();
        }
    }

    public void setSelectedKey(String key) {
        if (!java.util.Objects.equals(this.selectedKey, key)) {
            this.selectedKey = key;
            styleDirty = true;
            applyPendingRefresh();
        }
    }

    public void setHighlights(Set<String> keys) {
        Set<String> next = (keys == null) ? Set.of() : Set.copyOf(keys);
        if (!this.highlighted.equals(next)) {
            this.highlighted = next;
            styleDirty = true;
            applyPendingRefresh();
        }
    }

    public void setGroupHighlights(Set<String> keys) {
        Set<String> next = (keys == null) ? Set.of() : Set.copyOf(keys);
        if (!this.groupKeys.equals(next)) {
            this.groupKeys = next;
            styleDirty = true;
            applyPendingRefresh();
        }
    }

    public void setLabels(Set<String> keys) {
        Set<String> next = (keys == null) ? Set.of() : Set.copyOf(keys);
        if (!this.labels.equals(next)) {
            this.labels = next;
            styleDirty = true;
            applyPendingRefresh();
        }
    }

    public void applyState(List<PlotPoint> pts,
                           String selected,
                           Set<String> highlights,
                           Set<String> groups,
                           Set<String> labelKeys) {
        List<PlotPoint> nextPoints = (pts == null) ? List.of() : List.copyOf(pts);
        if (!this.points.equals(nextPoints)) {
            this.points = nextPoints;
            pointsDirty = true;
        }

        if (!java.util.Objects.equals(this.selectedKey, selected)) {
            this.selectedKey = selected;
            styleDirty = true;
        }

        Set<String> nextHighlights = (highlights == null) ? Set.of() : Set.copyOf(highlights);
        if (!this.highlighted.equals(nextHighlights)) {
            this.highlighted = nextHighlights;
            styleDirty = true;
        }

        Set<String> nextGroups = (groups == null) ? Set.of() : Set.copyOf(groups);
        if (!this.groupKeys.equals(nextGroups)) {
            this.groupKeys = nextGroups;
            styleDirty = true;
        }

        Set<String> nextLabels = (labelKeys == null) ? Set.of() : Set.copyOf(labelKeys);
        if (!this.labels.equals(nextLabels)) {
            this.labels = nextLabels;
            styleDirty = true;
        }

        applyPendingRefresh();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.clickCallback = handler;
    }

    protected void requestPointsRefresh() {
        pointsDirty = true;
        applyPendingRefresh();
    }

    private void applyPendingRefresh() {
        if (!pointsDirty && !styleDirty) {
            return;
        }

        boolean pointsChanged = pointsDirty;
        boolean styleChanged = styleDirty;
        pointsDirty = false;
        styleDirty = false;
        refreshView(pointsChanged, styleChanged);
    }

    protected abstract void refreshView(boolean pointsChanged, boolean styleChanged);
}
