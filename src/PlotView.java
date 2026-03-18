import javafx.scene.Node;
import model.PlotPoint;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface PlotView {

    Node getNode();

    void setPoints(List<PlotPoint> points);

    void setSelectedKey(String key);

    // 🟠 תוצאות (למשל קרובים ל-centroid)
    void setHighlights(Set<String> keys);

    // 🟢 קבוצה
    void setGroupHighlights(Set<String> keys);

    void setLabels(Set<String> keys);

    void setOnItemClicked(Consumer<String> handler);
}
