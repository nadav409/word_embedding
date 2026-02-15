import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface EmbeddingLoader {

    /**
     * Loads raw embeddings from a source file.
     * @param path path to the file (e.g., full_vectors.json)
     * @return list of raw embeddings (text + raw numeric array)
     * @throws IOException if the file cannot be read
     */
    List<RawEmbedding> load(Path path) throws IOException;
}

