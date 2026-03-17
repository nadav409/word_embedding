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
import java.util.function.Consumer;

public class CustomProjectionPane {

    private final VBox root = new VBox(10);

    private final Label titleLabel = new Label("Custom Projection");

    private final TextField aField = new TextField();
    private final TextField bField = new TextField();
    private final TextField kField = new TextField("10");

    private final Button projectBtn = new Button("Project");

    private final ListView<String> aList = new ListView<>();
    private final ListView<String> bList = new ListView<>();

    private final Label statusLabel = new Label("");
    private final Label errorLabel = new Label("");

    private TriConsumer<String, String, Integer> onProjectRequested;

    private enum TargetField {
        A, B
    }

    private TargetField activeTarget = TargetField.A;

    public CustomProjectionPane(Consumer<TextField> installAutocomplete) {

        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        statusLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b00020;");

        aField.setPromptText("word A…");
        bField.setPromptText("word B…");
        kField.setPrefColumnCount(5);

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

        kField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                fireProjectRequested();
                e.consume();
            }
        });

        aList.setPrefHeight(260);
        bList.setPrefHeight(260);

        HBox inputRow = new HBox(
                8,
                new Label("A:"), aField,
                new Label("→"),
                new Label("B:"), bField
        );
        inputRow.setStyle("-fx-alignment: center-left;");

        HBox actionRow = new HBox(
                8,
                new Label("K:"),
                kField,
                projectBtn
        );
        actionRow.setStyle("-fx-alignment: center-left;");

        HBox listsRow = new HBox(
                12,
                buildListBox("Most A-like:", aList),
                buildListBox("Most B-like:", bList)
        );

        root.getChildren().addAll(
                titleLabel,
                inputRow,
                actionRow,
                new Separator(),
                listsRow,
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

    public void setOnProjectRequested(TriConsumer<String, String, Integer> handler) {
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
            aList.getItems().setAll("(no results)");
            bList.getItems().setAll("(no results)");
            return;
        }

        titleLabel.setText("Custom Projection (" + result.getA() + " → " + result.getB() + ")");
        aList.getItems().setAll(formatItems(result.getTopA()));
        bList.getItems().setAll(formatItems(result.getTopB()));
    }

    public void clearResult() {
        titleLabel.setText("Custom Projection");
        aList.getItems().clear();
        bList.getItems().clear();
    }

    public void resetPane() {
        aField.clear();
        bField.clear();
        kField.setText("10");
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

        int k = parseKOrDefault();
        onProjectRequested.accept(a, b, k);
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

    private VBox buildListBox(String labelText, ListView<String> listView) {
        Label label = new Label(labelText);
        VBox box = new VBox(6, label, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return box;
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

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}