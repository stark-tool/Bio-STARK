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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * Instances of this class represent {@link DataStateFunction}s defined per variables.
 */
public class DefaultDataStateFunction implements DataStateFunction {

    private final VariableRegistry registry;
    private final DataStateRandomExpression[] updates;

    /**
     * Creates a data state function that implements the identity function.
     *
     * @param registry
     */
    public DefaultDataStateFunction(VariableRegistry registry, Map<String, DataStateRandomExpression> updates) {
        this.registry = registry;
        this.updates = new DataStateRandomExpression[registry.size()];
        setDefaultStepFunctions();
    }

    private void setDefaultStepFunctions() {
        IntStream.range(0, updates.length).forEach(i -> updates[i] = (rg, ds) -> ds.getValue(i));
    }

    /**
     * Returns the function used to compute the value of the variable with the given name. A null value is returned
     * if such variable does not exist.
     *
     * @param name variable name.
     * @return the function used to compute the value of the variable with the given name. A null value is returned
     * if such variable does not exist.
     */
    public DataStateRandomExpression getVariableUpdateFunction(String name) {
        return getVariableUpdateFunction(registry.getVariable(name));
    }

    /**
     * Returns the function used to compute the value of the variable with the given name. A null value is returned
     * if the given index is not valid.
     *
     * @param var variable name.
     * @return the function used to compute the value of the variable with the given name
     * in the resulting data state. A null value is returned if no variable does exist with the given name.
     */
    private DataStateRandomExpression getVariableUpdateFunction(Variable var) {
        if (var == null) {
            return null;
        }
        return updates[var.getFirstCellIndex()]; //TODO: CHECK!!!!
    }

    @Override
    public DataState apply(RandomGenerator rg, DataState ds) {
        return new DataState(registry, i -> updates[i].eval(rg, ds));
    }


}
