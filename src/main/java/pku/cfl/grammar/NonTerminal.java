package pku.cfl.grammar;

public enum NonTerminal implements Symbol {
    FLOW_TO("FlowTo"),
    T1("T1"),
    T2("T2"),
    T3("T3"),
    T4("T4"),
    T5("T5"),
    T6("T6"),
    POINTS_TO("PointsTo"),
    ALIAS("Alias");

    private final String name;

    NonTerminal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
