package ui;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import model.PlotPoint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PlotPane extends StackPane {
    private final PlotView plot2D;
    private final PlotView plot3D;
    private PlotView currentPlot;
    private List<PlotPoint> currentPoints;
    private String selectedKey;
    private Set<String> neighborKeys;
    private Set<String> groupKeys;

    private Consumer<String> itemClickHandler;

    public PlotPane(PlotView plot2D, PlotView plot3D) {
        this.plot2D = plot2D;
        this.plot3D = plot3D;

        this.currentPoints = List.of();
        this.selectedKey = null;
        this.neighborKeys = Set.of();
        this.groupKeys = Set.of();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        setPickOnBounds(true);

        setMode(false);
    }

    public void setMode(boolean use3D) {
        if (use3D) {
            currentPlot = plot3D;
        } else {
            currentPlot = plot2D;
        }

        getChildren().clear();
        getChildren().add(currentPlot);

        currentPlot.setOnItemClicked(key -> {
            if (itemClickHandler != null) {
                itemClickHandler.accept(key);
            }
        });

        updateCurrentPlot();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.itemClickHandler = handler;
    }

    public void setPoints(List<PlotPoint> points) {
        if (points == null) {
            this.currentPoints = List.of();
        } else {
            this.currentPoints = List.copyOf(points);
        }
        updateCurrentPlot();
    }

    public void setSelectedKey(String key) {
        this.selectedKey = key;
        updateCurrentPlot();
    }

    public void setNeighborHighlights(Set<String> keys) {
        if (keys == null) {
            this.neighborKeys = Set.of();
        } else {
            this.neighborKeys = Set.copyOf(keys);
        }

        updateCurrentPlot();
    }

    public void clearNeighborHighlights() {
        this.neighborKeys = Set.of();
        updateCurrentPlot();
    }

    public void setGroupHighlights(Set<String> keys) {
        if (keys == null) {
            this.groupKeys = Set.of();
        } else {
            this.groupKeys = Set.copyOf(keys);
        }

        updateCurrentPlot();
    }

    public void clearGroupHighlights() {
        this.groupKeys = Set.of();
        updateCurrentPlot();
    }

    private void updateCurrentPlot() {
        if (currentPlot == null) {
            return;
        }

        currentPlot.setPoints(currentPoints);
        currentPlot.setSelectedKey(selectedKey);
        currentPlot.setHighlights(neighborKeys);
        currentPlot.setGroupHighlights(groupKeys);
        currentPlot.setLabels(buildLabels());
    }

    private Set<String> buildLabels() {
        Set<String> labels = new HashSet<>();

        if (selectedKey != null && !selectedKey.isBlank()) {
            labels.add(selectedKey);
        }

        labels.addAll(neighborKeys);
        labels.addAll(groupKeys);

        return labels;
    }
}