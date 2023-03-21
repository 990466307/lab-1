/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.pta.plugin.reflection;

import pascal.taie.analysis.pta.core.cs.element.CSVar;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.plugin.util.AbstractModel;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;

import java.util.Set;

/**
 * Base class for reflection inference.
 * TODO: take ClassLoader.loadClass(...) into account.
 */
abstract class InferenceModel extends AbstractModel {

    protected final MetaObjHelper helper;

    protected final Set<Invoke> invokesWithLog;

    InferenceModel(Solver solver, MetaObjHelper helper, Set<Invoke> invokesWithLog) {
        super(solver);
        this.helper = helper;
        this.invokesWithLog = invokesWithLog;
    }

    protected abstract void handleNewNonInvokeStmt(Stmt stmt);

    public abstract void forName(CSVar csVar, PointsToSet pts, Invoke invoke);

    public abstract void getConstructor(CSVar csVar, PointsToSet pts, Invoke invoke);

    public abstract void getDeclaredConstructor(CSVar csVar, PointsToSet pts, Invoke invoke);

    public abstract void getMethod(CSVar csVar, PointsToSet pts, Invoke invoke);

    public abstract void getDeclaredMethod(CSVar csVar, PointsToSet pts, Invoke invoke);
}