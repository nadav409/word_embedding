package app;

import io.EmbeddingLoader;
import io.EmbeddingParser;
import io.JsonEmbeddingLoader;
import io.WordEmbeddingParser;
import model.*;
import model.Vector;
import operations.CosineDistance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AppSetup {

    public static Provider buildProvider() throws Exception {

        Path fullPath = Path.of("full_vectors.json");
        Path pcaPath  = Path.of("pca_vectors.json");

        if (!Files.exists(fullPath) || !Files.exists(pcaPath)) {
            runPythonEmbeddingScript();
        }

        EmbeddingLoader fullLoader = new JsonEmbeddingLoader(fullPath);
        EmbeddingLoader pcaLoader  = new JsonEmbeddingLoader(pcaPath);

        List<RawEmbedding> rawFull = fullLoader.load();
        List<RawEmbedding> rawPca  = pcaLoader.load();

        EmbeddingParser parser = new WordEmbeddingParser();

        EmbeddingSpace fullSpace = new EmbeddingSpace(parser.parseAll(rawFull));
        EmbeddingSpace pcaSpace  = new EmbeddingSpace(parser.parseAll(rawPca));

        Map<SpaceId, EmbeddingSpace> spaces = new HashMap<>();
        spaces.put(SpaceId.FULL, fullSpace);
        spaces.put(SpaceId.PCA, pcaSpace);

        return new ResearchEnvironment(spaces, new CosineDistance());
    }

    private static void runPythonEmbeddingScript() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("python", "embedder.py");
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Python script failed with exit code " + exitCode);
        }
    }


}