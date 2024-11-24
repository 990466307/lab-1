package pku.cfl;

import pascal.taie.ir.exp.Var;
import pku.cfl.grammar.Symbol;

/**
 * @param from   From
 * @param to     To
 * @param symbol attached symbol
 */
public record CFLEdge(CFLNode from, CFLNode to, Symbol symbol) {
}
