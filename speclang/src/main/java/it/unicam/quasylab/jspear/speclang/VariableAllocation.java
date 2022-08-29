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

package it.unicam.quasylab.jspear.speclang;

import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.speclang.variables.Variable;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;

import java.util.function.Function;

public class VariableAllocation {
    public boolean isAVariable(String arrayName) {
        return false;
    }

    public Function<DataState, JSpearValue> getQueryFunction(String name) {
        Variable variable = getVariable(name);
        return ds -> getValue(variable, ds);
    }

    public Variable getVariable(String name) {
        return null;
    }

    public JSpearValue getValue(Variable variable, DataState dataState) {
        return JSpearValue.ERROR_VALUE;
    }

}
