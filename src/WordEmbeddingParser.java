import model.Embedding;
import model.RawEmbedding;
import model.Vector;
import model.WordEmbedding;

public class WordEmbeddingParser extends EmbeddingParser {

    @Override
    protected Embedding parse(RawEmbedding raw) {
        Vector vector = new Vector(raw.getValuesCopy());
        return new WordEmbedding(raw.getKey(), vector);
    }
}


