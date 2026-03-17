import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GroupingPane {

    private final VBox root = new VBox(10);

    private final TextField inputField = new TextField();
    private final Button addBtn = new Button("Add");

    private final Button removeBtn = new Button("Remove Selected");
    private final Button removeAllBtn = new Button("Remove All");

    private final ObservableList<String> groupItems = FXCollections.observableArrayList();
    private final ListView<String> groupList = new ListView<>(groupItems);

    private final TextField kField = new TextField("10");
    private final Button computeBtn = new Button("Compute");

    private final ListView<String> resultsList = new ListView<>();

    private final Label statusLabel = new Label("");
    private final Label errorLabel = new Label("");

    private BiConsumer<List<String>, Integer> onGroupingRequested;

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

        removeBtn.setOnAction(e -> removeSelected());
        removeAllBtn.setOnAction(e -> resetPane());
        computeBtn.setOnAction(e -> fireCompute());

        groupList.setPrefHeight(120);
        resultsList.setPrefHeight(140);

        HBox addRow = new HBox(8, inputField, addBtn);
        HBox removeRow = new HBox(8, removeBtn, removeAllBtn);
        HBox kRow = new HBox(8, new Label("K:"), kField);

        root.getChildren().addAll(new Label("Group:"), addRow, groupList, removeRow, kRow, computeBtn, new Label("Closest to centroid:"), resultsList, statusLabel, errorLabel);
    }

    public Node getNode() {
        return root;
    }

    public void setOnGroupingRequested(BiConsumer<List<String>, Integer> handler) {
        this.onGroupingRequested = handler;
    }

    public void setVisiblePane(boolean visible) {
        root.setVisible(visible);
        root.setManaged(visible);
    }

    public void acceptSelectedKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        if (!groupItems.contains(key)) {
            groupItems.add(key);
        }
    }

    public List<String> getGroupItems() {
        return List.copyOf(groupItems);
    }

    public void showResults(List<Neighbor> results) {
        resultsList.getItems().clear();

        if (results == null || results.isEmpty()) {
            return;
        }

        for (Neighbor neighbor : results) {
            resultsList.getItems().add(
                    neighbor.getKey() + "  |  " +
                            String.format(java.util.Locale.ROOT, "%.4f", neighbor.getDistance())
            );
        }
    }

    public void clearResults() {
        resultsList.getItems().clear();
    }

    public void resetPane() {
        inputField.clear();
        groupItems.clear();
        kField.setText("10");
        clearResults();
        setStatus("");
        setError("");
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    public void setError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    private void addFromInput() {
        String text = inputField.getText();

        if (text == null) {
            return;
        }

        text = text.trim();

        if (text.isBlank()) {
            return;
        }

        if (!groupItems.contains(text)) {
            groupItems.add(text);
        }

        inputField.clear();
    }

    private void removeSelected() {
        String selected = groupList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            groupItems.remove(selected);
        }
    }

    private void fireCompute() {
        if (onGroupingRequested == null) {
            return;
        }

        if (groupItems.size() < 2) {
            return;
        }

        int k = parseKOrDefault();
        onGroupingRequested.accept(List.copyOf(groupItems), k);
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
}