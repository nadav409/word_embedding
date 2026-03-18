package ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.MetricType;
import model.Neighbor;
import operations.CombineOp;
import operations.DistanceStrategy;
import operations.EuclideanDistance;
import operations.VectorExpression;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VectorArithmeticPane {

    private final VBox root = new VBox(10);

    private final TextField searchField = new TextField();
    private final Button plusBtn = new Button("+");
    private final Button minusBtn = new Button("-");
    private final Button clearBtn = new Button("Clear");
    private final Button resultBtn = new Button("RESULT");

    private final TextField kField = new TextField("1");

    private final ListView<String> exprList = new ListView<>();
    private final ListView<String> resultsList = new ListView<>();

    private final RadioButton cosineBtn = new RadioButton("Cosine");
    private final RadioButton euclidBtn = new RadioButton("Euclidean");
    private final ToggleGroup metricGroup = new ToggleGroup();

    private final VectorExpression expr = new VectorExpression();

    private Consumer<MetricType> onMetricSelected;
    private BiConsumer<VectorExpression, Integer> onResultRequested;

    public VectorArithmeticPane(Consumer<TextField> autocompleteInstaller) {

        Label title = new Label("model.Vector Arithmetic");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        searchField.setPromptText("Type a word…");
        if (autocompleteInstaller != null) {
            autocompleteInstaller.accept(searchField);
        }

        plusBtn.setPrefWidth(44);
        minusBtn.setPrefWidth(44);

        plusBtn.setOnAction(e -> addStep(CombineOp.PLUS));
        minusBtn.setOnAction(e -> addStep(CombineOp.MINUS));
        clearBtn.setOnAction(e -> resetPane());

        resultBtn.setOnAction(e -> {
            if (expr.isEmpty()) {
                return;
            }

            int k = parseKOrDefault();

            if (onResultRequested != null) {
                onResultRequested.accept(expr, k);
            }
        });

        cosineBtn.setToggleGroup(metricGroup);
        euclidBtn.setToggleGroup(metricGroup);
        cosineBtn.setSelected(true);

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

        HBox buildRow = new HBox(8, searchField, plusBtn, minusBtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox actionRow = new HBox(8, new Label("K:"), kField, resultBtn, clearBtn);
        actionRow.setStyle("-fx-alignment: center-left;");
        kField.setPrefColumnCount(5);

        HBox metricRow = new HBox(10, new Label("Distance:"), cosineBtn, euclidBtn);
        metricRow.setStyle("-fx-alignment: center-left;");

        exprList.setPrefHeight(120);
        resultsList.setPrefHeight(220);

        root.getChildren().addAll(title, buildRow, actionRow, new Label("Expression:"), exprList, metricRow, new Separator(), new Label("Results:"), resultsList);

        root.setPadding(new Insets(10));
        root.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");
    }

    public Node getNode() {
        return root;
    }

    public void setOnMetricSelected(Consumer<MetricType> handler) {
        this.onMetricSelected = handler;
    }

    public void setOnResultRequested(BiConsumer<VectorExpression, Integer> handler) {
        this.onResultRequested = handler;
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
            resultsList.getItems().clear();
            return;
        }

        resultsList.getItems().setAll(
                results.stream()
                        .map(n -> n.getKey() + "  (" + formatDistance(n.getDistance()) + ")")
                        .collect(Collectors.toList())
        );
    }

    public void clearResults() {
        resultsList.getItems().clear();
    }

    public void resetPane() {
        searchField.clear();
        expr.clear();
        refreshExpressionList();
        clearResults();
        kField.setText("1");
    }

    private void addStep(CombineOp op) {
        String key = searchField.getText();

        if (key == null) {
            return;
        }

        key = key.trim();

        if (key.isBlank()) {
            return;
        }

        expr.add(op, key);
        searchField.clear();
        refreshExpressionList();
    }

    private void refreshExpressionList() {
        exprList.getItems().setAll(
                expr.getSteps().stream()
                        .map(step -> {
                            if (step.getOp() == CombineOp.PLUS) {
                                return "+ " + step.getKey();
                            } else {
                                return "- " + step.getKey();
                            }
                        })
                        .collect(Collectors.toList())
        );
    }

    private int parseKOrDefault() {
        int k;

        try {
            k = Integer.parseInt(kField.getText().trim());
        } catch (Exception ex) {
            k = 1;
            kField.setText("1");
        }

        if (k < 1) {
            k = 1;
            kField.setText("1");
        }

        return k;
    }

    private static String formatDistance(double value) {
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private void fireMetricSelected(MetricType type) {
        if (onMetricSelected != null) {
            onMetricSelected.accept(type);
        }
    }
}