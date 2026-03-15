import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VectorArithmeticPane {

    private final VBox root = new VBox(10);

    private final TextField searchField = new TextField();
    private final Button plusBtn = new Button("+");
    private final Button minusBtn = new Button("-");
    private final Button clearBtn = new Button("Clear");
    private final Button resultBtn = new Button("RESULT");

    private final TextField kField = new TextField("10");

    private final ListView<String> exprList = new ListView<>();

    private final RadioButton cosineBtn = new RadioButton("Cosine");
    private final RadioButton euclidBtn = new RadioButton("Euclidean");
    private final ToggleGroup metricGroup = new ToggleGroup();

    private final ListView<String> resultsList = new ListView<>();

    private final VectorExpression expr = new VectorExpression();

    private Consumer<MetricType> onMetricSelected;
    private java.util.function.BiConsumer<VectorExpression, Integer> onResultRequested;

    public VectorArithmeticPane(Consumer<TextField> autocompleteInstaller) {

        Label title = new Label("Vector Arithmetic");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        searchField.setPromptText("Type a word…");

        if (autocompleteInstaller != null) {
            autocompleteInstaller.accept(searchField);
        }

        plusBtn.setPrefWidth(44);
        minusBtn.setPrefWidth(44);

        plusBtn.setOnAction(e -> addStep(CombineOp.PLUS));
        minusBtn.setOnAction(e -> addStep(CombineOp.MINUS));

        clearBtn.setOnAction(e -> {
            expr.clear();
            refreshExprList();
            clearResults();
        });

        resultBtn.setOnAction(e -> {
            if (expr.isEmpty()) {
                return;
            }

            int k = parseKOrDefault();
            if (onResultRequested != null) {
                onResultRequested.accept(expr, k);
            }
        });

        HBox buildRow = new HBox(8, searchField, plusBtn, minusBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        cosineBtn.setToggleGroup(metricGroup);
        euclidBtn.setToggleGroup(metricGroup);
        cosineBtn.setSelected(true);

        metricGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                return;
            }

            if (newT == cosineBtn) {
                fireMetricSelected(MetricType.COSINE);
            } else if (newT == euclidBtn) {
                fireMetricSelected(MetricType.EUCLIDEAN);
            }
        });

        HBox metricRow = new HBox(10, new Label("Distance:"), cosineBtn, euclidBtn);
        metricRow.setStyle("-fx-alignment: center-left;");

        exprList.setPrefHeight(120);
        resultsList.setPrefHeight(220);

        kField.setPrefColumnCount(5);
        HBox actionsRow = new HBox(8, new Label("K:"), kField, resultBtn, clearBtn);
        actionsRow.setStyle("-fx-alignment: center-left;");

        root.getChildren().addAll(
                title,
                buildRow,
                actionsRow,
                new Label("Expression:"),
                exprList,
                metricRow,
                new Separator(),
                new Label("Results:"),
                resultsList
        );

        root.setPadding(new Insets(10));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");
    }

    public Node getNode() {
        return root;
    }

    public void setOnMetricSelected(Consumer<MetricType> cb) {
        this.onMetricSelected = cb;
    }

    public void setOnResultRequested(java.util.function.BiConsumer<VectorExpression, Integer> cb) {
        this.onResultRequested = cb;
    }

    public void setVisiblePane(boolean visible) {
        root.setVisible(visible);
        root.setManaged(visible);
    }

    public void acceptSelectedKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        searchField.setText(key);
        searchField.positionCaret(searchField.getText().length());
    }

    public void setMetric(DistanceStrategy metric) {
        if (metric instanceof EuclideanDistance) {
            euclidBtn.setSelected(true);
        } else {
            cosineBtn.setSelected(true);
        }
    }

    public void showResults(List<Neighbor> results) {
        if (results == null) {
            results = List.of();
        }

        resultsList.getItems().setAll(
                results.stream()
                        .map(n -> n.getKey() + "  (" + fmt(n.getDistance()) + ")")
                        .collect(Collectors.toList())
        );
    }

    public void clearResults() {
        resultsList.getItems().clear();
    }

    private void addStep(CombineOp op) {
        String key = (searchField.getText() == null) ? "" : searchField.getText().trim();
        if (key.isBlank()) {
            return;
        }

        expr.add(op, key);
        searchField.clear();
        refreshExprList();
    }

    private void refreshExprList() {
        exprList.getItems().setAll(
                expr.getSteps().stream()
                        .map(s -> (s.getOp() == CombineOp.PLUS ? "+ " : "- ") + s.getKey())
                        .collect(Collectors.toList())
        );
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

    private static String fmt(double x) {
        return String.format(java.util.Locale.ROOT, "%.4f", x);
    }

    private void fireMetricSelected(MetricType type) {
        if (onMetricSelected != null) {
            onMetricSelected.accept(type);
        }
    }
}