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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class provides utility methods that implement useful tasks.
 */
public class Util {


    public static <T extends SystemState> double[] estimateProbabilityDistribution(SampleSet<T> sampleSet,
                                                                                   DataStateExpression expr,
                                                                                   double from,
                                                                                   double to,
                                                                                   int steps) {
        double dt = (to-from)/steps;
        double[] result = sampleSet.evalPenaltyFunction(expr);
        double size = result.length;
        return IntStream.range(0, steps).mapToDouble(i -> DoubleStream.of(result).filter(x -> x<=from+i*dt).count()/size).toArray();
    }

    public static <T extends SystemState> double[] estimateProbabilityDistribution(SampleSet<T> sampleSet,
                                                                                   DataStateExpression expr,
                                                                                   int steps) {
        return estimateProbabilityDistribution(sampleSet, expr, 0, 1.0, steps);
    }

    public static <T extends SystemState> double[][] evalDataStateExpression(EvolutionSequence sequence, int to, DataStateExpression expressions) {
        return evalDataStateExpression(sequence, 0, to, expressions);
    }

    public static <T extends SystemState> double[][] evalDataStateExpression(EvolutionSequence sequence, int from, int to, DataStateExpression expressions) {
        return IntStream.range(from, to).mapToObj(i -> sequence.get(i).evalPenaltyFunction(expressions)).toArray(double[][]::new);
    }

    public static <T extends SystemState> double[][] evalDistanceExpression(EvolutionSequence sequence, EvolutionSequence sequence2, int to, DistanceExpression ...  expressions) {
        return evalDistanceExpression(sequence, sequence2, 0, to, expressions);
    }

    public static <T extends SystemState> double[][] evalDistanceExpression(EvolutionSequence sequence, EvolutionSequence sequence2, int from, int to, DistanceExpression ...  expressions) {
        return IntStream.range(from, to).mapToObj(i -> Stream.of(expressions).mapToDouble(expr -> expr.compute(i, sequence, sequence2)).toArray()).toArray(double[][]::new);
    }

    public static void writeToCSV(String fileName, double[][] data) throws IOException {
        Files.writeString(Path.of(fileName), stringOfCSV(data));
    }

    private static CharSequence stringOfCSV(double[][] data) {
        return Stream.of(data).sequential().map(row -> DoubleStream.of(row).sequential().mapToObj(d -> ""+d).collect(Collectors.joining(", "))).collect(Collectors.joining("\n"));
    }
}
