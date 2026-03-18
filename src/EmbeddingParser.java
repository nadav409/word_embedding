import model.Embedding;
import model.RawEmbedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class EmbeddingParser {

    public final List<Embedding> parseAll(Collection<RawEmbedding> raws) {

        if (raws == null) {
            throw new IllegalArgumentException("raws cannot be null");
        }
        if (raws.isEmpty()) {
            throw new IllegalArgumentException("raws cannot be empty");
        }

        List<Embedding> result = new ArrayList<>(raws.size());

        for (RawEmbedding raw : raws) {
            if (raw == null) {
                throw new IllegalArgumentException("raw embedding cannot be null");
            }

            Embedding e = parse(raw);
            if (e == null) {
                throw new IllegalStateException("parser returned null embedding");
            }

            result.add(e);
        }

        return result;
    }

    protected abstract Embedding parse(RawEmbedding raw);
}

