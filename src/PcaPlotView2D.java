import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.*;
import java.util.function.Consumer;

public class PcaPlotView2D extends Pane implements PlotView {

    private List<PlotPoint> points = new ArrayList<>();
    private String selectedKey = null;
    private Set<String> highlighted = new HashSet<>();
    private Set<String> labels = new HashSet<>();

    private Consumer<String> clickCallback;

    private final Color baseColor = Color.rgb(50, 80, 255, 0.7);
    private final Color selectedColor = Color.ORANGE;
    private final Color neighborColor = Color.rgb(0, 170, 120, 0.85);

    private final Label hoverLabel;

    public PcaPlotView2D() {
        setPrefSize(900, 700);

        hoverLabel = new Label();
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true);
        hoverLabel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-border-color: rgba(0,0,0,0.35);" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 3 8 3 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        getChildren().add(hoverLabel);

        // redraw כשמשנים גודל חלון
        widthProperty().addListener((obs, a, b) -> redraw());
        heightProperty().addListener((obs, a, b) -> redraw());
    }

    // =========================================================
    // PlotView interface
    // =========================================================

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void setPoints(List<PlotPoint> pts) {
        this.points = (pts == null) ? new ArrayList<>() : pts;
        redraw();
    }

    @Override
    public void setSelectedKey(String key) {
        this.selectedKey = key;
        redraw();
    }

    @Override
    public void setHighlights(Set<String> keys) {
        this.highlighted = (keys == null) ? new HashSet<>() : new HashSet<>(keys);
        redraw();
    }

    @Override
    public void setLabels(Set<String> keys) {
        this.labels = (keys == null) ? new HashSet<>() : new HashSet<>(keys);
        redraw();
    }

    @Override
    public void setOnWordClicked(Consumer<String> callback) {
        this.clickCallback = callback;
    }

    // =========================================================
    // Drawing
    // =========================================================

    private void redraw() {

        getChildren().clear();
        getChildren().add(hoverLabel);
        hoverLabel.setVisible(false);

        if (points == null || points.isEmpty()) return;

        // ===== autoscale =====
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (PlotPoint p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        double w = getWidth() > 0 ? getWidth() : getPrefWidth();
        double h = getHeight() > 0 ? getHeight() : getPrefHeight();
        double pad = 30;

        double dx = (maxX - minX);
        if (dx == 0) dx = 1e-9;
        double dy = (maxY - minY);
        if (dy == 0) dy = 1e-9;

        List<Text> labelNodes = new ArrayList<>();

        for (PlotPoint p : points) {

            String key = p.getKey();

            double cx = pad + (p.getX() - minX) / dx * (w - 2 * pad);
            double cy = pad + (maxY - p.getY()) / dy * (h - 2 * pad);

            boolean isSelected = key != null && key.equals(selectedKey);
            boolean isNeighbor = key != null && highlighted.contains(key);

            double r;
            Color fill;

            if (isSelected) {
                r = 3.6;
                fill = selectedColor;
            } else if (isNeighbor) {
                r = 2.8;
                fill = neighborColor;
            } else {
                r = 2.1;
                fill = baseColor;
            }

            Circle dot = new Circle(cx, cy, r);
            dot.setFill(fill);

            // hitbox שקוף ללחיצה + hover
            Circle hit = new Circle(cx, cy, 10);
            hit.setFill(Color.TRANSPARENT);

            hit.setOnMouseEntered(e -> showHoverLabel(cx, cy, key));
            hit.setOnMouseMoved(e -> showHoverLabel(cx, cy, key));
            hit.setOnMouseExited(e -> hoverLabel.setVisible(false));

            hit.setOnMouseClicked(e -> {
                if (clickCallback != null && key != null) {
                    clickCallback.accept(key);
                }
            });

            getChildren().addAll(dot, hit);

            // ===== labels =====
            if (key != null && labels.contains(key)) {
                Text t = new Text(key);
                t.setMouseTransparent(true);
                t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                t.setX(cx + 6);
                t.setY(cy - 6);
                labelNodes.add(t);
            }
        }

        // labels מעל כולם
        getChildren().addAll(labelNodes);
        for (Text t : labelNodes) t.toFront();

        hoverLabel.toFront();
    }

    private void showHoverLabel(double x, double y, String text) {
        if (text == null) return;

        hoverLabel.setText(text);

        double lw = hoverLabel.prefWidth(-1);
        double lh = hoverLabel.prefHeight(-1);

        double lx = x - lw / 2.0;
        double ly = y - 14 - lh;

        if (lx < 5) lx = 5;
        if (ly < 5) ly = 5;

        hoverLabel.setLayoutX(lx);
        hoverLabel.setLayoutY(ly);
        hoverLabel.setVisible(true);
        hoverLabel.toFront();
    }
}
