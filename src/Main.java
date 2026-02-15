import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        JsonEmbeddingLoader loader = new JsonEmbeddingLoader();

        List<RawEmbedding> full = loader.load(Path.of("full_vectors.json"));
        List<RawEmbedding> pca  = loader.load(Path.of("pca_vectors.json"));

        System.out.println("Loaded full: " + full.size());
        System.out.println("Loaded pca : " + pca.size());

        RawEmbedding first = full.get(0);
        System.out.println("First word: " + first.getText());
        System.out.println("Vector dimension: " + first.getValuesCopy().length);

        RawEmbedding firstPca = pca.get(0);
        System.out.println("PCA dimension: " + firstPca.getValuesCopy().length);
    }
}
