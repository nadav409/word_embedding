import java.util.List;
import java.util.Set;

public interface UiStateListener {

    void onSelectionChanged(String selectedKey);

    void onMetricChanged(DistanceStrategy metric);

    void onPrimaryResultsChanged(List<Neighbor> results);

    void onHighlightsChanged(Set<String> highlightedKeys);

    void onStatusChanged(String message);

    void onErrorChanged(String message);
}
