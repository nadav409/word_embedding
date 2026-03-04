import java.util.List;

public class CustomProjectionResult implements OperationResult {

    private String a;
    private String b;
    private int k;

    private List<CustomProjectionItem> topA;
    private List<CustomProjectionItem> topB;

    public CustomProjectionResult(String a, String b, int k, List<CustomProjectionItem> topA, List<CustomProjectionItem> topB) {

        this.a = a;
        this.b = b;
        this.k = k;

        if (topA == null) {
            this.topA = List.of();
        } else {
            this.topA = topA;
        }

        if (topB == null) {
            this.topB = List.of();
        } else {
            this.topB = topB;
        }
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
}