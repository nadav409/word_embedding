public class WordEmbeddingParser extends EmbeddingParser {

    @Override
    protected Embedding parse(RawEmbedding raw) {
        Vector vector = new Vector(raw.getValuesCopy());
        return new WordEmbedding(raw.getKey(), vector);
    }
}


