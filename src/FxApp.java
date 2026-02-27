import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class FxApp extends Application {

    private AppController controller;
    private UiState uiState;
    private AppPresenter presenter;

    private PlotPane plotPane;

    private PlotView plotView2D;
    private PlotView plotView3D;

    private NeighborsPane neighborsPane;
    private VectorArithmeticPane vectorPane;
    private CustomProjectionPane projectionPane;
    private DistancePane distancePane;
    private GroupingPane groupingPane;

    private ComboBox<Integer> xBox;
    private ComboBox<Integer> yBox;
    private ComboBox<Integer> zBox;
    private CheckBox threeDCheck;
    private ComboBox<OperationType> opBox;

    private List<String> allKeys;

    @Override
    public void start(Stage stage) {

        try {
            Provider provider = Bootstrap.buildProvider();
            controller = new AppController(provider);

            allKeys = provider.getSpace(SpaceId.FULL)
                    .getAll()
                    .stream()
                    .map(Embedding::getKey)
                    .sorted()
                    .toList();

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        uiState = new UiState();
        presenter = new AppPresenter(controller, uiState);

        // =========================
        // Create views ONCE
        // =========================

        plotView2D = new PcaPlotView2D();
        plotView3D = new PcaPlotView3D();

        plotPane = new PlotPane(plotView2D, plotView3D);


        uiState.addListener(plotPane);

        plotPane.setOnItemClicked(presenter::onItemSelected);

        // =========================
        // Side panes
        // =========================

        Consumer<TextField> installer = this::installAutocomplete;

        neighborsPane = new NeighborsPane(installer);
        vectorPane = new VectorArithmeticPane(installer);
        projectionPane = new CustomProjectionPane(installer);
        distancePane = new DistancePane(installer);
        groupingPane = new GroupingPane(installer);

        uiState.addListener(neighborsPane);
        uiState.addListener(vectorPane);
        uiState.addListener(projectionPane);
        uiState.addListener(distancePane);
        uiState.addListener(groupingPane);

        neighborsPane.setOnSearchPicked(presenter::onItemSelected);
        neighborsPane.setOnMetricSelected(presenter::onMetricSelected);
        neighborsPane.setOnFindNeighborsRequested(presenter::onFindNeighborsRequested);

        vectorPane.setOnMetricSelected(presenter::onMetricSelected);
        vectorPane.setOnResultRequested(presenter::onVectorResultRequested);

        projectionPane.setOnProjectRequested(presenter::onProjectionRequested);

        distancePane.setOnMetricSelected(presenter::onMetricSelected);
        distancePane.setOnDistanceRequested(presenter::onDistanceRequested);

        groupingPane.setOnGroupingRequested(presenter::onGroupingRequested);

        // =========================
        // Operation selector
        // =========================

        opBox = new ComboBox<>();
        opBox.getItems().addAll(OperationType.values());
        opBox.getSelectionModel().select(OperationType.NEIGHBORS);
        opBox.valueProperty().addListener((obs, o, n) -> {
            if (n != null) presenter.onOperationSelected(n);
        });

        uiState.setSelectedOperation(OperationType.NEIGHBORS);

        HBox opRow = new HBox(10, new Label("Operation:"), opBox);
        opRow.setPadding(new Insets(5));

        // =========================
        // Axis controls
        // =========================

        xBox = new ComboBox<>();
        yBox = new ComboBox<>();
        zBox = new ComboBox<>();
        threeDCheck = new CheckBox("3D");

        int dim = controller.getPcaDimension();

        for (int i = 0; i < dim; i++) {
            xBox.getItems().add(i);
            yBox.getItems().add(i);
            zBox.getItems().add(i);
        }

        xBox.getSelectionModel().select(0);
        yBox.getSelectionModel().select(1);
        zBox.getSelectionModel().select(2);

        zBox.setDisable(true);

        xBox.setOnAction(e -> redraw());
        yBox.setOnAction(e -> redraw());
        zBox.setOnAction(e -> redraw());

        threeDCheck.setOnAction(e -> {
            boolean is3D = threeDCheck.isSelected();
            zBox.setDisable(!is3D);
            redraw();
        });

        HBox top = new HBox(10, new Label("X:"), xBox, new Label("Y:"), yBox, new Label("Z:"), zBox, threeDCheck);

        top.setPadding(new Insets(10));

        // =========================
        // Layout
        // =========================

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(plotPane);


        VBox right = new VBox(10,
                opRow,
                neighborsPane.getNode(),
                vectorPane.getNode(),
                projectionPane.getNode(),
                distancePane.getNode(),
                groupingPane.getNode()
        );

        right.setPadding(new Insets(10));
        right.setPrefWidth(380);

        root.setRight(right);

        Scene scene = new Scene(root, 1250, 760);


        scene.getStylesheets().add(
                getClass().getResource("dark-theme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("EMBEDDING PROJECTOR");
        stage.show();


        redraw();
    }

    // =========================
    // REDRAW
    // =========================

    private void redraw() {

        Integer x = xBox.getValue();
        Integer y = yBox.getValue();
        if (x == null || y == null) return;

        boolean is3D = threeDCheck.isSelected();
        plotPane.setMode(is3D);

        if (is3D) {

            Integer z = zBox.getValue();
            if (z == null) return;

            controller.setAxes3D(x, y, z);
            plotPane.setPoints(controller.getAllPcaPoints3D());

        } else {

            controller.setAxes2D(x, y);
            plotPane.setPoints(controller.getAllPcaPoints2D());
        }
    }


    // =========================
    // Autocomplete
    // =========================

    private void installAutocomplete(TextField field) {

        ContextMenu popup = new ContextMenu();

        field.textProperty().addListener((obs, oldText, newText) -> {

            if (!field.isFocused()) {
                popup.hide();
                return;
            }

            if (newText == null || newText.isBlank()) {
                popup.hide();
                return;
            }

            var matches = allKeys.stream()
                    .filter(k -> k.startsWith(newText))
                    .limit(6)
                    .toList();

            if (matches.isEmpty()) {
                popup.hide();
                return;
            }

            popup.getItems().clear();

            for (String m : matches) {
                MenuItem item = new MenuItem(m);
                item.setOnAction(e -> {
                    field.setText(m);
                    popup.hide();
                });
                popup.getItems().add(item);
            }

            if (!popup.isShowing())
                popup.show(field, Side.BOTTOM, 0, 0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
