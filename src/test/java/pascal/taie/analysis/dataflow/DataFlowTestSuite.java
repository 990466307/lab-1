/*
 * Tai-e: A Program Analysis Framework for Java
 *
 * Copyright (C) 2020 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020 Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * This software is designed for the "Static Program Analysis" course at
 * Nanjing University, and it supports a subset of Java features.
 * Tai-e is only for educational and academic purposes, and any form of
 * commercial use is disallowed.
 */

package pascal.taie.analysis.dataflow;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import pascal.taie.analysis.dataflow.analysis.constprop.CPTestSuite;
import pascal.taie.analysis.dataflow.analysis.deadcode.DCDTestFull;
import pascal.taie.analysis.dataflow.lattice.LatticeTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CPTestSuite.class,
        DCDTestFull.class,
        LatticeTestSuite.class,
})
public class DataFlowTestSuite {
}