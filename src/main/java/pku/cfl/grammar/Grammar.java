package pku.cfl.grammar;
import java.util.*;


/*

FlowTo      := new (assign | (put[f] Alias get[f]))*
PointsTo    := (r_assign | (r_get[f] Alias r_put[f]))* r_new
Alias       := PointsTo FlowTo

 |
 |

FlowTo      := new T1

T1          := assign T1
            := put[f] T2
            := epsilon

T2          := Alias T3

T3          := get[f] T1

PointsTo    := T4 r_new

T4          := r_assign T4
            := r_get[f] T5
            := epsilon

T5          := Alias T6

T6          := r_put[f] T4

Alias       := PointsTo FlowTo

 */

// TODO: 文法是动态的? 根据 fields 来定; 可能要把 Terminal 和 NonTerminal 从枚举改成字符串匹配
public final class Grammar {
    private static final Map<NonTerminal, List<List<Symbol>>> RULES;

    static {
        RULES = new HashMap<>();
        RULES.put(NonTerminal.FLOW_TO,
                List.of(
                        Arrays.asList(Terminal.NEW, NonTerminal.T1)
                )
        );
        RULES.put(NonTerminal.T1,
                Arrays.asList(
                        Arrays.asList(Terminal.ASSIGN, NonTerminal.T1),
                        Arrays.asList(Terminal.PUT_F, NonTerminal.T2),
                        List.of(Terminal.EPSILON)
                )
        );
        RULES.put(NonTerminal.T2,
                List.of(
                        Arrays.asList(NonTerminal.ALIAS, NonTerminal.T3)
                )
        );
        RULES.put(NonTerminal.T3,
                List.of(
                        Arrays.asList(Terminal.GET_F, NonTerminal.T1)
                )
        );
        RULES.put(NonTerminal.POINTS_TO,
                List.of(
                        Arrays.asList(NonTerminal.T4, Terminal.RNEW)
                )
        );
        RULES.put(NonTerminal.T4,
                Arrays.asList(
                        Arrays.asList(Terminal.RASSIGN, NonTerminal.T4),
                        Arrays.asList(Terminal.RGET_F, NonTerminal.T5), List.of(Terminal.EPSILON)
                )
        );
        RULES.put(NonTerminal.T5,
                List.of(
                        Arrays.asList(NonTerminal.ALIAS, NonTerminal.T6)
                )
        );
        RULES.put(NonTerminal.T6,
                List.of(
                        Arrays.asList(Terminal.RPUT_F, NonTerminal.T4)
                )
        );
        RULES.put(NonTerminal.ALIAS,
                List.of(
                        Arrays.asList(NonTerminal.POINTS_TO, NonTerminal.FLOW_TO)
                )
        );
    }

    private Grammar() {
    }

    public static List<List<Symbol>> getRules(NonTerminal head) {
        return RULES.getOrDefault(head, Collections.emptyList());
    }

    public static void printRules() {
        RULES.forEach((key, value) -> value.forEach(body ->
                System.out.println(key + " -> " + body)
        ));
    }
}
