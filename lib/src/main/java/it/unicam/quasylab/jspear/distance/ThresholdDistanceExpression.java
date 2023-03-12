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

package it.unicam.quasylab.jspear.distance;

import it.unicam.quasylab.jspear.EvolutionSequence;
import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.ds.RelationOperator;

public final class ThresholdDistanceExpression implements DistanceExpression {

    private final double threshold;
    private final RelationOperator relop;
    private final DistanceExpression expression;

    public ThresholdDistanceExpression(DistanceExpression expression, RelationOperator relop, double threshold) {
        this.threshold = threshold;
        this.relop = relop;
        this.expression = expression;
    }


    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return (relop.eval(expression.compute(step, seq1, seq2),threshold)?0.0:1.0);
    }

    @Override
    public double computeLeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return (relop.eval(expression.computeLeq(step, seq1, seq2),threshold)?0.0:1.0);
    }

    @Override
    public double computeGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return (relop.eval(expression.computeGeq(step, seq1, seq2),threshold)?0.0:1.0);
    }

    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        double[] res = new double[3];
        double[] value = expression.evalCI(step, seq1, seq2, m, z);
        res[0]= relop.eval(value[0],threshold)?0.0:1.0;
        if(value[1]< threshold && threshold< value[2]){
            res[1] = 0.0;
            res[2] = 1.0;
        }
        else {
            res[1] = res[0];
            res[2] = res[0];
        }
        return res;
    }

    @Override
    public double[] evalCILeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        double[] res = new double[3];
        double[] value = expression.evalCILeq(step, seq1, seq2, m, z);
        res[0]= relop.eval(value[0],threshold)?0.0:1.0;
        if(value[1]< threshold && threshold< value[2]){
            res[1] = 0.0;
            res[2] = 1.0;
        }
        else {
            res[1] = res[0];
            res[2] = res[0];
        }
        return res;
    }

    @Override
    public double[] evalCIGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        double[] res = new double[3];
        double[] value = expression.evalCIGeq(step, seq1, seq2, m, z);
        res[0]= relop.eval(value[0],threshold)?0.0:1.0;
        if(value[1]< threshold && threshold< value[2]){
            res[1] = 0.0;
            res[2] = 1.0;
        }
        else {
            res[1] = res[0];
            res[2] = res[0];
        }
        return res;
    }
}
