public enum CombineOp {
    PLUS(+1),
    MINUS(-1);

    private final int sign;

    CombineOp(int sign) {
        this.sign = sign;
    }

    public int sign() {
        return sign;
    }
}
