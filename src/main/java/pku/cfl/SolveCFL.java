package pku.cfl;

import pku.PreprocessResult;
import pku.cfl.grammar.Grammar;
import pku.cfl.grammar.NonTerminal;
import pku.cfl.grammar.Symbol;
import pku.cfl.grammar.Terminal;

import java.util.*;

public class SolveCFL {

    public final CFLGraph cflGraph;

    public SolveCFL(PreprocessResult preprocessResult) {
        cflGraph = new CFLGraph();
        cflGraph.addNewEdges(preprocessResult.new_id);
        cflGraph.addAssignEdges(preprocessResult.assign);
        cflGraph.addGetFEdges(preprocessResult.getf);
        cflGraph.addPutFEdges(preprocessResult.putf);
    }

    // TODO: 收集 PointsTo, 给出处理 getF 和 putF 的正确方法
    public void Solve() {

        // 对于所有节点, 根据规则 (a) 加边
        cflGraph.getAllNodes().forEach((node) -> {
            for (NonTerminal nonTerminal : NonTerminal.values()) {
                // 寻找所有的 A -> epsilon
                var rule = Grammar.getRules(nonTerminal);
                if (rule.contains(List.of(Terminal.EPSILON))) {
                    // 加边
                    cflGraph.addEdge(new CFLEdge(node, node, nonTerminal));
                }
            }
        });

        // ToVisit <- all edges
        Queue<CFLEdge> toVisit = new LinkedList<>(cflGraph.getAllEdges());

        while (!toVisit.isEmpty()) {
            CFLEdge edge = toVisit.poll();

            // TODO: 这里没有处理不同的 GetField 和 PutField, 如何处理? 动态初始化语法还是什么?
            Symbol symbol = edge.symbol();

            // 根据规则 (b) 加边
            for (NonTerminal nonTerminal : NonTerminal.values()) {
                // forall (A in nonTerminal) if exist rule A -> B (where B == symbol)
                Grammar.getRules(nonTerminal).forEach((matchedRule) -> {
                    if (matchedRule.size() == 1 && matchedRule.get(0) == symbol) {
                        // add edge with symbol A
                        CFLEdge newEdge = new CFLEdge(edge.from(), edge.to(), nonTerminal);
                        cflGraph.addEdge(newEdge);
                        toVisit.offer(newEdge);
                    }
                });
            }

            // 根据规则 (c) 加边
            cflGraph.getPredEdges(edge.from()).forEach((pEdges) -> {
                Set<CFLEdge> newEdges = addEdgesByRuleC(pEdges, edge);
                newEdges.forEach((e) -> {
                    cflGraph.addEdge(e);
                    toVisit.offer(e);
                });
            });
            cflGraph.getSuccEdges(edge.to()).forEach((sEdges) -> {
                Set<CFLEdge> newEdges = addEdgesByRuleC(edge, sEdges);
                newEdges.forEach((e) -> {
                    cflGraph.addEdge(e);
                    toVisit.offer(e);
                });
            });
        }
    }

    private Set<CFLEdge> addEdgesByRuleC(CFLEdge leadingEdge, CFLEdge followingEdge) {
        Set<CFLEdge> edgeSet = new HashSet<>();
        Symbol leadingSymbol = leadingEdge.symbol();
        Symbol followingSymbol = followingEdge.symbol();

        for (NonTerminal nonTerminal : NonTerminal.values()) {
            // forall (A in nonTerminal) if exist rule A -> BC (where B == leadingSymbol and C == followingSymbol)
            Grammar.getRules(nonTerminal).forEach((matchedRule) -> {
                if (matchedRule.size() == 2 &&
                        matchedRule.get(0) == leadingSymbol &&
                        matchedRule.get(1) == followingSymbol) {
                    // add edge with symbol A
                    edgeSet.add(new CFLEdge(leadingEdge.from(), followingEdge.to(), nonTerminal));
                }
            });
        }
        return edgeSet;
    }
}
