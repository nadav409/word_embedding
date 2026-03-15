import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class CustomProjectionPane {

    private final VBox root = new VBox(10);

    private final Label title = new Label("Custom Projection");

    private final Label aLabel = new Label("A:");
    private final TextField aField = new TextField();

    private final Label bLabel = new Label("B:");
    private final TextField bField = new TextField();

    private final TextField kField = new TextField("10");
    private final Button projectBtn = new Button("Project");

    private final ListView<String> aList = new ListView<>();
    private final ListView<String> bList = new ListView<>();

    private final Label statusLabel = new Label("");
    private final Label errorLabel = new Label("");

    private TriConsumer<String, String, Integer> onProjectRequested;

    private enum TargetField { A, B }
    private TargetField activeTarget = TargetField.A;

    public CustomProjectionPane(Consumer<TextField> installAutocomplete) {

        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        aField.setPromptText("word A…");
        bField.setPromptText("word B…");

        if (installAutocomplete != null) {
            installAutocomplete.accept(aField);
            installAutocomplete.accept(bField);
        }

        aField.focusedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                activeTarget = TargetField.A;
            }
        });

        bField.focusedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                activeTarget = TargetField.B;
            }
        });

        aField.setPrefWidth(120);
        bField.setPrefWidth(120);

        HBox abRow = new HBox(
                8,
                aLabel, aField,
                new Label("→"),
                bLabel, bField
        );
        abRow.setStyle("-fx-alignment: center-left;");

        HBox.setHgrow(aField, Priority.NEVER);
        HBox.setHgrow(bField, Priority.NEVER);

        kField.setPrefColumnCount(5);
        HBox actionRow = new HBox(
                8,
                new Label("K:"),
                kField,
                projectBtn
        );
        actionRow.setStyle("-fx-alignment: center-left;");

        aList.setPrefHeight(260);
        bList.setPrefHeight(260);

        HBox lists = new HBox(
                12,
                box("Most A-like:", aList),
                box("Most B-like:", bList)
        );

        statusLabel.setStyle("-fx-font-size: 12px;");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b00020;");

        root.getChildren().addAll(
                title,
                abRow,
                actionRow,
                new Separator(),
                lists,
                statusLabel,
                errorLabel
        );

        root.setPadding(new Insets(10));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");

        projectBtn.setOnAction(e -> requestProject());

        aField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                requestProject();
                e.consume();
            }
        });

        bField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                requestProject();
                e.consume();
            }
        });

        kField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                requestProject();
                e.consume();
            }
        });

        updateButtonEnabled();

        aField.textProperty().addListener((o, ov, nv) -> updateButtonEnabled());
        bField.textProperty().addListener((o, ov, nv) -> updateButtonEnabled());
    }

    public Node getNode() {
        return root;
    }

    public void setOnProjectRequested(TriConsumer<String, String, Integer> cb) {
        this.onProjectRequested = cb;
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

    public void showResult(CustomProjectionResult res) {
        if (res == null) {
            title.setText("Custom Projection");
            aList.getItems().setAll("(no results)");
            bList.getItems().setAll("(no results)");
            return;
        }

        title.setText("Custom Projection (" + res.getA() + " → " + res.getB() + ")");
        aList.getItems().setAll(formatItems(res.getTopA()));
        bList.getItems().setAll(formatItems(res.getTopB()));
    }

    public void clearResult() {
        title.setText("Custom Projection");
        aList.getItems().clear();
        bList.getItems().clear();
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg == null ? "" : msg);
    }

    public void setError(String msg) {
        errorLabel.setText(msg == null ? "" : msg);
    }

    private void requestProject() {
        if (onProjectRequested == null) {
            return;
        }

        String a = aField.getText() == null ? "" : aField.getText().trim();
        String b = bField.getText() == null ? "" : bField.getText().trim();

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
        String a = aField.getText() == null ? "" : aField.getText().trim();
        String b = bField.getText() == null ? "" : bField.getText().trim();
        projectBtn.setDisable(a.isBlank() || b.isBlank());
    }

    private VBox box(String label, ListView<String> list) {
        Label l = new Label(label);
        VBox v = new VBox(6, l, list);
        VBox.setVgrow(list, Priority.ALWAYS);
        return v;
    }

    private List<String> formatItems(List<CustomProjectionItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of("(no results)");
        }

        return items.stream()
                .map(it -> it.getKey() + "  |  " +
                        String.format(java.util.Locale.ROOT, "%.6f", it.getScore()))
                .toList();
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}