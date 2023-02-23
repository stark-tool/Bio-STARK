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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public final class LinearCombinationDistanceExpression implements DistanceExpression {

    public final double weights[];
    public final DistanceExpression expressions[];

    public LinearCombinationDistanceExpression(double[] weights, DistanceExpression[] expressions) {
        if (weights.length != expressions.length) {
            throw new IllegalArgumentException();
        }
        this.weights = weights;
        this.expressions = expressions;
    }


    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        return IntStream.range(0, weights.length)
                .mapToDouble(i -> weights[i]*expressions[i].compute(step, seq1, seq2))
                .sum();
    }

    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        return IntStream.range(0,3).mapToDouble(j -> IntStream.range(0, weights.length)
                .mapToDouble(i -> weights[i]*expressions[i].evalCI(step, seq1, seq2, m, z)[j])
                .sum()).toArray();
    }

    @Override
    public double[] evalCILeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        return IntStream.range(0,3).mapToDouble(j -> IntStream.range(0, weights.length)
                .mapToDouble(i -> weights[i]*expressions[i].evalCILeq(step, seq1, seq2, m, z)[j])
                .sum()).toArray();
    }

    @Override
    public double[] evalCIGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        return IntStream.range(0,3).mapToDouble(j -> IntStream.range(0, weights.length)
                .mapToDouble(i -> weights[i]*expressions[i].evalCIGeq(step, seq1, seq2, m, z)[j])
                .sum()).toArray();
    }
}
