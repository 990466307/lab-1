package pku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.misc.IRDumper;
import pascal.taie.config.AnalysisConfig;
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

    public void analyze_single_method(PreprocessResult preprocess, JMethod method) {
        logger.info("Analyzing method {}", method.getName());
        preprocess.analysis(method.getIR());
        logger.info("invoke {} methods", preprocess.invokelist.size());
        preprocess.invokelist.forEach(info -> {
            JClass dfclass = info.defineclass;
            String invname = info.invokename;
            dfclass.getDeclaredMethods().forEach(dfmethod -> {
                if ((dfmethod.getName()).equals(invname)) {
                    preprocess.invokelist.remove(info);
                    analyze_single_method(preprocess, dfmethod);
                    if (!dfmethod.getReturnType().getName().equals("void")) {
                        preprocess.assign.put(preprocess.returnvalue, info.reciever);
                    }
                }
            });
        });
    }

    @Override

    public PointerAnalysisResult analyze() {
        var preprocess = new PreprocessResult();
        var result = new PointerAnalysisResult();

        World.get().getClassHierarchy().applicationClasses().forEach(jclass -> {
            jclass.getDeclaredMethods().forEach(method -> {
                if ((method.getName()).equals("main")) {
                    analyze_single_method(preprocess, method);
                }
            });
        });

        // var objs = new TreeSet<>(preprocess.obj_ids.values());
        // preprocess.test_pts.forEach((test_id, pt)->{
        //     result.put(test_id, objs);
        // });
        // 遍历每个测试点
        // preprocess.test_pts.forEach((test_id, pt) -> {
        //     TreeSet<Integer> matchingobjs = new TreeSet<>();
        //     if (preprocess.var2id.containsKey(pt)) {
        //         preprocess.var2id.get(pt).forEach((integer) -> {
        //             matchingobjs.add(integer);
        //         });
        //     }
        //     // 将匹配的对象集合放入结果中
        //     if (!matchingobjs.isEmpty()) {
        //         result.put(test_id, matchingobjs);
        //     }
        // });
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
