import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CustomProjectionPane {

    private final VBox root = new VBox(10);

    private final Label titleLabel = new Label("Custom Projection");

    private final TextField aField = new TextField();
    private final TextField bField = new TextField();

    private final Button projectBtn = new Button("Project");

    private final ListView<String> resultsList = new ListView<>();

    private final Label statusLabel = new Label("");
    private final Label errorLabel = new Label("");

    private BiConsumer<String, String> onProjectRequested;

    private enum TargetField {
        A, B
    }

    private TargetField activeTarget = TargetField.A;

    public CustomProjectionPane(Consumer<TextField> installAutocomplete) {

        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        statusLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b00020;");

        aField.setPromptText("word A...");
        bField.setPromptText("word B...");

        if (installAutocomplete != null) {
            installAutocomplete.accept(aField);
            installAutocomplete.accept(bField);
        }

        aField.focusedProperty().addListener((obs, oldValue, isNowFocused) -> {
            if (isNowFocused) {
                activeTarget = TargetField.A;
            }
        });

        bField.focusedProperty().addListener((obs, oldValue, isNowFocused) -> {
            if (isNowFocused) {
                activeTarget = TargetField.B;
            }
        });

        aField.textProperty().addListener((obs, oldValue, newValue) -> updateButtonEnabled());
        bField.textProperty().addListener((obs, oldValue, newValue) -> updateButtonEnabled());

        projectBtn.setOnAction(e -> fireProjectRequested());
        projectBtn.setPrefWidth(90);
        projectBtn.setMinWidth(90);

        aField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                fireProjectRequested();
                e.consume();
            }
        });

        bField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                fireProjectRequested();
                e.consume();
            }
        });

        resultsList.setPrefHeight(400);

        HBox inputRow = new HBox(
                8,
                new Label("A:"), aField,
                new Label("→"),
                new Label("B:"), bField,
                projectBtn
        );
        inputRow.setStyle("-fx-alignment: center-left;");

        HBox.setHgrow(aField, Priority.ALWAYS);
        HBox.setHgrow(bField, Priority.ALWAYS);

        root.getChildren().addAll(
                titleLabel,
                inputRow,
                new Separator(),
                new Label("Projection on axis:"),
                resultsList,
                statusLabel,
                errorLabel
        );

        root.setPadding(new Insets(10));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");

        updateButtonEnabled();
    }

    public Node getNode() {
        return root;
    }

    public void setOnProjectRequested(BiConsumer<String, String> handler) {
        this.onProjectRequested = handler;
    }

    public void setVisiblePane(boolean visible) {
        root.setVisible(visible);
        root.setManaged(visible);
    }

    public void acceptSelectedKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        if (activeTarget == TargetField.B) {
            bField.setText(key);
            bField.positionCaret(bField.getText().length());
        } else {
            aField.setText(key);
            aField.positionCaret(aField.getText().length());
        }

        updateButtonEnabled();
    }

    public void showResult(CustomProjectionResult result) {
        if (result == null) {
            titleLabel.setText("Custom Projection");
            resultsList.getItems().setAll("(no results)");
            return;
        }

        titleLabel.setText("Custom Projection (" + result.getA() + " → " + result.getB() + ")");
        resultsList.getItems().setAll(formatItems(result.getItems()));
    }

    public void clearResult() {
        titleLabel.setText("Custom Projection");
        resultsList.getItems().clear();
    }

    public void resetPane() {
        aField.clear();
        bField.clear();
        clearResult();
        setStatus("");
        setError("");
        activeTarget = TargetField.A;
        updateButtonEnabled();
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null ? "" : message);
    }

    public void setError(String message) {
        errorLabel.setText(message == null ? "" : message);
    }

    private void fireProjectRequested() {
        if (onProjectRequested == null) {
            return;
        }

        String a = aField.getText();
        String b = bField.getText();

        if (a == null) {
            a = "";
        }

        if (b == null) {
            b = "";
        }

        a = a.trim();
        b = b.trim();

        if (a.isBlank() || b.isBlank()) {
            return;
        }

        onProjectRequested.accept(a, b);
    }

    private void updateButtonEnabled() {
        String a = aField.getText();
        String b = bField.getText();

        if (a == null) {
            a = "";
        }

        if (b == null) {
            b = "";
        }

        a = a.trim();
        b = b.trim();

        projectBtn.setDisable(a.isBlank() || b.isBlank());
    }

    private List<String> formatItems(List<CustomProjectionItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of("(no results)");
        }

        return items.stream()
                .map(item -> item.getKey() + "  |  " +
                        String.format(java.util.Locale.ROOT, "%.6f", item.getScore()))
                .toList();
    }
}