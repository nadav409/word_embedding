import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FxApp extends Application {

    private AppController controller;

    // Plot
    private PlotView plotView;

    private ComboBox<Integer> xBox;
    private ComboBox<Integer> yBox;

    // ===== RIGHT UI: mode selector =====
    private ComboBox<String> modeBox;
    private VBox neighborsPanel;
    private VBox projectionPanel;
    private VBox vectorPanel;

    // ===== Neighbors UI =====
    private Label selectedWordLabel;
    private TextField kField;
    private Button neighborsBtn;
    private ListView<String> neighborsList;
    private String selectedWord = null;

    // ===== Vector Arithmetic UI =====
    private ListView<String> exprList;
    private Button plusBtn;
    private Button minusBtn;
    private Button undoBtn;
    private Button clearBtn;

    private TextField exprKField;
    private TextField exprSearchField;
    private Button solveExprBtn;
    private ListView<String> exprResultList;

    private final VectorExpression currentExpr = new VectorExpression();
    private CombineOp pendingOp = CombineOp.PLUS;

    // כדי לאפשר Undo בלי לשנות את VectorExpression
    private final Deque<ExpressionStep> exprStepsStack = new ArrayDeque<>();

    // ===== Search (Neighbors) =====
    private TextField searchField;

    // ===== Shared Autocomplete (for searchField + axis A/B) =====
    private Popup suggestPopup;
    private ListView<String> suggestList;
    private List<String> allWordsSorted = new ArrayList<>();
    private boolean indexReady = false;

    private TextField activeSuggestField = null;
    private Consumer<String> activePickHandler = null;

    private static final int SUGGEST_MAX = 4;

    // Prevent popup when we set text programmatically (e.g., clicking a point)
    private boolean programmaticUpdate = false;

    // ===== Highlight on plot =====
    private final LinkedHashSet<String> highlightedKeys = new LinkedHashSet<>();
    private boolean highlightOnPlot = false;

    // ===== Metric UI =====
    private RadioButton cosineBtn;
    private RadioButton euclideanBtn;

    // ===== Custom Projection UI =====
    private TextField axisAField;
    private TextField axisBField;
    private TextField axisKField;
    private Button axisBtn;

    private ListView<String> axisAList; // Most A-like
    private ListView<String> axisBList; // Most B-like

    @Override
    public void start(Stage stage) {
        try {
            Provider provider = Bootstrap.buildProvider();
            controller = new AppController(provider);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // PlotView
        plotView = new PcaPlotView2D();
        plotView.setOnWordClicked(this::selectWord);

        // Top controls
        xBox = new ComboBox<>();
        yBox = new ComboBox<>();
        Button redrawBtn = new Button("Redraw");

        int dim = controller.getPcaDimension();
        for (int i = 0; i < dim; i++) {
            xBox.getItems().add(i);
            yBox.getItems().add(i);
        }
        xBox.getSelectionModel().select(0);
        yBox.getSelectionModel().select(1);

        redrawBtn.setOnAction(e -> redraw());
        xBox.setOnAction(e -> redraw());
        yBox.setOnAction(e -> redraw());

        HBox top = new HBox(10,
                new Label("X:"), xBox,
                new Label("Y:"), yBox,
                redrawBtn
        );
        top.setStyle("-fx-padding: 10; -fx-alignment: center-left;");

        // Right panel
        VBox right = buildRightPanel(stage);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(plotView.getNode());
        root.setRight(right);

        Scene scene = new Scene(root, 1250, 760);
        stage.setTitle("PCA Plot");
        stage.setScene(scene);
        stage.show();

        redraw();
    }

    // ------------------ Right panel ------------------

    private VBox buildRightPanel(Stage stage) {

        initSharedAutocompletePopup(stage);

        selectedWordLabel = new Label("Selected: (none)");
        selectedWordLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll("Nearest neighbors", "Custom projection", "Vector arithmetic");
        modeBox.getSelectionModel().select(0);

        HBox modeRow = new HBox(8, new Label("Operation:"), modeBox);
        modeRow.setStyle("-fx-alignment: center-left;");

        neighborsPanel = buildNeighborsPanel(stage);
        projectionPanel = buildProjectionPanel(stage);
        vectorPanel = buildVectorArithmeticPanel(stage);

        // start with neighbors visible
        setPanelVisible(neighborsPanel, true);
        setPanelVisible(projectionPanel, false);
        setPanelVisible(vectorPanel, false);

        modeBox.setOnAction(e -> {
            int idx = modeBox.getSelectionModel().getSelectedIndex();
            setPanelVisible(neighborsPanel, idx == 0);
            setPanelVisible(projectionPanel, idx == 1);
            setPanelVisible(vectorPanel, idx == 2);
            hideSuggestions();
        });

        VBox box = new VBox(12,
                selectedWordLabel,
                modeRow,
                neighborsPanel,
                projectionPanel,
                vectorPanel
        );
        box.setPadding(new Insets(12));
        box.setPrefWidth(380);
        return box;
    }

    private void setPanelVisible(Region panel, boolean v) {
        panel.setVisible(v);
        panel.setManaged(v);
    }

    // ------------------ Neighbors panel ------------------

    private VBox buildNeighborsPanel(Stage stage) {

        searchField = new TextField();
        searchField.setPromptText("Search word…");

        installAutocomplete(stage, searchField, picked -> {
            if (picked != null && !picked.isBlank()) selectWord(picked);
        });

        HBox searchRow = new HBox(8, new Label("Search:"), searchField);
        searchRow.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        cosineBtn = new RadioButton("Cosine");
        euclideanBtn = new RadioButton("Euclidean");

        ToggleGroup metricGroup = new ToggleGroup();
        cosineBtn.setToggleGroup(metricGroup);
        euclideanBtn.setToggleGroup(metricGroup);

        DistanceStrategy cur = controller.getDistanceStrategy();
        if (cur instanceof EuclideanDistance) euclideanBtn.setSelected(true);
        else cosineBtn.setSelected(true);

        metricGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == cosineBtn) controller.setDistanceStrategy(new CosineDistance());
            else if (newT == euclideanBtn) controller.setDistanceStrategy(new EuclideanDistance());
        });

        HBox metricRow = new HBox(10, new Label("Distance:"), cosineBtn, euclideanBtn);
        metricRow.setStyle("-fx-alignment: center-left;");

        kField = new TextField("10");
        kField.setPrefColumnCount(5);

        neighborsBtn = new Button("Find neighbors");
        neighborsBtn.setDisable(true);
        neighborsBtn.setOnAction(e -> loadNeighbors());

        HBox actionRow = new HBox(8, new Label("K:"), kField, neighborsBtn);
        actionRow.setStyle("-fx-alignment: center-left;");

        neighborsList = new ListView<>();
        neighborsList.setPrefHeight(320);

        VBox panel = new VBox(10,
                searchRow,
                metricRow,
                actionRow,
                new Label("Nearest neighbors (word | distance):"),
                neighborsList
        );

        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");
        return panel;
    }

    // ------------------ Projection panel ------------------

    private VBox buildProjectionPanel(Stage stage) {

        Label title = new Label("Custom Projection (A → B)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        axisAField = new TextField();
        axisAField.setPromptText("Word A");
        installAutocomplete(stage, axisAField, picked -> {
            if (picked != null && !picked.isBlank()) axisAField.setText(picked);
        });

        axisBField = new TextField();
        axisBField.setPromptText("Word B");
        installAutocomplete(stage, axisBField, picked -> {
            if (picked != null && !picked.isBlank()) axisBField.setText(picked);
        });

        HBox wordsRow = new HBox(8,
                new Label("A:"), axisAField,
                new Label("B:"), axisBField
        );
        wordsRow.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(axisAField, Priority.ALWAYS);
        HBox.setHgrow(axisBField, Priority.ALWAYS);

        axisKField = new TextField("10");
        axisKField.setPrefColumnCount(4);

        axisBtn = new Button("Project");
        axisBtn.setOnAction(e -> runCustomProjection());

        HBox actionRow = new HBox(8, new Label("K:"), axisKField, axisBtn);
        actionRow.setStyle("-fx-alignment: center-left;");

        axisAList = new ListView<>();
        axisBList = new ListView<>();
        axisAList.setPrefHeight(200);
        axisBList.setPrefHeight(200);

        axisAList.setOnMouseClicked(e -> {
            String w = extractWord(axisAList.getSelectionModel().getSelectedItem());
            if (w != null) selectWord(w);
        });
        axisBList.setOnMouseClicked(e -> {
            String w = extractWord(axisBList.getSelectionModel().getSelectedItem());
            if (w != null) selectWord(w);
        });

        VBox left = new VBox(6, new Label("Most A-like:"), axisAList);
        VBox right = new VBox(6, new Label("Most B-like:"), axisBList);

        HBox listsRow = new HBox(10, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        VBox panel = new VBox(10, title, wordsRow, actionRow, listsRow);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");
        return panel;
    }

    private String extractWord(String row) {
        if (row == null) return null;
        int idx = row.indexOf('|');
        if (idx <= 0) return row.trim();
        return row.substring(0, idx).trim();
    }

    // ------------------ Vector Arithmetic panel ------------------

    private VBox buildVectorArithmeticPanel(Stage stage) {

        Label title = new Label("Vector Arithmetic (+ / -)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        exprList = new ListView<>();
        exprList.setPrefHeight(140);
        exprList.getItems().setAll("(choose + or - then click words on the plot)");

        plusBtn = new Button("+");
        minusBtn = new Button("-");
        undoBtn = new Button("Undo");
        clearBtn = new Button("Clear");

        plusBtn.setOnAction(e -> pendingOp = CombineOp.PLUS);
        minusBtn.setOnAction(e -> pendingOp = CombineOp.MINUS);

        undoBtn.setOnAction(e -> undoLastExprStep());
        clearBtn.setOnAction(e -> clearExpression());

        HBox opRow = new HBox(8, new Label("Next op:"), plusBtn, minusBtn, undoBtn, clearBtn);
        opRow.setStyle("-fx-alignment: center-left;");

        exprKField = new TextField("5");
        exprKField.setPrefColumnCount(4);

        solveExprBtn = new Button("Solve");
        solveExprBtn.setOnAction(e -> runVectorArithmetic());

        HBox actionRow = new HBox(8, new Label("Top K:"), exprKField, solveExprBtn);
        actionRow.setStyle("-fx-alignment: center-left;");

        // Search field (with same autocomplete as other panels)
        exprSearchField = new TextField();
        exprSearchField.setPromptText("Search word…");

        installAutocomplete(stage, exprSearchField, picked -> {
            if (picked != null && !picked.isBlank()) {
                addWordToExpression(picked);   // זה מוסיף לביטוי (+/-)
            }
        });

        HBox searchRow = new HBox(8, new Label("Search:"), exprSearchField);
        searchRow.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(exprSearchField, Priority.ALWAYS);


        exprResultList = new ListView<>();
        exprResultList.setPrefHeight(220);

        VBox panel = new VBox(10,
                title,
                searchRow,
                exprList,
                opRow,
                actionRow,
                new Label("Results (word | distance):"),
                exprResultList
        );


        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 8; -fx-background-radius: 8;");
        return panel;
    }

    private void addWordToExpression(String word) {
        if (word == null || word.isBlank()) return;

        // UI-stack ל-Undo
        ExpressionStep step = new ExpressionStep(pendingOp, word);
        exprStepsStack.addLast(step);

        // rebuild currentExpr (כי אין remove/clear בהכרח)
        rebuildExpressionFromStack();

        // update UI
        if (exprList != null) {
            if (exprList.getItems().size() == 1 && exprList.getItems().get(0).startsWith("(choose")) {
                exprList.getItems().clear();
            }
            String sign = (pendingOp == CombineOp.PLUS) ? "+" : "-";
            exprList.getItems().add(sign + " " + word);
        }
    }

    private void rebuildExpressionFromStack() {
        // מאפסים ובונים מחדש currentExpr בלי לשבור OOP:
        // אם אין clear() ב-VectorExpression שלך, פשוט ניצור אחד חדש דרך רפלקציה? לא.
        // לכן: נניח שה-VectorExpression שלך הוא בדיוק כמו שכתבת (עם steps פנימי),
        // והפתרון הכי נקי הוא להוסיף לו clear(). אם לא הוספת, אז תעשה "new VectorExpression" במקום final.
        //
        // ✅ פתרון מומלץ: שנה את currentExpr להיות לא-final ואז:
        // currentExpr = new VectorExpression();
        //
        // אבל כי אצלך זה final, נניח שהוספת clear() ל-VectorExpression.
        currentExpr.clear();

        for (ExpressionStep s : exprStepsStack) {
            currentExpr.add(s.getOp(), s.getKey());
        }
    }

    private void undoLastExprStep() {
        if (exprStepsStack.isEmpty()) return;

        exprStepsStack.removeLast();

        // rebuild expression
        rebuildExpressionFromStack();

        // update exprList
        if (exprList != null && !exprList.getItems().isEmpty()) {
            // אם זו הודעת placeholder – אין מה להסיר
            if (exprList.getItems().size() == 1 && exprList.getItems().get(0).startsWith("(choose")) return;

            exprList.getItems().remove(exprList.getItems().size() - 1);
            if (exprList.getItems().isEmpty()) {
                exprList.getItems().setAll("(choose + or - then click words on the plot)");
            }
        }

        // clear results (כי הביטוי השתנה)
        if (exprResultList != null) exprResultList.getItems().clear();
    }

    private void clearExpression() {
        exprStepsStack.clear();
        currentExpr.clear();

        if (exprList != null) exprList.getItems().setAll("(choose + or - then click words on the plot)");
        if (exprResultList != null) exprResultList.getItems().clear();
    }

    private void runVectorArithmetic() {
        int k;
        try {
            k = Integer.parseInt(exprKField.getText().trim());
        } catch (Exception ex) {
            k = 5;
            exprKField.setText("5");
        }
        if (k < 1) k = 1;

        exprResultList.getItems().clear();

        if (exprStepsStack.isEmpty()) {
            exprResultList.getItems().add("ERROR: expression is empty");
            return;
        }

        try {
            List<Neighbor> res = controller.vectorArithmetic(currentExpr, k);

            for (Neighbor n : res) {
                exprResultList.getItems().add(
                        n.getKey() + "  |  " + String.format("%.6f", n.getDistance())
                );
            }
            if (res.isEmpty()) exprResultList.getItems().add("(no results)");

        } catch (Exception ex) {
            exprResultList.getItems().add("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ------------------ Shared Autocomplete ------------------

    private void initSharedAutocompletePopup(Stage stage) {
        suggestList = new ListView<>();
        suggestList.setMaxHeight(28 * SUGGEST_MAX + 2);
        suggestList.setPrefHeight(28 * SUGGEST_MAX + 2);
        suggestList.setPrefWidth(220);
        suggestList.setFocusTraversable(false);

        suggestPopup = new Popup();
        suggestPopup.setAutoHide(true);
        suggestPopup.setHideOnEscape(true);

        suggestList.setStyle(
                "-fx-border-color: rgba(0,0,0,0.25);" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        suggestPopup.getContent().add(suggestList);

        suggestList.setOnMouseClicked(e -> pickFromSuggestions());

        suggestList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                pickFromSuggestions();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                hideSuggestions();
                if (activeSuggestField != null) activeSuggestField.requestFocus();
                e.consume();
            }
        });

        suggestPopup.setOnHidden(e -> suggestList.getItems().clear());
    }

    private void installAutocomplete(Stage stage, TextField field, Consumer<String> onPick) {

        field.textProperty().addListener((obs, oldV, newV) -> {
            if (programmaticUpdate) return;
            if (!field.isFocused()) return;

            activeSuggestField = field;
            activePickHandler = onPick;
            updateSuggestions(stage, field, newV);
        });

        field.focusedProperty().addListener((obs, was, isNow) -> {
            if (!isNow) hideSuggestions();
        });

        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                if (suggestPopup.isShowing() && !suggestList.getItems().isEmpty()) {
                    suggestList.requestFocus();
                    suggestList.getSelectionModel().select(0);
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                if (suggestPopup.isShowing() && !suggestList.getItems().isEmpty()) {
                    pickFromSuggestions();
                } else {
                    String raw = field.getText() == null ? "" : field.getText().trim();
                    if (!raw.isBlank() && onPick != null) onPick.accept(raw);
                }
                hideSuggestions();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                hideSuggestions();
                e.consume();
            }
        });
    }

    private void pickFromSuggestions() {
        if (!suggestPopup.isShowing()) return;
        String pick = suggestList.getSelectionModel().getSelectedItem();
        if (pick == null && !suggestList.getItems().isEmpty()) pick = suggestList.getItems().get(0);

        if (pick != null && activePickHandler != null) {
            activePickHandler.accept(pick);
        }
        hideSuggestions();
        if (activeSuggestField != null) activeSuggestField.requestFocus();
    }

    private void updateSuggestions(Stage stage, TextField field, String prefixRaw) {
        if (!indexReady) return;

        String prefix = (prefixRaw == null) ? "" : prefixRaw.trim();
        if (prefix.isEmpty()) {
            hideSuggestions();
            return;
        }

        String p = prefix.toLowerCase(Locale.ROOT);

        ArrayList<String> matches = new ArrayList<>(SUGGEST_MAX);
        for (String w : allWordsSorted) {
            if (w.toLowerCase(Locale.ROOT).startsWith(p)) {
                matches.add(w);
                if (matches.size() >= SUGGEST_MAX) break;
            }
        }

        if (matches.isEmpty()) {
            hideSuggestions();
            return;
        }

        suggestList.getItems().setAll(matches);
        suggestList.getSelectionModel().select(0);

        showSuggestionsUnderField(stage, field);
    }

    private void showSuggestionsUnderField(Stage stage, TextField field) {
        Point2D p = field.localToScreen(0, field.getHeight());
        if (p == null) return;

        suggestList.setPrefWidth(Math.max(220, field.getWidth()));
        suggestPopup.setX(p.getX());
        suggestPopup.setY(p.getY() + 2);

        if (!suggestPopup.isShowing()) suggestPopup.show(stage);
    }

    private void hideSuggestions() {
        if (suggestPopup != null) suggestPopup.hide();
    }

    // ------------------ Plot + Data ------------------

    private void redraw() {
        if (controller == null) return;

        Integer x = xBox.getValue();
        Integer y = yBox.getValue();
        if (x == null || y == null) return;

        controller.setAxes2D(x, y);

        List<PlotPoint> pts = controller.getAllPcaPoints2D();
        plotView.setPoints(pts);

        plotView.setSelectedKey(selectedWord);
        plotView.setHighlights(highlightOnPlot ? highlightedKeys : Collections.emptySet());

        Set<String> labels = new HashSet<>();
        if (selectedWord != null) labels.add(selectedWord);
        if (highlightOnPlot) labels.addAll(highlightedKeys);
        plotView.setLabels(labels);

        buildWordIndexOnce(pts);
    }

    private void buildWordIndexOnce(List<PlotPoint> pts) {
        if (indexReady) return;

        allWordsSorted = pts.stream()
                .map(PlotPoint::getKey)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        indexReady = true;
    }

    // ------------------ Actions ------------------

    private void selectWord(String word) {
        if (word == null || word.isBlank()) return;

        selectedWord = word;
        selectedWordLabel.setText("Selected: " + word);
        if (neighborsBtn != null) neighborsBtn.setDisable(false);

        // אם אנחנו במצב Vector arithmetic – מוסיפים לביטוי
        if (modeBox != null && modeBox.getSelectionModel().getSelectedIndex() == 2) {
            addWordToExpression(word);
        }

        highlightedKeys.clear();
        highlightOnPlot = false;

        if (searchField != null) {
            programmaticUpdate = true;
            try {
                if (!word.equals(searchField.getText())) {
                    searchField.setText(word);
                    searchField.positionCaret(word.length());
                }
            } finally {
                programmaticUpdate = false;
            }
        }
        hideSuggestions();

        if (neighborsList != null) {
            neighborsList.getItems().setAll("Click 'Find neighbors' to load…");
        }

        plotView.setSelectedKey(selectedWord);
        plotView.setHighlights(Collections.emptySet());
        plotView.setLabels(Set.of(selectedWord));
    }

    private void loadNeighbors() {
        if (selectedWord == null || selectedWord.isBlank()) return;

        int k;
        try {
            k = Integer.parseInt(kField.getText().trim());
        } catch (Exception ex) {
            k = 10;
            kField.setText("10");
        }
        if (k < 1) k = 1;

        neighborsList.getItems().clear();

        try {
            List<Neighbor> neighbors = controller.nearestNeighbors(selectedWord, k);

            highlightedKeys.clear();
            for (Neighbor n : neighbors) {
                String w = n.getKey();
                if (w != null && !w.equals(selectedWord)) highlightedKeys.add(w);
            }
            highlightOnPlot = true;

            for (Neighbor n : neighbors) {
                neighborsList.getItems().add(
                        n.getKey() + "  |  " + String.format("%.6f", n.getDistance())
                );
            }
            if (neighbors.isEmpty()) neighborsList.getItems().add("(no neighbors returned)");

            plotView.setSelectedKey(selectedWord);
            plotView.setHighlights(highlightedKeys);

            Set<String> labels = new HashSet<>();
            labels.add(selectedWord);
            labels.addAll(highlightedKeys);
            plotView.setLabels(labels);

        } catch (Exception ex) {
            neighborsList.getItems().add("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void runCustomProjection() {
        String a = axisAField.getText() == null ? "" : axisAField.getText().trim();
        String b = axisBField.getText() == null ? "" : axisBField.getText().trim();

        int k;
        try {
            k = Integer.parseInt(axisKField.getText().trim());
        } catch (Exception ex) {
            k = 10;
            axisKField.setText("10");
        }
        if (k < 1) k = 1;

        axisAList.getItems().clear();
        axisBList.getItems().clear();

        try {
            CustomProjectionResult res = controller.customProjection(a, b, k);

            for (CustomProjectionItem item : res.getTopA()) {
                axisAList.getItems().add(item.getKey() + "  |  " + String.format("%.6f", item.getScore()));
            }
            for (CustomProjectionItem item : res.getTopB()) {
                axisBList.getItems().add(item.getKey() + "  |  " + String.format("%.6f", item.getScore()));
            }

            if (res.getTopA().isEmpty()) axisAList.getItems().add("(no results)");
            if (res.getTopB().isEmpty()) axisBList.getItems().add("(no results)");

        } catch (Exception ex) {
            axisAList.getItems().add("ERROR: " + ex.getMessage());
            axisBList.getItems().add("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
