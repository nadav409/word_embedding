import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class FxApp extends Application {

    // Core
    private AppController controller;
    private UiState uiState;
    private AppPresenter presenter;

    // View parts
    private PlotPane plotPane;
    private NeighborsPane neighborsPane;
    private VectorArithmeticPane vectorPane;
    private CustomProjectionPane projectionPane;

    // Top controls
    private ComboBox<Integer> xBox;
    private ComboBox<Integer> yBox;
    private ComboBox<OperationType> opBox;

    @Override
    public void start(Stage stage) {

        // ---------- BOOTSTRAP ----------
        try {
            Provider provider = Bootstrap.buildProvider();
            controller = new AppController(provider);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        uiState = new UiState();
        presenter = new AppPresenter(controller, uiState);

        // ---------- PLOT ----------
        PlotView plotView = new PcaPlotView2D();
        plotPane = new PlotPane(plotView);

        // ---------- PANES ----------
        neighborsPane = new NeighborsPane(null);
        vectorPane = new VectorArithmeticPane(null);
        projectionPane = new CustomProjectionPane(null);

        // ---------- CONNECT UI STATE ----------
        uiState.addListener(plotPane);
        uiState.addListener(neighborsPane);
        uiState.addListener(vectorPane);
        uiState.addListener(projectionPane);

        // ---------- WIRE EVENTS ----------
        plotPane.setOnItemClicked(presenter::onItemSelected);

        neighborsPane.setOnSearchPicked(presenter::onItemSelected);
        neighborsPane.setOnMetricSelected(presenter::onMetricSelected);
        neighborsPane.setOnFindNeighborsRequested(presenter::onFindNeighborsRequested);

        vectorPane.setOnMetricSelected(presenter::onMetricSelected);
        vectorPane.setOnResultRequested(presenter::onVectorResultRequested);

        projectionPane.setOnProjectRequested(presenter::onProjectionRequested);

        // ---------- OPERATION SELECTOR ----------
        opBox = new ComboBox<>();
        opBox.getItems().addAll(OperationType.values());
        opBox.getSelectionModel().select(OperationType.NEIGHBORS);

        opBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) presenter.onOperationSelected(newV);
        });

        uiState.setSelectedOperation(OperationType.NEIGHBORS);

        HBox opRow = new HBox(10, new Label("Operation:"), opBox);
        opRow.setPadding(new Insets(5));

        // ---------- AXES ----------
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
        top.setPadding(new Insets(10));

        // ---------- LAYOUT ----------
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(plotPane.getNode());

        VBox right = new VBox(10,
                opRow,
                neighborsPane.getNode(),
                vectorPane.getNode(),
                projectionPane.getNode()
        );
        right.setPadding(new Insets(10));
        right.setPrefWidth(380);

        root.setRight(right);

        // ---------- OPERATION VISIBILITY ----------
        applyVisibility(uiState.getSelectedOperation());

        uiState.addListener(new UiStateListener() {
            @Override
            public void onOperationChanged(OperationType type) {
                applyVisibility(type);
            }

            @Override public void onSelectionChanged(String key) {}
            @Override public void onMetricChanged(DistanceStrategy metric) {}
            @Override public void onPrimaryResultsChanged(java.util.List<Neighbor> results) {}
            @Override public void onHighlightsChanged(java.util.Set<String> keys) {}
            @Override public void onStatusChanged(String msg) {}
            @Override public void onErrorChanged(String msg) {}
            @Override public void onProjectionResultChanged(CustomProjectionResult res) {}
        });

        Scene scene = new Scene(root, 1250, 760);
        stage.setScene(scene);
        stage.setTitle("PCA Plot");
        stage.show();

        redraw();
    }

    private void applyVisibility(OperationType type) {
        neighborsPane.getNode().setManaged(type == OperationType.NEIGHBORS);
        neighborsPane.getNode().setVisible(type == OperationType.NEIGHBORS);

        vectorPane.getNode().setManaged(type == OperationType.ARITHMETIC);
        vectorPane.getNode().setVisible(type == OperationType.ARITHMETIC);

        projectionPane.getNode().setManaged(type == OperationType.PROJECTION);
        projectionPane.getNode().setVisible(type == OperationType.PROJECTION);
    }

    private void redraw() {
        Integer x = xBox.getValue();
        Integer y = yBox.getValue();
        if (x == null || y == null) return;

        controller.setAxes2D(x, y);
        plotPane.setPoints(controller.getAllPcaPoints2D());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
