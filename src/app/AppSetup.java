package app;

import io.EmbeddingLoader;
import io.JsonEmbeddingLoader;
import model.*;
import model.Vector;
import operations.CosineDistance;

import java.nio.file.Path;
import java.util.*;

public class AppSetup {

    public static Provider buildProvider() throws Exception {

        runPythonEmbeddingScript();

        EmbeddingLoader fullLoader = new JsonEmbeddingLoader(Path.of("full_vectors.json"));
        EmbeddingLoader pcaLoader  = new JsonEmbeddingLoader(Path.of("pca_vectors.json"));

        List<RawEmbedding> rawFull = fullLoader.load();
        List<RawEmbedding> rawPca  = pcaLoader.load();

        EmbeddingSpace fullSpace = new EmbeddingSpace(toWordEmbeddings(rawFull));
        EmbeddingSpace pcaSpace  = new EmbeddingSpace(toWordEmbeddings(rawPca));

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

    private static List<Embedding> toWordEmbeddings(List<RawEmbedding> raw) {
        List<Embedding> out = new ArrayList<>(raw.size());

        for (RawEmbedding r : raw) {
            String key = r.getKey();
            double[] values = r.getValuesCopy();

            model.Vector vector = new Vector(values);
            Embedding embedding = new WordEmbedding(key, vector);

            out.add(embedding);
        }

        return out;
    }
}