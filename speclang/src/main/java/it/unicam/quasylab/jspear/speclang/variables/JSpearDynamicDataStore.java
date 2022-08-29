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

package it.unicam.quasylab.jspear.speclang.variables;

import it.unicam.quasylab.jspear.speclang.values.JSpearValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is a data store where variables can be dynamically allocated and deallocated.
 */
public class JSpearDynamicDataStore implements JSpearStore {

    private final Map<Variable, JSpearValue> values;

    private final JSpearStore nestedStore;

    public JSpearDynamicDataStore(Map<Variable, JSpearValue> values, JSpearStore nestedStore) {
        this.values = values;
        this.nestedStore = nestedStore;
    }

    public JSpearDynamicDataStore(Map<Variable, JSpearValue> values) {
        this(values, null);
    }

    public JSpearDynamicDataStore() {
        this(new HashMap<>());
    }


    @Override
    public JSpearValue get(Variable variable) {
        JSpearValue value = this.values.get(variable);
        return Objects.requireNonNullElseGet(value, () -> (this.nestedStore!=null?this.nestedStore.get(variable):JSpearValue.ERROR_VALUE));
    }

    @Override
    public int size() {
        int size = this.values.size();
        if (this.nestedStore != null) {
            size += this.nestedStore.size();
        }
        return size;
    }

    public JSpearValue set(Variable variable, JSpearValue newValue) {
        return this.values.put(variable, newValue);
    }

    public void clear(Variable variable) {
        this.values.remove(variable);
    }
}
