import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NeighborsPane implements UiStateListener {

    private final VBox root = new VBox(10);

    // UI
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

    // callbacks (to Presenter)
    private Consumer<String> onSearchPicked;
    private Consumer<Integer> onFindNeighborsRequested;
    private Consumer<MetricType> onMetricSelected;   // ✅ UI enum, not DistanceStrategy

    // local state
    private String selectedKey;

    public NeighborsPane(Consumer<TextField> installAutocomplete) {

        selectedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        searchField.setPromptText("Search item…");
        HBox searchRow = new HBox(8, new Label("Search:"), searchField);
        searchRow.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        if (installAutocomplete != null) {
            installAutocomplete.accept(searchField);
        }

        // Metric row
        cosineBtn.setToggleGroup(metricGroup);
        euclideanBtn.setToggleGroup(metricGroup);

        // default selection
        cosineBtn.setSelected(true);

        HBox metricRow = new HBox(10, new Label("Distance:"), cosineBtn, euclideanBtn);
        metricRow.setStyle("-fx-alignment: center-left;");

        metricGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) return;
            if (newT == cosineBtn) fireMetricSelected(MetricType.COSINE);
            else if (newT == euclideanBtn) fireMetricSelected(MetricType.EUCLIDEAN);
        });

        // Action row
        kField.setPrefColumnCount(5);
        findBtn.setDisable(true);

        HBox actionRow = new HBox(8, new Label("K:"), kField, findBtn);
        actionRow.setStyle("-fx-alignment: center-left;");

        resultsList.setPrefHeight(320);

        statusLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b00020;");

        root.getChildren().addAll(
                selectedLabel,
                searchRow,
                metricRow,
                actionRow,
                new Label("Nearest neighbors (key | distance):"),
                resultsList,
                statusLabel,
                errorLabel
        );

        root.setPadding(new Insets(8));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");

        // Events → Presenter
        findBtn.setOnAction(e -> requestFindNeighbors());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String raw = (searchField.getText() == null) ? "" : searchField.getText().trim();
                if (!raw.isBlank()) fireSearchPicked(raw);
                e.consume();
            }
        });

        kField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                requestFindNeighbors();
                e.consume();
            }
        });
    }

    public Node getNode() {
        return root;
    }

    // ---------- Wiring (Presenter sets these) ----------

    public void setOnSearchPicked(Consumer<String> handler) {
        this.onSearchPicked = handler;
    }

    public void setOnFindNeighborsRequested(Consumer<Integer> handler) {
        this.onFindNeighborsRequested = handler;
    }

    public void setOnMetricSelected(Consumer<MetricType> handler) {
        this.onMetricSelected = handler;
    }

    // ---------- UiStateListener ----------

    @Override
    public void onSelectionChanged(String selectedKey) {
        this.selectedKey = selectedKey;

        if (selectedKey == null || selectedKey.isBlank()) {
            selectedLabel.setText("Selected: (none)");
            findBtn.setDisable(true);
            resultsList.getItems().setAll("Select an item, then click 'Find neighbors'.");
        } else {
            selectedLabel.setText("Selected: " + selectedKey);
            findBtn.setDisable(false);
        }
    }

    @Override
    public void onMetricChanged(DistanceStrategy metric) {
        // display sync only
        if (metric instanceof EuclideanDistance) euclideanBtn.setSelected(true);
        else cosineBtn.setSelected(true);
    }

    @Override
    public void onPrimaryResultsChanged(List<Neighbor> results) {
        resultsList.getItems().clear();

        if (results == null || results.isEmpty()) {
            resultsList.getItems().add("(no neighbors returned)");
            return;
        }

        for (Neighbor n : results) {
            resultsList.getItems().add(
                    n.getKey() + "  |  " + String.format(java.util.Locale.ROOT, "%.6f", n.getDistance())
            );
        }
    }

    @Override
    public void onHighlightsChanged(Set<String> highlightedKeys) {
        // NeighborsPane doesn't draw highlights
    }

    @Override
    public void onStatusChanged(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    @Override
    public void onErrorChanged(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    @Override
    public void onOperationChanged(OperationType type) {
        boolean visible = (type == OperationType.NEIGHBORS);
        root.setVisible(visible);
        root.setManaged(visible);
    }

    // ---------- Helpers ----------

    private void requestFindNeighbors() {
        if (onFindNeighborsRequested == null) return;

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
        if (onSearchPicked != null) onSearchPicked.accept(key);
    }

    private void fireMetricSelected(MetricType type) {
        if (onMetricSelected != null) onMetricSelected.accept(type);
    }
    public void onProjectionResultChanged(CustomProjectionResult res){}
}
