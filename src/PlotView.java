import javafx.scene.Node;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface PlotView {

    // מה שמכניסים ל-UI (BorderPane.setCenter)
    Node getNode();

    void setPoints(List<PlotPoint> points);

    void setSelectedKey(String key);

    void setHighlights(Set<String> keys);

    void setLabels(Set<String> keys);

    void setOnWordClicked(Consumer<String> callback);
}
