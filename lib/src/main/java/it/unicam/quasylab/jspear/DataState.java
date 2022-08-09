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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

/**
 * A data state is an object associating variables with values.
 *
 */
public class DataState {

    private final double[] data;
    private final VariableRegistry registry;

    /**
     * Creates a new data state where all the variables in the given registry are equal to <code>0</code>.
     *
     * @param registry a registry containing the variables in the created data state.
     */
    public DataState(VariableRegistry registry) {
        this(registry, i -> 0.0);
    }

    /**
     * Creates a new data state where each variable <code>v</code> in the given registry is set equal to
     * <code>initFunction.apply(v.index())</code>.
     *
     * @param registry
     * @param initFunction
     */
    public DataState(VariableRegistry registry, IntToDoubleFunction initFunction) {
        this.registry = registry;
        this.data = IntStream.range(0, registry.size()).parallel().mapToDouble(initFunction).toArray();
    }

    private DataState(VariableRegistry registry, double[] data) {
        this.registry = registry;
        this.data = data;
    }

    //TODO: FIX ME!
//    public DataState(VariableRegistry variableRegistry, Map<Variable, IntToDoubleFunction> values) {
//        this(variableRegistry);
//        values.forEach((var, initFunction) -> {
//                IntStream.range(0, var.getSize()).forEach(i -> this.data[i+var.getFirstCellIndex()] = initFunction.applyAsDouble(i));
//            }
//        );
//    }

    public DataState(VariableRegistry variableRegistry, Map<Variable, Double> values) {
        this(variableRegistry);
        values.forEach((var, initFunction) -> {
                    double v = values.getOrDefault(var, 0.0);
                    IntStream.range(0, var.getSize()).forEach(i -> this.data[i+var.getFirstCellIndex()] = v);
                }
        );
    }


    /**
     * Returns the size of this data state, namely the number of stored variables.
     *
     * @return the number of stored variables.
     */
    public int size() {
        return this.data.length;
    }


    /**
     * Returns the value of the variable with the given name.
     *
     * @param name variable name.
     * @return the value of the variable with the given index.
     */
    public double getValue(String name) {
        Variable index = this.registry.getVariable(name);
        if (index == null) {
            throw new IllegalArgumentException(String.format("Variable %s is unknown!", name));
        }
        return getValue(index);
    }

    /**
     * Returns the value associated with the given variable. An {@link IllegalArgumentException} is thrown if the
     * variable is generated from a different registry.
     *
     * @param var a variable.
     * @return the value associated with the given variable.
     */
    public double getValue(Variable var) {
        return getValue(var, 0);
    }

    public double getValue(Variable var, int index) {
        if (var.registry() != this.registry) {
            throw new IllegalArgumentException("Illegal variable!");
        }
        return this.data[var.getFirstCellIndex()+index];
    }

    public double getValue(int i) {
        return this.data[i];
    }

    public DataState set(Variable var, double value) {
        return set(List.of(new DataStateUpdate(var, value)));
    }

    public DataState set(List<DataStateUpdate> updates) {
        double[] values = Arrays.copyOf(this.data, this.data.length);
        updates.forEach(vu -> vu.apply(values));
        return new DataState(this.registry, values);
    }


    public double[] getArrayValues(String name) {
        return null;//TODO FIXME!
    }
}
