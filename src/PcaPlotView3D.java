import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.*;
import java.util.function.Consumer;

public class PcaPlotView3D extends Pane implements PlotView {

    private List<PlotPoint> points = List.of();
    private String selectedKey = null;
    private Set<String> highlighted = Set.of();
    private Set<String> groupKeys = Set.of();

    private Consumer<String> clickCallback;

    private final Group world = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate camRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate camRotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate camTranslate = new Translate(0, 0, -1500);

    private final Pane overlay = new Pane();
    private final Label tooltip = new Label();

    private double anchorX;
    private double anchorY;

    public PcaPlotView3D() {

        setPrefSize(900, 700);

        subScene = new SubScene(world, 900, 700, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(240, 240, 240));
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());

        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(35);

        camera.getTransforms().addAll(camRotateY, camRotateX, camTranslate);
        subScene.setCamera(camera);

        AmbientLight ambient = new AmbientLight(Color.WHITE);
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-1000);

        world.getChildren().addAll(ambient, light);

        tooltip.setStyle("""
            -fx-background-color: white;
            -fx-padding: 4 6 4 6;
            -fx-border-color: gray;
            -fx-font-size: 11px;
        """);
        tooltip.setVisible(false);

        overlay.setMouseTransparent(true);
        overlay.getChildren().add(tooltip);

        getChildren().addAll(subScene, overlay);

        enableMouseControl();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void setPoints(List<PlotPoint> points) {
        this.points = points;
        redraw();
    }

    @Override
    public void setSelectedKey(String key) {
        this.selectedKey = key;
        redraw();
    }

    @Override
    public void setHighlights(Set<String> keys) {
        this.highlighted = keys == null ? Set.of() : Set.copyOf(keys);
        redraw();
    }

    @Override
    public void setGroupHighlights(Set<String> keys) {
        this.groupKeys = keys == null ? Set.of() : Set.copyOf(keys);
        redraw();
    }

    @Override
    public void setLabels(Set<String> keys) {}

    @Override
    public void setOnItemClicked(Consumer<String> handler) {
        this.clickCallback = handler;
    }

    private void redraw() {

        world.getChildren().removeIf(n -> n instanceof Sphere);

        tooltip.setVisible(false);

        if (points.isEmpty()) return;

        double scale = 250;

        for (PlotPoint p : points) {

            String key = p.getKey();

            boolean isSelected = key.equals(selectedKey);
            boolean isHighlighted = highlighted.contains(key);
            boolean isGroup = groupKeys.contains(key);

            Sphere sphere = new Sphere(
                    isSelected ? 6 :
                            isHighlighted || isGroup ? 5 : 3
            );

            PhongMaterial material = new PhongMaterial();

            if (isSelected) {
                material.setDiffuseColor(Color.ORANGE);
            } else if (isGroup) {
                material.setDiffuseColor(Color.LIMEGREEN);
            } else if (isHighlighted) {
                material.setDiffuseColor(Color.DEEPSKYBLUE);
            } else {
                material.setDiffuseColor(Color.rgb(50, 80, 255));
            }

            sphere.setMaterial(material);

            sphere.setTranslateX(p.getX() * scale);
            sphere.setTranslateY(-p.getY() * scale);
            sphere.setTranslateZ((p.getZ() == null ? 0 : p.getZ()) * scale);

            sphere.setOnMouseClicked(e -> {
                if (clickCallback != null)
                    clickCallback.accept(key);
            });

            sphere.setOnMouseEntered(e -> showTooltip(sphere, key));
            sphere.setOnMouseExited(e -> {
                if (!isSelected && !isHighlighted && !isGroup)
                    tooltip.setVisible(false);
            });

            if (isSelected || isHighlighted || isGroup) {
                showTooltip(sphere, key);
            }

            world.getChildren().add(sphere);
        }
    }

    private void showTooltip(Sphere sphere, String text) {

        tooltip.setText(text);

        javafx.geometry.Point2D screenPoint =
                sphere.localToScreen(0, 0);

        if (screenPoint == null) return;

        javafx.geometry.Point2D localPoint =
                this.screenToLocal(screenPoint);

        tooltip.setLayoutX(localPoint.getX() + 8);
        tooltip.setLayoutY(localPoint.getY() - 8);
        tooltip.setVisible(true);
    }


    private void enableMouseControl() {

        subScene.setFocusTraversable(true);

        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {

            double deltaX = event.getSceneX() - anchorX;
            double deltaY = event.getSceneY() - anchorY;

            if (event.isPrimaryButtonDown()) {
                camRotateY.setAngle(camRotateY.getAngle() + deltaX * 0.4);
                camRotateX.setAngle(camRotateX.getAngle() - deltaY * 0.4);
            }

            if (event.isSecondaryButtonDown()) {
                camTranslate.setX(camTranslate.getX() - deltaX * 2);
                camTranslate.setY(camTranslate.getY() - deltaY * 2);
            }

            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });

        subScene.setOnScroll(event ->
                camTranslate.setZ(camTranslate.getZ() + event.getDeltaY() * 3.0)
        );
    }
}
