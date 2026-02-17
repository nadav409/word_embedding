public class CustomProjectionItem {
    private final String key;
    private final double score;

    public CustomProjectionItem(String key, double score) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null/blank");
        }
        this.key = key;
        this.score = score;
    }

    public String getKey() {
        return key;
    }

    public double getScore() {
        return score;
    }
}
