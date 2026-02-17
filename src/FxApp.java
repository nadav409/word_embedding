import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

public class FxApp extends Application {

    private AppController controller;

    private Pane canvas;

    private ComboBox<Integer> xBox;
    private ComboBox<Integer> yBox;

    // תגית hover אחת קבועה (לא מוסיפים/מסירים כל הזמן)
    private Label hoverLabel;

    @Override
    public void start(Stage stage) {

        try {
            Provider provider = Bootstrap.buildProvider();
            controller = new AppController(provider);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        canvas = new Pane();
        canvas.setPrefSize(900, 700);

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
                new Label("X (PC index):"), xBox,
                new Label("Y (PC index):"), yBox,
                redrawBtn
        );
        top.setStyle("-fx-padding: 10; -fx-alignment: center-left;");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(canvas);

        Scene scene = new Scene(root, 1000, 750);
        stage.setTitle("PCA Plot (hover to see word)");
        stage.setScene(scene);
        stage.show();

        // Hover label פעם אחת
        hoverLabel = new Label();
        hoverLabel.setVisible(false);
        hoverLabel.setMouseTransparent(true); // ⭐⭐ זה מה שמפסיק את הריצוד!
        hoverLabel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-border-color: rgba(0,0,0,0.35);" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 3 8 3 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        canvas.getChildren().add(hoverLabel);

        canvas.widthProperty().addListener((obs, a, b) -> redraw());
        canvas.heightProperty().addListener((obs, a, b) -> redraw());

        redraw();
    }

    private void redraw() {
        if (controller == null) return;

        Integer x = xBox.getValue();
        Integer y = yBox.getValue();
        if (x == null || y == null) return;

        controller.setAxes2D(x, y);
        drawAllPoints();
    }


    private void drawAllPoints() {
        canvas.getChildren().clear();
        canvas.getChildren().add(hoverLabel);
        hoverLabel.setVisible(false);

        List<PlotPoint> pts = controller.getAllPcaPoints2D();
        if (pts.isEmpty()) return;

        // auto-scale
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (PlotPoint p : pts) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double w = canvas.getWidth() > 0 ? canvas.getWidth() : canvas.getPrefWidth();
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : canvas.getPrefHeight();
        double pad = 30;

        double dx = (maxX - minX);
        double dy = (maxY - minY);

        if (dx == 0) dx = 1e-9;
        if (dy == 0) dy = 1e-9;


        // "יפה" יותר: נקודות קטנות עם שקיפות + בלי stroke כבד
        // בחרתי כחול עדין. אם אתה רוצה צבע אחר תגיד.
        Color dotColor = Color.rgb(50, 80, 255, 0.7);

        for (PlotPoint p : pts) {
            double cx = pad + (p.getX() - minX) / dx * (w - 2 * pad);
            double cy = pad + (maxY - p.getY()) / dy * (h - 2 * pad);

            Circle dot = new Circle(cx, cy, 2.1);
            dot.setFill(dotColor);
            dot.setStroke(null);
            dot.setEffect(null); // shadow על אלפים נקודות זה כבד ומכוער

            // hitbox שקוף גדול (רק לתפיסת עכבר)
            Circle hit = new Circle(cx, cy, 10);
            hit.setFill(Color.TRANSPARENT);

            hit.setOnMouseEntered(e -> {
                showHoverLabel(cx, cy, p.getKey());
            });

            hit.setOnMouseMoved(e -> {
                // אם מזיזים קצת בתוך האזור, נעדכן מיקום מדויק
                showHoverLabel(cx, cy, p.getKey());
            });

            hit.setOnMouseExited(e -> hoverLabel.setVisible(false));

            canvas.getChildren().addAll(dot, hit);
        }

        hoverLabel.toFront();
    }

    private void showHoverLabel(double x, double y, String text) {
        hoverLabel.setText(text);
        hoverLabel.autosize();

        double lw = hoverLabel.getWidth();
        double lh = hoverLabel.getHeight();

        // למעלה מהנקודה, מרכז
        double lx = x - lw / 2.0;
        double ly = y - 14 - lh;

        // שלא יברח מחוץ למסך
        if (lx < 5) lx = 5;
        if (ly < 5) ly = 5;

        hoverLabel.setLayoutX(lx);
        hoverLabel.setLayoutY(ly);
        hoverLabel.setVisible(true);
        hoverLabel.toFront();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
