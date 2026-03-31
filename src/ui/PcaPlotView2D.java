package ui;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.PlotPoint;

public class PcaPlotView2D extends PlotView {

    private final Group world = new Group();
    private final Label hoverLabel;

    private double scale = 1.0;
    private double anchorX;
    private double anchorY;

    public PcaPlotView2D() {

        setPrefSize(900, 700);
        setStyle("-fx-background-color: #020617;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        world.setPickOnBounds(false);

        hoverLabel = new Label();
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true);
        hoverLabel.setStyle("""
            -fx-background-color: rgba(20,20,20,0.9);
            -fx-text-fill: white;
            -fx-padding: 4 8 4 8;
            -fx-background-radius: 8;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        getChildren().add(world);
        world.getChildren().add(hoverLabel);

        enableMouseControl();

        widthProperty().addListener((obs, oldValue, newValue) -> refreshView());
        heightProperty().addListener((obs, oldValue, newValue) -> refreshView());
    }

    @Override
    protected void refreshView() {
        redraw();
    }

    private void redraw() {

        world.getChildren().clear();
        world.getChildren().add(hoverLabel);
        hoverLabel.setVisible(false);

        if (points.isEmpty()) {
            return;
        }

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (PlotPoint p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        double w = getWidth();
        double h = getHeight();
        double pad = 40;

        double dx = Math.max(maxX - minX, 1e-9);
        double dy = Math.max(maxY - minY, 1e-9);

        for (PlotPoint p : points) {

            String key = p.getKey();

            double cx = pad + (p.getX() - minX) / dx * (w - 2 * pad);
            double cy = pad + (maxY - p.getY()) / dy * (h - 2 * pad);

            boolean isSelected = key != null && key.equals(selectedKey);
            boolean isNeighbor = key != null && highlighted.contains(key);
            boolean isGroup = key != null && groupKeys.contains(key);

            double radius;
            Color fill;

            if (isSelected) {
                radius = 5;
                fill = Color.web("#ff9d00");
            } else if (isNeighbor) {
                radius = 4.5;
                fill = Color.web("#39ff14");
            } else if (isGroup) {
                radius = 4.5;
                fill = Color.web("#b86cff");
            } else {
                radius = 3.2;
                fill = Color.web("#3b82f6");
            }

            Circle dot = new Circle(cx, cy, radius);
            dot.setFill(fill);

            DropShadow glow = new DropShadow();
            glow.setColor(fill);
            glow.setRadius(isSelected ? 16 : (isNeighbor || isGroup ? 10 : 5));
            dot.setEffect(glow);

            Circle hit = new Circle(cx, cy, 12);
            hit.setFill(Color.TRANSPARENT);

            hit.setOnMouseEntered(e -> showHoverLabel(cx, cy, key));
            hit.setOnMouseExited(e -> hoverLabel.setVisible(false));

            hit.setOnMouseClicked(e -> {
                if (clickCallback != null && key != null) {
                    clickCallback.accept(key);
                }
            });

            world.getChildren().addAll(dot, hit);

            if (key != null && labels.contains(key)) {
                Text t = new Text(key);
                t.setMouseTransparent(true);
                t.setFill(Color.WHITE);
                t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                t.setX(cx + 8);
                t.setY(cy - 8);
                world.getChildren().add(t);
            }
        }

        hoverLabel.toFront();
    }

    private void showHoverLabel(double x, double y, String text) {
        if (text == null) {
            return;
        }

        hoverLabel.setText(text);
        hoverLabel.setLayoutX(x + 8);
        hoverLabel.setLayoutY(y - 22);
        hoverLabel.setVisible(true);
        hoverLabel.toFront();
    }

    private void enableMouseControl() {

        setOnScroll(event -> {
            double zoomFactor = 1.08;

            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor;
            }

            scale *= zoomFactor;
            scale = Math.max(0.3, Math.min(5.0, scale));

            world.setScaleX(scale);
            world.setScaleY(scale);

            event.consume();
        });

        setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - anchorX;
            double deltaY = event.getSceneY() - anchorY;

            double newX = world.getTranslateX() + deltaX;
            double newY = world.getTranslateY() + deltaY;

            double maxTranslate = 2000;
            newX = Math.max(-maxTranslate, Math.min(maxTranslate, newX));
            newY = Math.max(-maxTranslate, Math.min(maxTranslate, newY));

            world.setTranslateX(newX);
            world.setTranslateY(newY);

            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });
    }
}