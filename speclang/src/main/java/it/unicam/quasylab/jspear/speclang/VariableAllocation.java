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
import it.unicam.quasylab.jspear.speclang.types.*;
import it.unicam.quasylab.jspear.speclang.values.*;
import it.unicam.quasylab.jspear.speclang.variables.JSpearDataStateStore;
import it.unicam.quasylab.jspear.speclang.variables.JSpearStore;
import it.unicam.quasylab.jspear.speclang.variables.Variable;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class VariableAllocation {

    private final Map<Variable, VariableAllocationData> variables = new HashMap<>();

    public boolean isAVariable(String arrayName) {
        return false;
    }

    public JSpearValue getValue(Variable variable, DataState dataState) {
        VariableAllocationData data = variables.get(variable);
        if (data == null) {
            return JSpearValue.ERROR_VALUE;
        }
        if (data.type instanceof JSpearBooleanType booleanType) {
            return booleanType.fromDouble(dataState.get(data.startingIndex));
        }
        if (data.type instanceof JSpearIntegerType integerType) {
            return integerType.fromDouble(dataState.get(data.startingIndex));
        }
        if (data.type instanceof JSpearRealType) {
            return new JSpearReal(dataState.get(data.startingIndex));
        }
        if (data.type instanceof JSpearArrayType) {
            return new JSpearArray(data.length, i -> dataState.get(data.startingIndex+i));
        }
        return JSpearValue.ERROR_VALUE;
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(Variable target, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if (allocationData == null) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> generateAssignment(allocationData, expr.eval(rg, s));
    }

    private List<DataStateUpdate> generateAssignment(VariableAllocationData allocationData, JSpearValue value) {
        if (allocationData.type instanceof JSpearArrayType) {
            if (value instanceof JSpearArray arrayValue) {
                //TODO: Manage runtime errors related to assignments with different lengths.
                return IntStream.range(0, Math.min(allocationData.length, arrayValue.lenght())).
                        mapToObj(i -> new DataStateUpdate(i+ allocationData.startingIndex, arrayValue.get(i))).toList();
            } else {
                return List.of();
            }
        } else {
            return List.of(new DataStateUpdate(allocationData.startingIndex, JSpearValue.doubleOf(value)));
        }
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(JSpearExpressionEvaluationFunction guard, Variable target, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if (allocationData == null) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> (JSpearValue.isTrue(guard.eval(rg, s))?generateAssignment(allocationData, expr.eval(rg, s)):List.of());
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(Variable target, JSpearExpressionEvaluationFunction index, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if ((allocationData == null)||!(allocationData.type instanceof JSpearArrayType)) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> generateAssignment(allocationData, index.eval(rg, s), expr.eval(rg, s));
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(JSpearExpressionEvaluationFunction guard, Variable target, JSpearExpressionEvaluationFunction index, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if ((allocationData == null)||!(allocationData.type instanceof JSpearArrayType)) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> (JSpearValue.isTrue(guard.eval(rg, s))?generateAssignment(allocationData, index.eval(rg, s), expr.eval(rg, s)):List.of());
    }

    private List<DataStateUpdate> generateAssignment(VariableAllocationData allocationData, JSpearValue index, JSpearValue value) {
        if (index instanceof JSPearInteger indexValue) {
            if (indexValue.value()<allocationData.length) {
                return List.of(new DataStateUpdate(indexValue.value()+allocationData.startingIndex, JSpearValue.doubleOf(value)));
            }
        }
        return List.of();
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(JSpearExpressionEvaluationFunction guard, Variable target, JSpearExpressionEvaluationFunction from, JSpearExpressionEvaluationFunction to, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if ((allocationData == null)||!(allocationData.type instanceof JSpearArrayType)) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> (JSpearValue.isTrue(guard.eval(rg, s))?generateAssignment(allocationData, from.eval(rg, s), to.eval(rg, s), expr.eval(rg, s)):List.of());
    }

    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(Variable target, JSpearExpressionEvaluationFunction from, JSpearExpressionEvaluationFunction to, JSpearExpressionEvaluationFunction expr) {
        VariableAllocationData allocationData = variables.get(target);
        if ((allocationData == null)||!(allocationData.type instanceof JSpearArrayType)) {
            return (rg, s) -> List.of();
        }
        return (rg, s) -> generateAssignment(allocationData, from.eval(rg, s), to.eval(rg, s), expr.eval(rg, s));
    }

    private List<DataStateUpdate> generateAssignment(VariableAllocationData allocationData, JSpearValue from, JSpearValue to,JSpearValue value) {
        if ((from instanceof JSPearInteger fromValue)
            &&(to instanceof JSPearInteger toValue)
            &&(value instanceof JSpearArray arrayValue)) {
            if ((fromValue.value()<allocationData.length)&&(toValue.value()<=allocationData.length)&&(fromValue.value()<toValue.value())) {
                return IntStream.range(0, Math.min(toValue.value()-fromValue.value(), arrayValue.lenght())).
                        mapToObj(i -> new DataStateUpdate(i+ fromValue.value()+allocationData.startingIndex, arrayValue.get(i))).toList();
            }
        }
        return List.of();
    }


    public JSpearStore getStore(DataState ds) {
        return new JSpearDataStateStore(this, ds);
    }

    public int size() {
        return variables.size();
    }

    private class VariableAllocationData {

        private final int startingIndex;

        private final int length;

        private final JSpearType type;

        private VariableAllocationData(int startingIndex, int length, JSpearType type) {
            this.startingIndex = startingIndex;
            this.length = length;
            this.type = type;
        }

    }

}
