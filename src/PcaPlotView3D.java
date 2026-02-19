import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.geometry.Point2D;

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
    private final Label hoverTooltip = new Label();

    private final Map<Label, Sphere> tagMap = new HashMap<>();

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

        hoverTooltip.setStyle("""
            -fx-background-color: rgba(20,20,20,0.85);
            -fx-text-fill: white;
            -fx-padding: 4 8 4 8;
            -fx-background-radius: 6;
            -fx-font-size: 11px;
            -fx-font-weight: bold;
        """);
        hoverTooltip.setVisible(false);

        overlay.setMouseTransparent(true);
        overlay.getChildren().add(hoverTooltip);

        getChildren().addAll(subScene, overlay);

        enableMouseControl();
    }

    @Override public Node getNode() { return this; }

    @Override
    public void setPoints(List<PlotPoint> points) {
        this.points = points;
        buildScene();
    }

    @Override
    public void setSelectedKey(String key) {
        this.selectedKey = key;
        buildScene();
    }

    @Override
    public void setHighlights(Set<String> keys) {
        this.highlighted = keys == null ? Set.of() : Set.copyOf(keys);
        buildScene();
    }

    @Override
    public void setGroupHighlights(Set<String> keys) {
        this.groupKeys = keys == null ? Set.of() : Set.copyOf(keys);
        buildScene();
    }

    @Override public void setLabels(Set<String> keys) {}

    @Override
    public void setOnItemClicked(Consumer<String> handler) {
        this.clickCallback = handler;
    }

    private void buildScene() {

        world.getChildren().removeIf(n -> n instanceof Sphere);
        overlay.getChildren().clear();
        overlay.getChildren().add(hoverTooltip);
        tagMap.clear();

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

            if (isSelected) material.setDiffuseColor(Color.ORANGE);
            else if (isGroup) material.setDiffuseColor(Color.LIMEGREEN);
            else if (isHighlighted) material.setDiffuseColor(Color.DEEPSKYBLUE);
            else material.setDiffuseColor(Color.rgb(50, 80, 255));

            sphere.setMaterial(material);
            sphere.setTranslateX(p.getX() * scale);
            sphere.setTranslateY(-p.getY() * scale);
            sphere.setTranslateZ((p.getZ() == null ? 0 : p.getZ()) * scale);

            sphere.setOnMouseClicked(e -> {
                if (clickCallback != null)
                    clickCallback.accept(key);
            });

            sphere.setOnMouseEntered(e -> showHover(sphere, key));
            sphere.setOnMouseExited(e -> hoverTooltip.setVisible(false));

            if (isSelected || isHighlighted || isGroup) {
                Label tag = createTag(key);
                overlay.getChildren().add(tag);
                tagMap.put(tag, sphere);
            }

            world.getChildren().add(sphere);
        }

        updateAllTagPositions();
    }

    private void showHover(Sphere sphere, String text) {
        hoverTooltip.setText(text);
        updateSinglePosition(sphere, hoverTooltip);
        hoverTooltip.setVisible(true);
    }

    private Label createTag(String text) {

        Label tag = new Label(text);

        tag.setStyle("""
            -fx-background-color: rgba(20,20,20,0.85);
            -fx-text-fill: white;
            -fx-padding: 3 7 3 7;
            -fx-background-radius: 6;
            -fx-font-size: 11px;
        """);

        tag.setMouseTransparent(true);
        return tag;
    }

    private void updateSinglePosition(Sphere sphere, Label label) {

        Point2D screenPoint = sphere.localToScreen(0, 0);
        if (screenPoint == null) return;

        Point2D local = this.screenToLocal(screenPoint);
        label.setLayoutX(local.getX() + 8);
        label.setLayoutY(local.getY() - 8);
    }

    private void updateAllTagPositions() {

        for (Map.Entry<Label, Sphere> entry : tagMap.entrySet()) {
            updateSinglePosition(entry.getValue(), entry.getKey());
        }

        if (hoverTooltip.isVisible()) {
            // reposition hover
            tagMap.values().stream().findFirst()
                    .ifPresent(s -> updateSinglePosition(s, hoverTooltip));
        }
    }

    private void enableMouseControl() {

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

            updateAllTagPositions();
        });

        subScene.setOnScroll(event -> {
            camTranslate.setZ(camTranslate.getZ() + event.getDeltaY() * 3.0);
            updateAllTagPositions();
        });
    }
}
