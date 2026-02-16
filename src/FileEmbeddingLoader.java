import java.nio.file.Path;

public abstract class FileEmbeddingLoader implements EmbeddingLoader {

    protected final Path path;

    protected FileEmbeddingLoader(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
