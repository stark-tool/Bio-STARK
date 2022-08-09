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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DataStateUpdate {
    private final int from;

    private final int to;
    private final double[] values;


    public DataStateUpdate(Variable variable, double value) {
        this(variable, 0, value );
    }

    public DataStateUpdate(Variable variable, int idx, double value) {
        this(variable.getFirstCellIndex()+idx, variable.getFirstCellIndex()+idx+1, new double[] { value });
    }

    public DataStateUpdate(Variable variable, int from, int to, double[] values) {
        this(variable.getFirstCellIndex()+from, variable.getFirstCellIndex()+to, values);
    }

    public DataStateUpdate(Variable variable, double[] values) {
        this(variable.getFirstCellIndex(), variable.getFirstCellIndex()+variable.getSize(), values);
    }


    public DataStateUpdate(int from, int to, double[] values) {
        if ((values.length < to-from)||(from<0)||(to<0)) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.to = to;
        this.values = values;
    }


    public int getStartOfUpdatingIngerval() {
        return from;
    }

    public int getEndOfUpdatingInterval() {
        return to;
    }

    public void apply(double[] cells) {
        IntStream.range(from, to).forEach(i -> cells[i] = values[i] );
    }



    @Override
    public String toString() {
        return "["+IntStream.range(0, to-from).mapToObj(i -> (from+i)+" <- "+values[i]).collect(Collectors.joining(", "))+"]";
    }
}
