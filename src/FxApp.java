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

    // Core
    private AppController controller;
    private UiState uiState;
    private AppPresenter presenter;

    // View parts
    private PlotPane plotPane;
    private NeighborsPane neighborsPane;

    // Top controls (axes)
    private ComboBox<Integer> xBox;
    private ComboBox<Integer> yBox;

    // ===== Shared Autocomplete (UI-only) =====
    private Popup suggestPopup;
    private ListView<String> suggestList;

    private List<String> allKeysSorted = new ArrayList<>();
    private boolean indexReady = false;

    private TextField activeSuggestField = null;
    private Consumer<String> activePickHandler = null;

    private static final int SUGGEST_MAX = 4;

    // prevent popup on programmatic setText
    private boolean programmaticUpdate = false;

    @Override
    public void start(Stage stage) {

        // ---- bootstrap + controller ----
        try {
            Provider provider = Bootstrap.buildProvider();
            controller = new AppController(provider);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // ---- UiState ----
        uiState = new UiState();

        // ---- Plot (View + Adapter) ----
        PlotView plotView = new PcaPlotView2D();
        plotPane = new PlotPane(plotView);

        // ---- Autocomplete popup init ----
        initSharedAutocompletePopup(stage);

        // ---- Neighbors pane ----
        neighborsPane = new NeighborsPane(field ->
                installAutocomplete(stage, field, picked -> {
                    if (picked != null && !picked.isBlank()) {
                        presenter.onItemSelected(picked);
                    }
                })
        );

        // ---- Connect UiState observers (MUST be before presenter init) ----
        uiState.addListener(plotPane);
        uiState.addListener(neighborsPane);

        // ---- Presenter (AFTER listeners) ----
        presenter = new AppPresenter(controller, uiState);

        // ---- Wire UI events -> Presenter ----
        plotPane.setOnItemClicked(presenter::onItemSelected);

        neighborsPane.setOnSearchPicked(presenter::onItemSelected);
        neighborsPane.setOnMetricSelected(presenter::onMetricSelected);
        neighborsPane.setOnFindNeighborsRequested(presenter::onFindNeighborsRequested);

        // ---- Top controls (axes) ----
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

        // ---- Layout ----
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(plotPane.getNode());

        VBox right = new VBox(12, neighborsPane.getNode());
        right.setPadding(new Insets(12));
        right.setPrefWidth(380);
        root.setRight(right);

        Scene scene = new Scene(root, 1250, 760);
        stage.setTitle("PCA Plot");
        stage.setScene(scene);
        stage.show();

        // ---- initial draw ----
        redraw();
    }

    // =========================================================
    // Redraw: only data fetch + setPoints + build autocomplete index
    // =========================================================

    private void redraw() {
        Integer x = xBox.getValue();
        Integer y = yBox.getValue();
        if (x == null || y == null) return;

        controller.setAxes2D(x, y);

        List<PlotPoint> pts = controller.getAllPcaPoints2D();

        plotPane.setPoints(pts);
        buildKeyIndexOnce(pts);
    }

    private void buildKeyIndexOnce(List<PlotPoint> pts) {
        if (indexReady) return;

        allKeysSorted = pts.stream()
                .map(PlotPoint::getKey)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        indexReady = true;
    }

    // =========================================================
    // Shared Autocomplete
    // =========================================================

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
        for (String key : allKeysSorted) {
            if (key.toLowerCase(Locale.ROOT).startsWith(p)) {
                matches.add(key);
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

    public static void main(String[] args) {
        launch(args);
    }
}
