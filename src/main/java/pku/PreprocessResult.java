package pku;

import java.util.HashMap;
import java.util.Map;

import pascal.taie.ir.IR;
import pascal.taie.ir.exp.IntLiteral;
import pascal.taie.ir.exp.InvokeStatic;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Copy;
import pascal.taie.ir.stmt.FieldStmt;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.New;

public class PreprocessResult {

    public final Map<Var, Var> assign;
    public final Map<Var, Map<String, Var>> getf;
    public final Map<Var, Map<String, Var>> putf;
    public final Map<Var, Integer> new_id;
    public final Map<Integer, Var> test_pts;

    public PreprocessResult() {
        assign = new HashMap();
        new_id = new HashMap();
        test_pts = new HashMap();
        getf = new HashMap();
        putf = new HashMap();
    }

    /**
     * Benchmark.alloc(id); X x = new X;// stmt
     *
     * @param stmt statement that allocates a new object
     * @param id id of the object allocated
     */
    public void alloc(New stmt, int id) {
        Var new_var = stmt.getLValue();
        new_id.put(new_var, id);
    }

    /**
     * Benchmark.test(id, var)
     *
     * @param id id of the testing
     * @param v the pointer/variable
     */
    public void test(int id, Var v) {
        test_pts.put(id, v);
    }

    public void add_getf(Var v1, String s, Var v2) {
        if (!getf.containsKey(v2)) {
            Map<String, Var> empty = new HashMap();
            getf.put(v2, empty);
        }
        getf.get(v2).put(s, v1);
    }

    public void add_putf(Var v1, String s, Var v2) {
        if (!putf.containsKey(v2)) {
            Map<String, Var> empty = new HashMap();
            putf.put(v2, empty);
        }
        putf.get(v2).put(s, v1);
    }

    /**
     * analysis of a JMethod, the result storing in this
     *
     * @param ir ir of a JMethod
     */
    public void analysis(IR ir) {
        var stmts = ir.getStmts();
        Integer id = 0;
        for (var stmt : stmts) {

            if (stmt instanceof Invoke) {
                var exp = ((Invoke) stmt).getInvokeExp();
                if (exp instanceof InvokeStatic) {
                    var methodRef = ((InvokeStatic) exp).getMethodRef();
                    var className = methodRef.getDeclaringClass().getName();
                    var methodName = methodRef.getName();
                    if (className.equals("benchmark.internal.Benchmark")
                            || className.equals("benchmark.internal.BenchmarkN")) {
                        if (methodName.equals("alloc")) {
                            var lit = exp.getArg(0).getConstValue();
                            assert lit instanceof IntLiteral;
                            id = ((IntLiteral) lit).getNumber();
                            continue;
                        } else if (methodName.equals("test")) {
                            var lit = exp.getArg(0).getConstValue();
                            assert lit instanceof IntLiteral;
                            var test_id = ((IntLiteral) lit).getNumber();
                            var pt = exp.getArg(1);
                            this.test(test_id, pt);
                        }
                    }

                }
            } else if (stmt instanceof New) {
                if (id != 0) // ignore unlabeled `new` stmts
                {
                    this.alloc((New) stmt, id);
                }
            } else if (stmt instanceof Copy) {
                var lval = ((Copy) stmt).getLValue();
                var rval = ((Copy) stmt).getRValue();

                assign.put(rval, lval);
            } else if (stmt instanceof FieldStmt) {
                var fieldstmt = (FieldStmt) stmt;
                var fieldvar = fieldstmt.getFieldRef();
                var lval = fieldstmt.getLValue();
                var rval = fieldstmt.getRValue();
                var lfield = lval.getUses();
                var rfield = rval.getUses();
                // System.out.printf("Name: %s\n", x);
                if (!lfield.isEmpty()) {
                    lfield.forEach((v) -> {
                        add_putf((Var) v, fieldvar.getName(), (Var) rval);
                    });
                }
                if (!rfield.isEmpty()) {
                    rfield.forEach((v) -> {
                        add_getf((Var) lval, fieldvar.getName(), (Var) v);
                    });
                }
            }
            id = 0;
        }
    }
}
