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
import it.unicam.quasylab.jspear.ds.DataStateExpression;

/**
 * Class AtomicDistanceExpressionGeq implements the atomic distance expression
 * evaluating the hemidistance between the first and the second evolution sequence at a given time step.
 */
public final class AtomicDistanceExpressionLeq implements DistanceExpression {

    private final DataStateExpression rho;

    /**
     * Generates the atomic distance expression that will use the given penalty function
     * for the evaluation of the ground distance on data states
     * @param rho the penalty function
     */
    public AtomicDistanceExpressionLeq(DataStateExpression rho) {
        this.rho = rho;
    }

    /**
     * Evaluates the hemidistance between the first and second evolution sequence at the given time step.
     *
     * @param step time step at which the atomic distance is evaluated
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @return the hemidistance between the distribution reached by <code>seq1</code> and that reached by <code>seq2</code> at time <code>step</code>.
     */
    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return seq1.get(step).distanceLeq(rho, seq2.get(step));
    }

    /**
     * In addition to the evaluation of the distance,
     * it also computes, via empirical bootstrap, the confidence interval on the evaluation
     * with respect to a desired coverage probability.
     *
     * @param step time step at which the atomic distance is evaluated
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @param m number of repetitions for the bootstrap method
     * @param z the quantile of the normal distribution encoding the desired coverage probability
     * @return the array containing the evaluation of the distance
     * and the two bounds of the confidence interval evaluated via empirical bootstrap.
     */
    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z){
        double[] res = new double[3];
        res[0] = seq1.get(step).distanceLeq(rho, seq2.get(step));
        double[] partial = seq1.get(step).bootstrapDistanceLeq(rho, seq2.get(step),m,z);
        res[1] = partial[0];
        res[2] = partial[1];
        return res;
    }

}
