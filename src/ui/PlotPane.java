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
    private String selectedKey;
    private Set<String> neighborKeys;
    private Set<String> groupKeys;
    private Consumer<String> itemClickHandler;

    public PlotPane(PlotView plot2D, PlotView plot3D) {
        this.plot2D = plot2D;
        this.plot3D = plot3D;

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
        getChildren().add(currentPlot.getNode());

        currentPlot.setOnItemClicked(key -> {
            if (itemClickHandler != null) {
                itemClickHandler.accept(key);
            }
        });

        updateViews();
    }

    public void setOnItemClicked(Consumer<String> handler) {
        this.itemClickHandler = handler;
    }

    public void setPoints(List<PlotPoint> points) {
        plot2D.setPoints(points);
        plot3D.setPoints(points);
        updateLabels();
    }

    public void setSelectedKey(String key) {
        selectedKey = key;
        updateViews();
    }

    public void setNeighborHighlights(Set<String> keys) {
        if (keys == null) {
            neighborKeys = Set.of();
        } else {
            neighborKeys = Set.copyOf(keys);
        }
        updateViews();
    }

    public void clearNeighborHighlights() {
        neighborKeys = Set.of();
        updateViews();
    }

    public void setGroupHighlights(Set<String> keys) {
        if (keys == null) {
            groupKeys = Set.of();
        } else {
            groupKeys = Set.copyOf(keys);
        }
        updateViews();
    }

    public void clearGroupHighlights() {
        groupKeys = Set.of();
        updateViews();
    }

    private void updateViews() {
        plot2D.setSelectedKey(selectedKey);
        plot3D.setSelectedKey(selectedKey);

        plot2D.setHighlights(neighborKeys);
        plot3D.setHighlights(neighborKeys);

        plot2D.setGroupHighlights(groupKeys);
        plot3D.setGroupHighlights(groupKeys);

        updateLabels();
    }

    private void updateLabels() {
        Set<String> labels = new HashSet<>();

        if (selectedKey != null && !selectedKey.isBlank()) {
            labels.add(selectedKey);
        }

        labels.addAll(neighborKeys);
        labels.addAll(groupKeys);

        plot2D.setLabels(labels);
        plot3D.setLabels(labels);
    }
}