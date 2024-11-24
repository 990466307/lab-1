package pku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.misc.IRDumper;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.Var;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;

public class PointerAnalysisTrivial extends ProgramAnalysis<PointerAnalysisResult> {

    public static final String ID = "pku-pta-trivial";

    private static final Logger logger = LogManager.getLogger(IRDumper.class);

    /**
     * Directory to dump Result.
     */
    private final File dumpPath = new File("result.txt");

    public PointerAnalysisTrivial(AnalysisConfig config) {
        super(config);
        if (dumpPath.exists()) {
            dumpPath.delete();
        }
    }

    public void analyze_virtual_method(PreprocessResult preprocess, JMethod method, Var return_receiver, List<Var> in_params, Var self) {
        logger.info("Analyzing method {}", method.getName());
        var single_preprocess = new PreprocessResult();
        single_preprocess.analysis(method.getIR());
        if (!method.getReturnType().getName().equals("void")) {
            single_preprocess.assign.put(single_preprocess.returnvalue, return_receiver);
        }
        if (!in_params.isEmpty()) {
            int count = in_params.size();
            for (int i = 0; i < count; i++) {
                single_preprocess.assign.put(in_params.get(i), single_preprocess.params.get(i));
            }
        }
        single_preprocess.assign.put(self, single_preprocess.this_self);
        logger.info("invoke {} static methods", single_preprocess.staticinvokelist.size());
        single_preprocess.staticinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_static_method(single_preprocess, dfmethod, info.receiver, info.inparams);
                }
            });
        });
        logger.info("invoke {} virtual methods", single_preprocess.virtualinvokelist.size());
        single_preprocess.virtualinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_virtual_method(single_preprocess, dfmethod, info.receiver, info.inparams, info.self);
                }
            });
        });
        single_preprocess.test_pts.forEach((i, j) -> {
            preprocess.test_pts.put(i, j);
        });
        single_preprocess.assign.forEach((i, j) -> {
            preprocess.assign.put(i, j);
        });
        single_preprocess.new_id.forEach((i, j) -> {
            preprocess.new_id.put(i, j);
        });
        single_preprocess.putf.forEach((i, j) -> {
            preprocess.putf.put(i, j);
        });
        single_preprocess.getf.forEach((i, j) -> {
            preprocess.getf.put(i, j);
        });
    }

    public void analyze_static_method(PreprocessResult preprocess, JMethod method, Var return_receiver, List<Var> in_params) {
        logger.info("Analyzing method {}", method.getName());
        var single_preprocess = new PreprocessResult();
        single_preprocess.analysis(method.getIR());
        if (!method.getReturnType().getName().equals("void")) {
            single_preprocess.assign.put(single_preprocess.returnvalue, return_receiver);
        }
        if (!in_params.isEmpty()) {
            int count = in_params.size();
            for (int i = 0; i < count; i++) {
                single_preprocess.assign.put(in_params.get(i), single_preprocess.params.get(i));
            }
        }
        logger.info("invoke {} static methods", single_preprocess.staticinvokelist.size());
        single_preprocess.staticinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_static_method(single_preprocess, dfmethod, info.receiver, info.inparams);
                }
            });
        });
        logger.info("invoke {} virtual methods", single_preprocess.virtualinvokelist.size());
        single_preprocess.virtualinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_virtual_method(single_preprocess, dfmethod, info.receiver, info.inparams, info.self);
                }
            });
        });
        single_preprocess.test_pts.forEach((i, j) -> {
            preprocess.test_pts.put(i, j);
        });
        single_preprocess.assign.forEach((i, j) -> {
            preprocess.assign.put(i, j);
        });
        single_preprocess.new_id.forEach((i, j) -> {
            preprocess.new_id.put(i, j);
        });
        single_preprocess.putf.forEach((i, j) -> {
            preprocess.putf.put(i, j);
        });
        single_preprocess.getf.forEach((i, j) -> {
            preprocess.getf.put(i, j);
        });
    }

    public void analyze_main_method(PreprocessResult preprocess, JMethod method) {
        logger.info("Analyzing method {}", method.getName());
        var single_preprocess = new PreprocessResult();
        single_preprocess.analysis(method.getIR());
        logger.info("invoke {} static methods", single_preprocess.staticinvokelist.size());
        single_preprocess.staticinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_static_method(single_preprocess, dfmethod, info.receiver, info.inparams);
                }
            });
        });
        logger.info("invoke {} virtual methods", single_preprocess.virtualinvokelist.size());
        single_preprocess.virtualinvokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    analyze_virtual_method(single_preprocess, dfmethod, info.receiver, info.inparams, info.self);
                }
            });
        });
        single_preprocess.test_pts.forEach((i, j) -> {
            preprocess.test_pts.put(i, j);
        });
        single_preprocess.assign.forEach((i, j) -> {
            preprocess.assign.put(i, j);
        });
        single_preprocess.new_id.forEach((i, j) -> {
            preprocess.new_id.put(i, j);
        });
        single_preprocess.putf.forEach((i, j) -> {
            preprocess.putf.put(i, j);
        });
        single_preprocess.getf.forEach((i, j) -> {
            preprocess.getf.put(i, j);
        });
    }

    @Override

    public PointerAnalysisResult analyze() {
        var preprocess = new PreprocessResult();
        var result = new PointerAnalysisResult();

        World.get().getClassHierarchy().applicationClasses().forEach(jclass -> {
            jclass.getDeclaredMethods().forEach(method -> {
                if ((method.getName()).equals("main")) {
                    analyze_main_method(preprocess, method);
                }
            });
        });

        preprocess.assign.forEach((v1, v2) -> {
            logger.info("assign {} in {} = {} in {}", v2.getName(), v2.getMethod(), v1.getName(), v1.getMethod());
        });
        preprocess.new_id.forEach((v, i) -> {
            logger.info("{} in {} point to new {} ", v.getName(), v.getMethod(), i);
        });
        preprocess.getf.forEach((v1, M) -> {
            M.forEach((s, v2) -> {
                logger.info("assign {} in {} = {}.{} in {}", v2.getName(), v2.getMethod(), v1.getName(), s, v1.getMethod());
            });
        });
        preprocess.putf.forEach((v1, M) -> {
            M.forEach((s, v2) -> {
                logger.info("assign {}.{} in {} = {} in {}", v2.getName(), s, v2.getMethod(), v1.getName(), v1.getMethod());
            });
        });

        dump(result);

        return result;
    }

    protected void dump(PointerAnalysisResult result) {
        try (PrintStream out = new PrintStream(new FileOutputStream(dumpPath))) {
            out.println(result);
        } catch (FileNotFoundException e) {
            logger.warn("Failed to dump", e);
        }
    }

}
