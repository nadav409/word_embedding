import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GroupingPane implements UiStateListener {

    private final VBox root = new VBox(10);

    private final TextField inputField = new TextField();
    private final Button addBtn = new Button("Add");

    private final Button removeBtn = new Button("Remove Selected");
    private final Button removeAllBtn = new Button("Remove All"); // 🔥 חדש

    private final ListView<String> groupList = new ListView<>();
    private final ObservableList<String> groupItems =
            FXCollections.observableArrayList();

    private final TextField kField = new TextField("10");
    private final Button computeBtn = new Button("Compute");

    private final ListView<String> resultsList = new ListView<>();

    private final Label statusLabel = new Label();
    private final Label errorLabel = new Label();

    private BiConsumer<List<String>, Integer> groupingHandler;

    public GroupingPane(Consumer<TextField> installAutocomplete) {

        root.setPadding(new Insets(8));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8;");

        inputField.setPromptText("Add item...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        if (installAutocomplete != null) {
            installAutocomplete.accept(inputField);
        }

        addBtn.setOnAction(e -> addFromInput());
        inputField.setOnAction(e -> addFromInput());

        removeBtn.setOnAction(e -> {
            String selected = groupList.getSelectionModel().getSelectedItem();
            if (selected != null) groupItems.remove(selected);
        });

        // 🔥 Remove All
        removeAllBtn.setOnAction(e -> {
            groupItems.clear();
            resultsList.getItems().clear();
        });

        groupList.setItems(groupItems);
        groupList.setPrefHeight(120);

        computeBtn.setOnAction(e -> fireCompute());

        resultsList.setPrefHeight(140);

        HBox addRow = new HBox(8, inputField, addBtn);
        HBox removeRow = new HBox(8, removeBtn, removeAllBtn); // 🔥 שורה חדשה
        HBox kRow = new HBox(8, new Label("K:"), kField);

        root.getChildren().addAll(
                new Label("Group:"),
                addRow,
                groupList,
                removeRow,   // 🔥 פה זה נכנס
                kRow,
                computeBtn,
                new Label("Closest to centroid:"),
                resultsList,
                statusLabel,
                errorLabel
        );
    }

    public Node getNode() {
        return root;
    }

    public void setOnGroupingRequested(BiConsumer<List<String>, Integer> handler) {
        this.groupingHandler = handler;
    }

    private void addFromInput() {

        String text = inputField.getText();
        if (text == null || text.isBlank()) return;

        text = text.trim();

        if (!groupItems.contains(text)) {
            groupItems.add(text);
        }

        inputField.clear();
    }

    private void fireCompute() {

        if (groupingHandler == null) return;
        if (groupItems.size() < 2) return;

        int k;
        try {
            k = Integer.parseInt(kField.getText().trim());
        } catch (Exception ex) {
            k = 10;
            kField.setText("10");
        }

        groupingHandler.accept(List.copyOf(groupItems), k);
    }

    @Override
    public void onSelectionChanged(String key) {
        if (key == null || key.isBlank()) return;

        if (!groupItems.contains(key)) {
            groupItems.add(key);
        }
    }

    @Override
    public void onPrimaryResultsChanged(List<Neighbor> results) {

        resultsList.getItems().clear();

        if (results == null || results.isEmpty()) return;

        for (Neighbor n : results) {
            resultsList.getItems().add(
                    n.getKey() + "  |  " + String.format("%.4f", n.getDistance())
            );
        }
    }

    @Override public void onHighlightsChanged(Set<String> highlightedKeys) {}

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
        boolean visible = (type == OperationType.GROUPING);
        root.setVisible(visible);
        root.setManaged(visible);
    }

    @Override public void onMetricChanged(DistanceStrategy metric) {}
    @Override public void onProjectionResultChanged(CustomProjectionResult res) {}
}
