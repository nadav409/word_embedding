package ui;

import javafx.geometry.Point2D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import model.PlotPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PcaPlotView3D extends PlotView {

    private final Group world = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate camRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate camRotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate camTranslate = new Translate(0, 0, -1500);

    private final Pane overlay = new Pane();
    private final Label hoverTooltip = new Label();

    private final Map<String, Sphere> spheresByKey = new HashMap<>();
    private final Map<String, Label> labelsByKey = new HashMap<>();

    private double anchorX;
    private double anchorY;

    public PcaPlotView3D() {

        subScene = new SubScene(world, 900, 700, true, SceneAntialiasing.BALANCED);
        subScene.setPickOnBounds(true);
        subScene.setFill(Color.rgb(5, 8, 20));
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
        subScene.setFocusTraversable(false);

        enableMouseControl();
    }

    @Override
    protected void refreshView(boolean pointsChanged, boolean styleChanged) {
        if (pointsChanged) {
            buildScene();
            return;
        }

        if (styleChanged) {
            updateVisualState();
        }
    }

    private void buildScene() {

        world.getChildren().removeIf(node -> node instanceof Sphere);
        overlay.getChildren().clear();
        overlay.getChildren().add(hoverTooltip);
        hoverTooltip.setVisible(false);

        spheresByKey.clear();
        labelsByKey.clear();

        if (points.isEmpty()) {
            return;
        }

        double scale = 250;

        for (PlotPoint p : points) {

            String key = p.getKey();
            boolean isSelected = key != null && key.equals(selectedKey);
            boolean isHighlighted = key != null && highlighted.contains(key);
            boolean isGroup = key != null && groupKeys.contains(key);

            Sphere sphere = new Sphere(
                    isSelected ? 6 : (isHighlighted || isGroup ? 5 : 3)
            );

            sphere.setPickOnBounds(false);

            PhongMaterial material = new PhongMaterial();
            Glow glow = new Glow();

            if (isSelected) {
                material.setDiffuseColor(Color.web("#ff9800"));
                material.setSpecularColor(Color.WHITE);
                material.setSpecularPower(64);
                glow.setLevel(0.9);
            } else if (isHighlighted) {
                material.setDiffuseColor(Color.web("#39ff14"));
                material.setSpecularColor(Color.web("#ccffcc"));
                material.setSpecularPower(48);
                glow.setLevel(0.85);
            } else if (isGroup) {
                material.setDiffuseColor(Color.web("#b86cff"));
                material.setSpecularColor(Color.WHITE);
                material.setSpecularPower(64);
                glow.setLevel(0.9);
            } else {
                material.setDiffuseColor(Color.web("#3d7bff"));
                material.setSpecularColor(Color.web("#99ccff"));
                material.setSpecularPower(32);
                glow.setLevel(0.2);
            }

            sphere.setMaterial(material);
            sphere.setEffect(glow);

            sphere.setTranslateX(p.getX() * scale);
            sphere.setTranslateY(-p.getY() * scale);
            sphere.setTranslateZ((p.getZ() == null ? 0 : p.getZ()) * scale);

            sphere.setOnMouseClicked(e -> {
                if (clickCallback != null && key != null) {
                    clickCallback.accept(key);
                }
            });

            sphere.setOnMouseEntered(e -> showHover(sphere, key));
            sphere.setOnMouseExited(e -> hoverTooltip.setVisible(false));

            if (key != null) {
                spheresByKey.put(key, sphere);
                if (labels.contains(key)) {
                    addLabelForKey(key, sphere);
                }
            }

            world.getChildren().add(sphere);
        }

        updateAllTagPositions();
    }

    private void updateVisualState() {
        for (Map.Entry<String, Sphere> entry : spheresByKey.entrySet()) {
            applySphereStyle(entry.getValue(), entry.getKey());
        }

        syncLabels();
        updateAllTagPositions();
    }

    private void applySphereStyle(Sphere sphere, String key) {
        if (sphere == null || key == null) {
            return;
        }

        boolean isSelected = key.equals(selectedKey);
        boolean isHighlighted = highlighted.contains(key);
        boolean isGroup = groupKeys.contains(key);

        sphere.setRadius(isSelected ? 6 : (isHighlighted || isGroup ? 5 : 3));

        PhongMaterial material;
        if (sphere.getMaterial() instanceof PhongMaterial) {
            material = (PhongMaterial) sphere.getMaterial();
        } else {
            material = new PhongMaterial();
            sphere.setMaterial(material);
        }

        Glow glow;
        if (sphere.getEffect() instanceof Glow) {
            glow = (Glow) sphere.getEffect();
        } else {
            glow = new Glow();
            sphere.setEffect(glow);
        }

        if (isSelected) {
            material.setDiffuseColor(Color.web("#ff9800"));
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(64);
            glow.setLevel(0.9);
        } else if (isHighlighted) {
            material.setDiffuseColor(Color.web("#39ff14"));
            material.setSpecularColor(Color.web("#ccffcc"));
            material.setSpecularPower(48);
            glow.setLevel(0.85);
        } else if (isGroup) {
            material.setDiffuseColor(Color.web("#b86cff"));
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(64);
            glow.setLevel(0.9);
        } else {
            material.setDiffuseColor(Color.web("#3d7bff"));
            material.setSpecularColor(Color.web("#99ccff"));
            material.setSpecularPower(32);
            glow.setLevel(0.2);
        }
    }

    private void syncLabels() {
        Set<String> toRemove = new HashSet<>(labelsByKey.keySet());
        toRemove.removeAll(labels);

        for (String key : toRemove) {
            Label tag = labelsByKey.remove(key);
            if (tag != null) {
                overlay.getChildren().remove(tag);
            }
        }

        for (String key : labels) {
            if (labelsByKey.containsKey(key)) {
                continue;
            }

            Sphere sphere = spheresByKey.get(key);
            if (sphere != null) {
                addLabelForKey(key, sphere);
            }
        }
    }

    private void addLabelForKey(String key, Sphere sphere) {
        Label tag = createTag(key);
        labelsByKey.put(key, tag);
        overlay.getChildren().add(tag);
        updateSinglePosition(sphere, tag);
    }

    private void showHover(Sphere sphere, String text) {
        if (text == null) {
            return;
        }

        hoverTooltip.setText(text);
        updateSinglePosition(sphere, hoverTooltip);
        hoverTooltip.setVisible(true);
    }

    private Label createTag(String text) {
        Label tag = new Label(text);

        tag.setStyle("""
            -fx-background-color: rgba(30,30,40,0.95);
            -fx-text-fill: #6ec1ff;
            -fx-padding: 4 9 4 9;
            -fx-background-radius: 8;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        tag.setMouseTransparent(true);
        return tag;
    }

    private void updateSinglePosition(Sphere sphere, Label label) {
        Point2D screenPoint = sphere.localToScreen(0, 0);

        if (screenPoint == null) {
            return;
        }

        Point2D local = this.screenToLocal(screenPoint);
        label.setLayoutX(local.getX() + 8);
        label.setLayoutY(local.getY() - 8);
    }

    private void updateAllTagPositions() {
        for (Map.Entry<String, Label> entry : labelsByKey.entrySet()) {
            Sphere sphere = spheresByKey.get(entry.getKey());
            if (sphere != null) {
                updateSinglePosition(sphere, entry.getValue());
            }
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

        subScene.setOnMouseReleased(event -> requestFocus());
    }
}