/*
 * JSpear: a SimPle Environment for statistical estimation of Adaptation and Reliability.
 *
 *              Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unicam.quasylab.jspear.robtl;

import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.EvolutionSequence;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.ds.RelationOperator;

public final class AtomicRobustnessFormula implements RobustnessFormula {

    private final Perturbation perturbation;
    private final DistanceExpression expr;
    private final RelationOperator relop;
    private final double value;
    private final int op;

    public AtomicRobustnessFormula(Perturbation perturbation, DistanceExpression expr, RelationOperator relop, double value) {
        this(perturbation, expr, relop, value, 0);
    }

    public AtomicRobustnessFormula(Perturbation perturbation, DistanceExpression expr, RelationOperator relop, double value, int op) {
        this.perturbation = perturbation;
        this.expr = expr;
        this.relop = relop;
        this.value = value;
        this.op = op;
    }

    @Override
    public <T> RobustnessFunction<T> eval(RobustnessFormulaVisitor<T> evaluator) {
        return evaluator.evalAtomic(this);
    }

    public DistanceExpression getDistanceExpression() {
        return this.expr;
    }

    public Perturbation getPerturbation() {
        return this.perturbation;
    }

    public RelationOperator getRelationOperator() {
        return this.relop;
    }

    public double getValue() {
        return this.value;
    }
}
