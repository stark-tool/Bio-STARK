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

package it.unicam.quasylab.jspear.ds;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public record DataRange(double minValue, double maxValue) {

    public DataRange() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public static DataRange[] getDefaultRangeArray(int size) {
        return IntStream.range(0, size).mapToObj(i -> new DataRange()).toArray(DataRange[]::new);
    }

    public static double[] apply(DataRange[] dataRanges, double[] data) {
        return IntStream.range(0, dataRanges.length).mapToDouble(i -> dataRanges[i].apply(data[i])).toArray();
    }

    public double apply(double v) {
        return Math.max(minValue, Math.min(maxValue, v));
    }
}
