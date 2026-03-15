import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DistancePane {

    private final VBox root = new VBox(10);

    private final TextField itemAField = new TextField();
    private final TextField itemBField = new TextField();

    private final ComboBox<MetricType> metricBox = new ComboBox<>();
    private final Button calcBtn = new Button("Calculate");

    private final Label resultLabel = new Label();
    private final Label errorLabel = new Label();

    private BiConsumer<String, String> distanceHandler;
    private Consumer<MetricType> metricHandler;

    public DistancePane(Consumer<TextField> installAutocomplete) {

        root.setPadding(new Insets(8));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");

        itemAField.setPromptText("Item A");
        itemBField.setPromptText("Item B");

        if (installAutocomplete != null) {
            installAutocomplete.accept(itemAField);
            installAutocomplete.accept(itemBField);
        }

        HBox row1 = new HBox(8, new Label("Item A:"), itemAField);
        HBox row2 = new HBox(8, new Label("Item B:"), itemBField);
        HBox.setHgrow(itemAField, Priority.ALWAYS);
        HBox.setHgrow(itemBField, Priority.ALWAYS);

        metricBox.getItems().addAll(MetricType.values());
        metricBox.getSelectionModel().select(MetricType.COSINE);

        HBox metricRow = new HBox(8, new Label("Metric:"), metricBox);

        calcBtn.setOnAction(e -> {
            if (distanceHandler != null) {
                distanceHandler.accept(
                        itemAField.getText(),
                        itemBField.getText()
                );
            }
        });

        metricBox.setOnAction(e -> {
            if (metricHandler != null) {
                metricHandler.accept(metricBox.getValue());
            }
        });

        resultLabel.setStyle("-fx-font-weight: bold;");
        errorLabel.setStyle("-fx-text-fill: #b00020;");

        root.getChildren().addAll(
                row1,
                row2,
                metricRow,
                calcBtn,
                resultLabel,
                errorLabel
        );
    }

    public Node getNode() {
        return root;
    }

    public void setOnDistanceRequested(BiConsumer<String, String> handler) {
        this.distanceHandler = handler;
    }

    public void setOnMetricSelected(Consumer<MetricType> handler) {
        this.metricHandler = handler;
    }

    public void setVisiblePane(boolean visible) {
        root.setVisible(visible);
        root.setManaged(visible);
    }

    public void acceptSelectedKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        if (itemAField.getText().isBlank()) {
            itemAField.setText(key);
        } else if (itemBField.getText().isBlank()) {
            itemBField.setText(key);
        } else {
            itemAField.setText(key);
            itemBField.clear();
        }
    }

    public void setMetric(DistanceStrategy metric) {
        if (metric instanceof EuclideanDistance) {
            metricBox.setValue(MetricType.EUCLIDEAN);
        } else {
            metricBox.setValue(MetricType.COSINE);
        }
    }

    public void showDistance(double dist) {
        resultLabel.setText("Distance = " + String.format(java.util.Locale.ROOT, "%.6f", dist));
    }

    public void clearResult() {
        resultLabel.setText("");
    }

    public void setError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }
}