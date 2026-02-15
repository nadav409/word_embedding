public class WordEmbeddingFactory implements EmbeddingFactory {

    @Override
    public AbstractEmbedding create(RawEmbedding raw) {
        if (raw == null) {
            throw new IllegalArgumentException("raw cannot be null");
        }
        return new WordEmbedding(raw.getText(), new Vector(raw.getValuesCopy()));
    }
}
