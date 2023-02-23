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

import java.util.Objects;
import java.util.stream.IntStream;

public final class MaxIntervalDistanceExpression implements DistanceExpression {

    private final DistanceExpression argument;
    private final int from;
    private final int to;

    public MaxIntervalDistanceExpression(DistanceExpression argument, int from, int to) {
        this.argument = Objects.requireNonNull(argument);
        if ((from<0)||(to<0)||(from>=to)) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public double compute(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(from+step, to+step).parallel().mapToDouble(i -> argument.compute(i, seq1, seq2)).max().orElse(Double.NaN);
    }

    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = new double[3];
        /*
        for(int i = from; i<to; i++) {

            double[] partial = argument.evalCI(i + step, seq1, seq2, m, z);
            res[0] = Math.max(res[0], partial[0]);
            res[1] = Math.max(res[1], partial[1]);
            res[2] = Math.max(res[2], partial[2]);
        }

         */
        res[0] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[0]).max().orElse(Double.NaN);
        res[1] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[1]).max().orElse(Double.NaN);
        res[2] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[2]).max().orElse(Double.NaN);
        return res;
    }

    @Override
    public double[] evalCILeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = new double[3];

        res[0] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCILeq(i, seq1, seq2, m, z)[0]).max().orElse(Double.NaN);
        res[1] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCILeq(i, seq1, seq2, m, z)[1]).max().orElse(Double.NaN);
        res[2] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCILeq(i, seq1, seq2, m, z)[2]).max().orElse(Double.NaN);
        return res;
    }

    @Override
    public double[] evalCIGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = new double[3];

        res[0] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCIGeq(i, seq1, seq2, m, z)[0]).max().orElse(Double.NaN);
        res[1] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCIGeq(i, seq1, seq2, m, z)[1]).max().orElse(Double.NaN);
        res[2] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCIGeq(i, seq1, seq2, m, z)[2]).max().orElse(Double.NaN);
        return res;
    }
}
