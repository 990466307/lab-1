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

    @Override
    public PointerAnalysisResult analyze() {
        var preprocess = new PreprocessResult();
        var result = new PointerAnalysisResult();

        World.get().getClassHierarchy().applicationClasses().forEach(jclass -> {
            logger.info("Analyzing class {}", jclass.getName());
            jclass.getDeclaredMethods().forEach(method -> {
                if (!method.isAbstract()) {
                    logger.info("Analyzing method {}", method.getName());
                    preprocess.analysis(method.getIR());
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
            logger.info("assign {} = {}", v2.getName(), v1.getName());
        });
        preprocess.new_id.forEach((v, i) -> {
            logger.info("new {} point to {} ", v.getName(), i);
        });
        preprocess.getf.forEach((v1, M) -> {
            M.forEach((s, v2) -> {
                logger.info("assign {} = {}.{}", v2.getName(), v1.getName(), s);
            });
        });
        preprocess.putf.forEach((v1, M) -> {
            M.forEach((s, v2) -> {
                logger.info("assign {}.{} = {}", v2.getName(), s, v1.getName());
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
