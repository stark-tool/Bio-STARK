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

import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.EvolutionSequence;

import java.util.Objects;
import java.util.stream.IntStream;

public final class UntilDistanceExpression implements DistanceExpression {
    private final DistanceExpression leftExpression;
    private final int from;
    private final int to;
    private final DistanceExpression rightExpression;

    public UntilDistanceExpression(DistanceExpression leftExpression, int from, int to, DistanceExpression rightExpression) {
        this.leftExpression = Objects.requireNonNull(leftExpression);
        this.rightExpression = Objects.requireNonNull(rightExpression);
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
        return IntStream.range(from+step, to+step).parallel()
                .mapToDouble(i -> Math.max(rightExpression.compute(i, seq1, seq2),
                        IntStream.range(from+step,i).parallel()
                                .mapToDouble(j-> leftExpression.compute(j,seq1,seq2)).max().getAsDouble()))
                .min().orElse(Double.NaN);
    }

    @Override
    public double computeLeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(from+step, to+step).parallel()
                .mapToDouble(i -> Math.max(rightExpression.computeLeq(i, seq1, seq2),
                        IntStream.range(from+step,i).parallel()
                                .mapToDouble(j-> leftExpression.computeLeq(j,seq1,seq2)).max().getAsDouble()))
                .min().orElse(Double.NaN);
    }

    @Override
    public double computeGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(from+step, to+step).parallel()
                .mapToDouble(i -> Math.max(rightExpression.computeGeq(i, seq1, seq2),
                        IntStream.range(from+step,i).parallel()
                                .mapToDouble(j-> leftExpression.computeGeq(j,seq1,seq2)).max().getAsDouble()))
                .min().orElse(Double.NaN);
    }

    @Override
    public double[] evalCI(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = {1.0,1.0,1.0};
        for(int i = from+step; i<to+step; i++) {
            double[] resR = rightExpression.evalCI(i, seq1, seq2, m, z);
            double[] resL = leftExpression.evalCI(i,seq1,seq2,m,z);
            for(int j =from+step; j<i; j++) {
                double[] partialL = leftExpression.evalCI(j,seq1,seq2,m,z);
                resL[0] = Math.max(resL[0], partialL[0]);
                resL[1] = Math.max(resL[1], partialL[1]);
                resL[2] = Math.max(resL[2], partialL[2]);
            }
            res[0] = Math.min(res[0],Math.max(resR[0],resL[0]));
            res[1] = Math.min(res[1],Math.max(resR[1],resL[1]));
            res[2] = Math.min(res[2],Math.max(resR[2],resL[2]));
        }
        //res[0] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[0]).max().orElse(Double.NaN);
        //res[1] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[1]).max().orElse(Double.NaN);
        //res[2] = IntStream.range(from + step, to + step).parallel().mapToDouble(i -> argument.evalCI(i, seq1, seq2, m, z)[2]).max().orElse(Double.NaN);
        return res;
    }

    @Override
    public double[] evalCILeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = {1.0,1.0,1.0};
        for(int i = from+step; i<to+step; i++) {
            double[] resR = rightExpression.evalCILeq(i, seq1, seq2, m, z);
            double[] resL = leftExpression.evalCILeq(i,seq1,seq2,m,z);
            for(int j =from+step; j<i; j++) {
                double[] partialL = leftExpression.evalCILeq(j,seq1,seq2,m,z);
                resL[0] = Math.max(resL[0], partialL[0]);
                resL[1] = Math.max(resL[1], partialL[1]);
                resL[2] = Math.max(resL[2], partialL[2]);
            }
            res[0] = Math.min(res[0],Math.max(resR[0],resL[0]));
            res[1] = Math.min(res[1],Math.max(resR[1],resL[1]));
            res[2] = Math.min(res[2],Math.max(resR[2],resL[2]));
        }

        return res;
    }

    @Override
    public double[] evalCIGeq(int step, EvolutionSequence seq1, EvolutionSequence seq2, int m, double z) {
        if (step<0) {
            throw new IllegalArgumentException();
        }
        double[] res = {1.0,1.0,1.0};
        for(int i = from+step; i<to+step; i++) {
            double[] resR = rightExpression.evalCIGeq(i, seq1, seq2, m, z);
            double[] resL = leftExpression.evalCIGeq(i,seq1,seq2,m,z);
            for(int j =from+step; j<i; j++) {
                double[] partialL = leftExpression.evalCIGeq(j,seq1,seq2,m,z);
                resL[0] = Math.max(resL[0], partialL[0]);
                resL[1] = Math.max(resL[1], partialL[1]);
                resL[2] = Math.max(resL[2], partialL[2]);
            }
            res[0] = Math.min(res[0],Math.max(resR[0],resL[0]));
            res[1] = Math.min(res[1],Math.max(resR[1],resL[1]));
            res[2] = Math.min(res[2],Math.max(resR[2],resL[2]));
        }
        return res;
    }
}
