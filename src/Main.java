public class Main {

    public static void main(String[] args) throws Exception {

        Provider provider = Bootstrap.buildProvider();

        VectorExpression expr = new VectorExpression();
        expr.add(CombineOp.PLUS, "made");
        expr.add(CombineOp.MINUS, "make");
        expr.add(CombineOp.PLUS, "do");

        VectorArithmeticOperation op = new VectorArithmeticOperation(provider, SpaceId.FULL, expr, 5);

        VectorArithmeticResult r = (VectorArithmeticResult) op.execute();

        System.out.println("Top " + r.getK() + ":");
        for (Neighbor n : r.getTopK()) {
            System.out.println("  " + n.getKey() + "  dist=" + n.getDistance());
        }
    }
}
