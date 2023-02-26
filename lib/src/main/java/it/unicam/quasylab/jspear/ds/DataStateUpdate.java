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


import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.function.BiFunction;

public final class DataStateUpdate {

    private final int index;

    private final double value;

    public DataStateUpdate(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public static BiFunction<RandomGenerator, DataState, List<DataStateUpdate>> set(int idx, double value) {
        return (rg, ds) -> List.of(new DataStateUpdate(idx, value));
    }

    @Override
    public String toString() {
        return index+"<-"+value;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}
