package operations;

import java.util.List;

public class CustomProjectionResult implements OperationResult {

    private final String a;
    private final String b;
    private final List<CustomProjectionItem> items;

    public CustomProjectionResult(String a, String b, List<CustomProjectionItem> items) {
        if (a == null || a.isBlank()) {
            throw new IllegalArgumentException("a cannot be null or blank");
        }

        if (b == null || b.isBlank()) {
            throw new IllegalArgumentException("b cannot be null or blank");
        }

        this.a = a;
        this.b = b;

        if (items == null) {
            this.items = List.of();
        } else {
            this.items = items;
        }
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public List<CustomProjectionItem> getItems() {
        return items;
    }
}