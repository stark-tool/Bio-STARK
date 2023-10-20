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

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Class MinIntervalDistanceExpression implements the distance expression for the evaluation of
 * the minimum value assumed by a given distance expression in a given time interval.
 */
public final class MinIntervalDistanceExpression implements DistanceExpression {

    private final DistanceExpression expression;
    private final int from;
    private final int to;

    /**
     * Generates the distance expression for the evaluation of
     * the minimum of given distance expression in a given time interval.
     *
     * @param expression the distance expression
     * @param from the left bound of the time interval
     * @param to the right bound of the time interval.
     */
    public MinIntervalDistanceExpression(DistanceExpression expression, int from, int to) {
        this.expression = Objects.requireNonNull(expression);
        if ((from<0)||(to<0)||(from>=to)) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Computes the minimum of the distance between two evolution sequences over the time interval shifted by a given time step.
     *
     * @param step time step at which we start the evaluation of the expression
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @return the minimum of the evaluations of <code>expression</code> between <code>seq1</code> and <code>seq2</code>
     * at each time step in the interval <code>[from+step, to+step]</code>
     */
    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(from+step, to+step)
                .sequential()
                .mapToDouble(i -> expression.compute(i, seq1, seq2))
                .min()
                .orElse(Double.NaN);
    }

    /**
     * The confidence interval is obtained from the confidence intervals on the evaluations of the expression
     * by taking the minima for each bound.
     *
     * @param step time step at which we start the evaluation of the expression
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @param m number of repetitions for the bootstrap method
     * @param z the quantile of the standard normal distribution corresponding to the desired coverage probability.
     * @return the evaluation of the minimum evaluation and the related confidence interval,
     * whose bounds are obtained by taking the minimum of the respective bounds from the
     * confidence intervals on the evaluations of the expression.
     */
    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = new double[3];
        List<double[]> resList = IntStream.range(from + step, to + step).parallel().mapToObj(i -> expression.evalCI(i, seq1, seq2, m, z)).toList();
        res[0] = resList.stream().parallel().mapToDouble(r -> r[0]).min().orElse(Double.NaN);
        res[1] = resList.stream().parallel().mapToDouble(r -> r[1]).min().orElse(Double.NaN);
        res[2] = resList.stream().parallel().mapToDouble(r -> r[2]).min().orElse(Double.NaN);
        return res;
    }

}
