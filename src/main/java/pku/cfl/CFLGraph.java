package pku.cfl;

import pascal.taie.ir.exp.Var;
import pku.cfl.grammar.*;

import java.util.*;
import java.util.stream.Collectors;


public class CFLGraph {

    /**
     * 以 node 为起点指向其后继的边
     */
    public final Map<CFLNode, List<CFLEdge>> cflGraphSuccs;

    /**
     *  以 node 为终点被其前驱指向的边
     */
    public final Map<CFLNode, List<CFLEdge>> cflGraphPreds;

    public CFLGraph() {
        this.cflGraphSuccs = new HashMap<>();
        this.cflGraphPreds = new HashMap<>();
    }

    /**
     * 初始化图, 加 new 边
     * i: x = new()
     *  |
     * o_i -- new -> x, x -- r_new -> o_i
     */
    public void addNewEdges(Map<Var, Integer> news) {
        news.forEach((var, id) -> {
            CFLNode allocNode = CFLNode.ofAllocId(id);
            CFLNode varNode = CFLNode.ofVar(var);

            addEdge(new CFLEdge(allocNode, varNode, Terminal.NEW));
            addEdge(new CFLEdge(varNode, allocNode, Terminal.RNEW));
        });
    }

    /**
     * 初始化图, 加 assign 边
     * x = y
     *  |
     * y -- assign -> x, x -- r_assign -> y
     */
    public void addAssignEdges(Map<Var, Var> assigns) {
        assigns.forEach((rval, lval) -> {
            CFLNode lvalNode = CFLNode.ofVar(lval);
            CFLNode rvalNode = CFLNode.ofVar(rval);

            addEdge(new CFLEdge(rvalNode, lvalNode, Terminal.ASSIGN));
            addEdge(new CFLEdge(lvalNode, rvalNode, Terminal.RASSIGN));
        });
    }

    /**
     * 初始化图, 加 putF 边
     * x.f = y
     *  |
     * y -- putF[f] -> x, x -- r_putF[f] -> y
     */
    public void addPutFEdges(Map<Var, Map<String, Var>> putFs) {
        putFs.forEach((rval, puts) -> {
            puts.forEach((fieldvar, lval) -> {
                CFLNode lvalNode = CFLNode.ofVar(lval);
                CFLNode rvalNode = CFLNode.ofVar(rval);

                addEdge(new CFLEdge(rvalNode, lvalNode, Terminal.createPutF(false, fieldvar)));
                addEdge(new CFLEdge(lvalNode, rvalNode, Terminal.createPutF(true, fieldvar)));
            });
        });
    }

    /**
     * 初始化图, 加 getF 边
     * x = y.f
     *  |
     * y -- getF[f] -> x, x -- r_getF[f] -> y
     */
    public void addGetFEdges(Map<Var, Map<String, Var>> getFs) {
        getFs.forEach((lval, gets) -> {
            gets.forEach((fieldvar, rval) -> {
                CFLNode lvalNode = CFLNode.ofVar(lval);
                CFLNode rvalNode = CFLNode.ofVar(rval);

                addEdge(new CFLEdge(rvalNode, lvalNode, Terminal.createGetF(false, fieldvar)));
                addEdge(new CFLEdge(lvalNode, rvalNode, Terminal.createGetF(true, fieldvar)));
            });
        });
    }

    public Set<CFLEdge> getAllEdges() {
        return cflGraphSuccs.values().stream().flatMap(List::stream).collect(Collectors.toSet());
    }

    public Set<CFLNode> getAllNodes(){
        return cflGraphSuccs.keySet();
    }

    public void addEdge(CFLEdge edge) {
        cflGraphSuccs.computeIfAbsent(edge.from(), var -> new ArrayList<>()).add(edge);
        cflGraphPreds.computeIfAbsent(edge.to(), var -> new ArrayList<>()).add(edge);
    }

    /**
     * 以 node 为起点指向其后继的边
     * @param node node
     * @return edges
     */
    public Set<CFLEdge> getSuccEdges(CFLNode node) {
        return new HashSet<>(cflGraphSuccs.get(node));
    }

    /**
     * 以 node 为终点被其前驱指向的边
     * @param node node
     * @return edges
     */
    public Set<CFLEdge> getPredEdges(CFLNode node) {
        return new HashSet<>(cflGraphPreds.get(node));
    }
}
