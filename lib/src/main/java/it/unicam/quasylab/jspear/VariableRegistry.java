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

import java.util.HashMap;
import java.util.Map;

/**
 * Instances of used to define the set of variables occurring in a model.
 */
public final class VariableRegistry {

    private final Map<String,Variable> variableRegistry;
    private final Variable[] variables;

    /**
     * Creates a registry containing the given variables. An {@link IllegalArgumentException} is thrown if
     * a duplicated name occurs among the parameters.
     */
    public VariableRegistry(String ...  names) {
        this.variableRegistry = new HashMap<>();
        this.variables = new Variable[names.length];
        fillRegistry(names);
    }

    private void fillRegistry(String[] names) {
        for(int i=0; i< names.length; i++) {
            if (variableRegistry.containsKey(names[i])) {
                throw new IllegalArgumentException(String.format("Duplicated name %s.", names[i]));
            } else {
                Variable v = new Variable(this, i, names[i]);
                this.variableRegistry.put(names[i], v);
                this.variables[i] = v;
            }
        }
    }

    /**
     * Returns the variable in this registry with the given name. A <code>null</code> value is
     * returned if no variable in the registry exists with the given name.
     *
     * @param name variable name.
     * @return the variable in this registry with the given name.
     */
    public synchronized Variable getVariable(String name) {
        return variableRegistry.get(name);
    }

    /**
     * Returns the variable in this registry with the given index. A {@link ArrayIndexOutOfBoundsException} is
     * thrown if an illegal index is used.
     *
     * @param index variable index.
     * @return the variable in this registry with the given index.
     */
    public synchronized Variable getVariable(int index) {
        return variables[index];
    }

    /**
     * Returns the number of variables in this registry.
     *
     * @return the number of variables in this registry.
     */
    public int size() {
        return variables.length;
    }

    /**
     * Returns the index of the variable with the given name if exists, otherwise <code>-1</code> is
     * returned.
     *
     * @param name variable name.
     * @return the index of the variable with the given name.
     */
    public int getIndexOf(String name) {
        Variable v = getVariable(name);
        if (v == null) {
            return -1;
        } else {
            return v.index();
        }
    }
}
