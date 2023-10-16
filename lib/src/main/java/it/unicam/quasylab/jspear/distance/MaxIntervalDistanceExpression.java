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
        List<double[]> resList = IntStream.range(from + step, to + step).parallel().mapToObj(i -> argument.evalCI(i, seq1, seq2, m, z)).toList();
        res[0] = resList.stream().parallel().mapToDouble(r -> r[0]).max().orElse(Double.NaN);
        res[1] = resList.stream().parallel().mapToDouble(r -> r[1]).max().orElse(Double.NaN);
        res[2] = resList.stream().parallel().mapToDouble(r -> r[2]).max().orElse(Double.NaN);
        return res;
    }

}
