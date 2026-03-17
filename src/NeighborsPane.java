import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class NeighborsPane {

    private final VBox root = new VBox(10);

    private final Label selectedLabel = new Label("Selected: (none)");
    private final TextField searchField = new TextField();
    private final TextField kField = new TextField("10");
    private final Button findBtn = new Button("Find neighbors");
    private final ListView<String> resultsList = new ListView<>();

    private final RadioButton cosineBtn = new RadioButton("Cosine");
    private final RadioButton euclideanBtn = new RadioButton("Euclidean");
    private final ToggleGroup metricGroup = new ToggleGroup();

    private final Label statusLabel = new Label("");
    private final Label errorLabel = new Label("");

    private Consumer<String> onSearchPicked;
    private Consumer<Integer> onFindNeighborsRequested;
    private Consumer<MetricType> onMetricSelected;

    public NeighborsPane(Consumer<TextField> installAutocomplete) {

        selectedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        statusLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b00020;");

        searchField.setPromptText("Search item…");
        kField.setPrefColumnCount(5);
        resultsList.setPrefHeight(320);

        findBtn.setDisable(true);

        cosineBtn.setToggleGroup(metricGroup);
        euclideanBtn.setToggleGroup(metricGroup);
        cosineBtn.setSelected(true);

        if (installAutocomplete != null) {
            installAutocomplete.accept(searchField);
        }

        HBox searchRow = new HBox(8, new Label("Search:"), searchField);
        searchRow.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox metricRow = new HBox(10, new Label("Distance:"), cosineBtn, euclideanBtn);
        metricRow.setStyle("-fx-alignment: center-left;");

        HBox actionRow = new HBox(8, new Label("K:"), kField, findBtn);
        actionRow.setStyle("-fx-alignment: center-left;");

        root.getChildren().addAll(selectedLabel, searchRow, metricRow, actionRow, new Label("Nearest neighbors (key | distance):"), resultsList, statusLabel, errorLabel);

        root.setPadding(new Insets(8));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");

        findBtn.setOnAction(e -> requestFindNeighbors());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String text = searchField.getText();

                if (text != null) {
                    text = text.trim();

                    if (!text.isBlank()) {
                        fireSearchPicked(text);
                    }
                }

                e.consume();
            }
        });

        kField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                requestFindNeighbors();
                e.consume();
            }
        });

        metricGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            if (newValue == cosineBtn) {
                fireMetricSelected(MetricType.COSINE);
            } else {
                fireMetricSelected(MetricType.EUCLIDEAN);
            }
        });
    }

    public Node getNode() {
        return root;
    }

    public void setOnSearchPicked(Consumer<String> handler) {
        this.onSearchPicked = handler;
    }

    public void setOnFindNeighborsRequested(Consumer<Integer> handler) {
        this.onFindNeighborsRequested = handler;
    }

    public void setOnMetricSelected(Consumer<MetricType> handler) {
        this.onMetricSelected = handler;
    }

    public void setSelectedWord(String key) {
        if (key == null || key.isBlank()) {
            selectedLabel.setText("Selected: (none)");
            findBtn.setDisable(true);
            return;
        }

        selectedLabel.setText("Selected: " + key);
        findBtn.setDisable(false);
    }

    public void setMetric(DistanceStrategy metric) {
        if (metric instanceof EuclideanDistance) {
            euclideanBtn.setSelected(true);
        } else {
            cosineBtn.setSelected(true);
        }
    }

    public void showResults(List<Neighbor> results) {
        resultsList.getItems().clear();

        if (results == null || results.isEmpty()) {
            resultsList.getItems().add("(no neighbors returned)");
            return;
        }

        for (Neighbor neighbor : results) {
            String line = neighbor.getKey() + "  |  " +
                    String.format(java.util.Locale.ROOT, "%.6f", neighbor.getDistance());
            resultsList.getItems().add(line);
        }
    }

    public void clearResults() {
        resultsList.getItems().clear();
    }

    public void resetPane() {
        searchField.clear();
        kField.setText("10");
        clearResults();
        setError("");
        setStatus("");
        setSelectedWord("");
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    public void setError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    private void requestFindNeighbors() {
        if (onFindNeighborsRequested == null) {
            return;
        }

        int k = parseKOrDefault();
        onFindNeighborsRequested.accept(k);
    }

    private int parseKOrDefault() {
        int k;

        try {
            k = Integer.parseInt(kField.getText().trim());
        } catch (Exception ex) {
            k = 10;
            kField.setText("10");
        }

        if (k < 1) {
            k = 1;
            kField.setText("1");
        }

        return k;
    }

    private void fireSearchPicked(String key) {
        if (onSearchPicked != null) {
            onSearchPicked.accept(key);
        }
    }

    private void fireMetricSelected(MetricType type) {
        if (onMetricSelected != null) {
            onMetricSelected.accept(type);
        }
    }
}