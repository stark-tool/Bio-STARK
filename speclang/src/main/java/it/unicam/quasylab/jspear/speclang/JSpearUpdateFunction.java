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
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;
import it.unicam.quasylab.jspear.speclang.variables.Variable;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class JSpearUpdateFunction {


    private final boolean isArray;
    private final Variable variable;
    private final JSpearExpressionEvaluationFunction guard;
    private final JSpearExpressionEvaluationFunction first;
    private final JSpearExpressionEvaluationFunction last;
    private final JSpearExpressionEvaluationFunction newValue;

    public JSpearUpdateFunction(boolean isArray, Variable variable, JSpearExpressionEvaluationFunction guard, JSpearExpressionEvaluationFunction first, JSpearExpressionEvaluationFunction last, JSpearExpressionEvaluationFunction newValue) {
        this.isArray = isArray;
        this.variable = variable;
        this.guard = guard;
        this.first = first;
        this.last = last;
        this.newValue = newValue;
    }

    public DataStateUpdate eval(RandomGenerator rg, Map<String, JSpearValue> lv, DataState ds) {
//        if ((guard != null)&&(!guard.eval(rg, lv, ds).booleanOf())) {
//            return null;
//        }
//        if (!isArray) {
//            return new DataStateUpdate(this.variable, this.newValue.eval(rg, lv, ds).doubleOf());
//        }
//        if (first == null) {
//            return new DataStateUpdate(this.variable, this.newValue.eval(rg, lv, ds).toDoubleArray());
//        }
//        if (last == null) {
//            return new DataStateUpdate(this.variable, this.first.eval(rg, lv, ds).integerOf(), this.newValue.eval(rg, lv, ds).doubleOf());
//        }
//        return new DataStateUpdate(this.variable, this.first.eval(rg, lv, ds).integerOf(), this.last.eval(rg, lv, ds).integerOf(), this.newValue.eval(rg, lv, ds).toDoubleArray());
        return null;
    }

}
