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

import java.util.stream.IntStream;

public final class MinDistanceExpression implements DistanceExpression {

    private final DistanceExpression expr1;
    private final DistanceExpression expr2;

    public MinDistanceExpression(DistanceExpression expr1, DistanceExpression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return Math.min(expr1.compute(step, seq1, seq2), expr2.compute(step, seq1, seq2));
    }

    @Override
    public double computeLeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return Math.min(expr1.computeLeq(step, seq1, seq2), expr2.computeLeq(step, seq1, seq2));
    }

    @Override
    public double computeGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return Math.min(expr1.computeGeq(step, seq1, seq2), expr2.computeGeq(step, seq1, seq2));
    }

    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0,3)
                .mapToDouble(i -> Math.min(expr1.evalCI(step, seq1, seq2, m, z)[i], expr2.evalCI(step, seq1, seq2, m, z)[i]))
                .toArray();
    }

    @Override
    public double[] evalCILeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0,3)
                .mapToDouble(i -> Math.min(expr1.evalCILeq(step, seq1, seq2, m, z)[i], expr2.evalCILeq(step, seq1, seq2, m, z)[i]))
                .toArray();
    }

    @Override
    public double[] evalCIGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0,3)
                .mapToDouble(i -> Math.min(expr1.evalCIGeq(step, seq1, seq2, m, z)[i], expr2.evalCIGeq(step, seq1, seq2, m, z)[i]))
                .toArray();
    }
}
