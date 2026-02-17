import java.util.Collections;
import java.util.List;

public class CustomProjectionResult extends OperationResult {

    private final String a;
    private final String b;
    private final int k;
    private final List<CustomProjectionItem> topA;
    private final List<CustomProjectionItem> topB;

    public CustomProjectionResult(String a, String b, int k,
                                  List<CustomProjectionItem> topA,
                                  List<CustomProjectionItem> topB) {

        super(OperationType.PROJECTION);

        this.a = a;
        this.b = b;
        this.k = k;

        this.topA = (topA == null) ? List.of() : Collections.unmodifiableList(topA);
        this.topB = (topB == null) ? List.of() : Collections.unmodifiableList(topB);
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public int getK() {
        return k;
    }

    public List<CustomProjectionItem> getTopA() {
        return topA;
    }

    public List<CustomProjectionItem> getTopB() {
        return topB;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Operation: CUSTOM PROJECTION\n");
        sb.append("Axis: ").append(a).append(" -> ").append(b).append("\n");

        sb.append("\nMost ").append(a).append("-like:\n");
        for (CustomProjectionItem item : topA) {
            sb.append(item.getKey())
                    .append(" | ")
                    .append(String.format("%.6f", item.getScore()))
                    .append("\n");
        }

        sb.append("\nMost ").append(b).append("-like:\n");
        for (CustomProjectionItem item : topB) {
            sb.append(item.getKey())
                    .append(" | ")
                    .append(String.format("%.6f", item.getScore()))
                    .append("\n");
        }

        return sb.toString();
    }
}
