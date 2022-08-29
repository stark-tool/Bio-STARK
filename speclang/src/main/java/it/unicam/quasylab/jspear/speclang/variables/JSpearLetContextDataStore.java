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

public class JSpearLetContextDataStore implements JSpearStore {

    private final Variable letVariable;

    private final JSpearValue value;
    private final JSpearStore nestedStore;

    public JSpearLetContextDataStore(Variable letVariable, JSpearValue value, JSpearStore nestedStore) {
        this.letVariable = letVariable;
        this.value = value;
        this.nestedStore = nestedStore;
    }


    @Override
    public JSpearValue get(Variable variable) {
        return (this.letVariable.equals(variable)?value:nestedStore.get(variable));
    }

    @Override
    public int size() {
        return 1+nestedStore.size();
    }
}
