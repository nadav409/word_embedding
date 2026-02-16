import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        Embedding e1 = new WordEmbedding("king", new Vector(new double[]{1, 0}));
        Embedding e2 = new WordEmbedding("queen", new Vector(new double[]{0, 1}));

        List<Embedding> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);

        EmbeddingSpace fullSpace = new EmbeddingSpace(list);

        Map<SpaceId, EmbeddingSpace> map = new EnumMap<>(SpaceId.class);
        map.put(SpaceId.FULL, fullSpace);

        ResearchEnvironment env = new ResearchEnvironment(map, new CosineDistance());

        ResearchOperation op = new DistanceOperation(env, SpaceId.FULL, "king", "queen");
        System.out.println("Cosine:    " + op.execute());

        env.setDistanceStrategy(new EuclideanDistance());
        System.out.println("Euclidean: " + op.execute());
    }
}
