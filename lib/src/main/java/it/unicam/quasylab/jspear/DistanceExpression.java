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

package it.unicam.quasylab.jspear;

import java.util.stream.IntStream;

public sealed interface DistanceExpression permits
        AtomicDistanceExpression,
        LinearCombinationDistanceExpression,
        MaxDistanceExpression,
        MaxIntervalDistanceExpression,
        MinDistanceExpression,
        MinIntervalDistanceExpression,
        ThresholdDistanceExpression {

    /**
     * Returns the evaluation of the distance expression among the two sequences at the given step.
     *
     * @param step step where the expression is evaluated
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @return the evaluation of the distance expression at the given step among the two sequences.
     */
    double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2);

    default double[] compute(int from, int to, EvolutionSequence seq1, EvolutionSequence seq2) {
        return compute(IntStream.range(from, to+1).toArray(), seq1, seq2);
    }

    default double[] compute(int[] steps, EvolutionSequence seq1, EvolutionSequence seq2) {
        return IntStream.of(steps).mapToDouble(i -> compute(i, seq1, seq2)).toArray();
    }

    /**
     * Returns the evaluation of the distance expression among the two sequences at the given step and the related confidence interval.
     *
     * @param step step where the expression is evaluated
     * @param seq1 an evolution sequence
     * @param seq2 an evolution sequence
     * @param m number of repetition for bootstrapping
     * @param z the desired z-score
     * @return the evaluation of the distance expression at the given step among the two sequences and its confidence interval.
     */
    double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z);

}
