package pku.cfl.grammar;

public enum Terminal implements Symbol {
    ASSIGN("assign"),
    RASSIGN("r_assign"),
    NEW("new"),
    RNEW("r_new"),
    PUT_F("putf"),
    RPUT_F("r_putf"),
    GET_F("getf"),
    RGET_F("r_getf"),
    EPSILON("epsilon");

    private final String name;
    private String field;

    Terminal(String name) {
        this.name = name;
        this.field = null;
    }

    Terminal(String name, String field) {
        this.name = name;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    // 动态字段 PUT_F
    public static Terminal createPutF(Boolean reverse, String field) {
        if (!reverse) {
            Terminal putF = Terminal.PUT_F;
            putF.field = field;
            return putF;
        }
        Terminal rputF = Terminal.RPUT_F;
        rputF.field = field;
        return rputF;
    }

    // 动态字段 GET_F
    public static Terminal createGetF(Boolean reverse, String field) {
        if (!reverse) {
            Terminal getF = Terminal.GET_F;
            getF.field = field;
            return getF;
        }
        Terminal rgetF = Terminal.RGET_F;
        rgetF.field = field;
        return rgetF;
    }

    @Override
    public String toString() {
        return field != null ? name + "_" + field : name;
    }
}
