package pku;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import pascal.taie.ir.IR;
import pascal.taie.ir.exp.IntLiteral;
import pascal.taie.ir.exp.InvokeStatic;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Copy;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.New;

public class PreprocessResult {

    public final Map<Var, TreeSet<Integer>> var2id;
    // public final Map<New, Integer> obj_ids;
    public final Map<Integer, Var> test_pts;

    public PreprocessResult() {
        var2id = new HashMap();
        // obj_ids = new HashMap<New, Integer>();
        test_pts = new HashMap();
    }

    /**
     * Benchmark.alloc(id); X x = new X;// stmt
     *
     * @param stmt statement that allocates a new object
     * @param id id of the object allocated
     */
    public void alloc(New stmt, int id) {
        // obj_ids.put(stmt, id);
        Var new_var = stmt.getLValue();
        if (!var2id.containsKey(new_var)) {
            TreeSet<Integer> empty = new TreeSet();
            var2id.put(new_var, empty);
        }
        var2id.get(new_var).add(id);
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

    /**
     *
     * @param stmt statement that allocates a new object
     * @return id of the object allocated
     */
    // public int getObjIdAt(New stmt) {
    //     return obj_ids.get(stmt);
    // }
    /**
     * @param id
     * @return the pointer/variable in Benchmark.test(id, var);
     */
    public Var getTestPt(int id) {
        return test_pts.get(id);
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
                Var lval = ((Copy) stmt).getLValue();
                Var rval = ((Copy) stmt).getRValue();

                TreeSet<Integer> copyid = new TreeSet<>();
                var2id.forEach((var, set) -> {
                    if (var == rval) {
                        set.forEach((integer) -> {
                            copyid.add(integer);
                        });
                    }
                });
                if (!var2id.containsKey(lval)) {
                    TreeSet<Integer> empty = new TreeSet();
                    var2id.put(lval, empty);
                }
                copyid.forEach((integer) -> {
                    var2id.get(lval).add(integer);
                    // System.out.println("1");
                });
            }
            id = 0;
        }
    }
}
