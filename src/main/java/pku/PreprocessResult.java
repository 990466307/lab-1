package pku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pascal.taie.ir.IR;
import pascal.taie.ir.exp.IntLiteral;
import pascal.taie.ir.exp.InvokeStatic;
import pascal.taie.ir.exp.InvokeVirtual;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Copy;
import pascal.taie.ir.stmt.FieldStmt;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.New;
import pascal.taie.ir.stmt.Return;
import pascal.taie.language.classes.JClass;

public class PreprocessResult {

    public class staticinvokeinfo {

        public Var receiver;
        public JClass defineclass;
        public String invokename;
        public List<Var> inparams;

        public staticinvokeinfo(Var v, JClass c, String s, List<Var> l) {
            this.receiver = v;
            this.defineclass = c;
            this.invokename = s;
            this.inparams = l;
        }
    }

    public class virtualinvokeinfo {

        public Var receiver;
        public JClass defineclass;
        public String invokename;
        public List<Var> inparams;
        public Var self;

        public virtualinvokeinfo(Var v, JClass c, String s, List<Var> l, Var ss) {
            this.receiver = v;
            this.defineclass = c;
            this.invokename = s;
            this.inparams = l;
            this.self = ss;
        }
    }

    public final Map<Var, Var> assign;
    public final Map<Var, Map<String, Var>> getf;
    public final Map<Var, Map<String, Var>> putf;
    public final Map<Var, Integer> new_id;
    public final Map<Integer, Var> test_pts;
    public final Set<staticinvokeinfo> staticinvokelist;
    public final Set<virtualinvokeinfo> virtualinvokelist;
    public final List<Var> params;
    public Var returnvalue;
    public Var this_self;

    public PreprocessResult() {
        assign = new HashMap();
        new_id = new HashMap();
        test_pts = new HashMap();
        getf = new HashMap();
        putf = new HashMap();
        staticinvokelist = new HashSet();
        virtualinvokelist = new HashSet();
        params = new ArrayList<>();
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
        var vars = ir.getParams();
        vars.forEach(param -> {
            params.add(param);
        });

        var ts = ir.getThis();
        this_self = ts;

        var stmts = ir.getStmts();
        Integer id = 0;
        for (var stmt : stmts) {
            if (stmt instanceof Invoke) {
                var invoke = (Invoke) stmt;
                var exp = invoke.getInvokeExp();
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
                    } else {
                        var receiver = invoke.getLValue();
                        var defineclass = invoke.getMethodRef().getDeclaringClass();
                        var invokename = invoke.getMethodRef().getName();
                        var inparams = invoke.getRValue().getArgs();
                        // System.out.printf("1 : %s\n", x);
                        // System.out.printf("2 : %s\n", y);
                        // System.out.printf("3 : %s\n", z);
                        // System.out.printf("4 : %s\n", w);
                        staticinvokelist.add(new staticinvokeinfo(receiver, defineclass, invokename, inparams));
                    }
                } else if (exp instanceof InvokeVirtual) {
                    var receiver = invoke.getLValue();
                    var defineclass = invoke.getMethodRef().getDeclaringClass();
                    var invokename = invoke.getMethodRef().getName();
                    var inparams = invoke.getRValue().getArgs();
                    var self = ((InvokeVirtual) exp).getBase();
                    virtualinvokelist.add(new virtualinvokeinfo(receiver, defineclass, invokename, inparams, self));
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
            } else if (stmt instanceof Return) {
                var ret = (Return) stmt;
                returnvalue = ret.getValue();
            }
            id = 0;
        }
    }
}
