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

package it.unicam.quasylab.jspear.robtl.old;

import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.EvolutionSequence;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.ds.RelationOperator;
import it.unicam.quasylab.jspear.robtl.TruthValues;

/**
 * This class represent atomic properties of evolution sequences, expressed in the following terms:
 * The asymmetric distance, expressed by a suitable distance expression, between the evolution sequence and
 * its perturbed version, is compared with a real value
 */

public final class AtomicThreeValuedFormulaGeq implements ThreeValuedFormula {


    private final Perturbation perturbation;
    private final DistanceExpression expr;
    private final RelationOperator relop;
    private final double value;
    private final int m;
    private final double z;

    public AtomicThreeValuedFormulaGeq(Perturbation perturbation, DistanceExpression expr, RelationOperator relop, double value, int m, double z) {
        this.perturbation = perturbation;
        this.expr = expr;
        this.relop = relop;
        this.value = value;
        this.m = m;
        this.z = z;
    }


    @Override
    public TruthValues eval(int sampleSize, int step, EvolutionSequence sequence) {
        double[] res = expr.evalCI(step, sequence, sequence.apply(perturbation, step, sampleSize), m, z);
        if(res[1] < value && value < res[2]){return TruthValues.UNKNOWN;}
        if(relop.eval(res[0],value)){return TruthValues.TRUE;}
        return TruthValues.FALSE;
    }
}
