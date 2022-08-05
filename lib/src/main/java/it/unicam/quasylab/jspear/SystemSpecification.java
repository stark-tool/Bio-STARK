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

import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * This class is used to describe a JSpear specification.
 */
public class SystemSpecification {

    private final VariableRegistry variableRegistry;

    private final Map<String, Supplier<ControlledSystem>> systemRegistry;

    private final Map<String, Supplier<DataStateExpression>> penaltyFunctions;

    public SystemSpecification(VariableRegistry variableRegistry, Map<String, Supplier<ControlledSystem>> systemRegistry, Map<String, Supplier<DataStateExpression>> penaltyFunctions) {
        this.variableRegistry = variableRegistry;
        this.systemRegistry = systemRegistry;
        this.penaltyFunctions = penaltyFunctions;
    }


    public VariableRegistry getVariableRegistry() {
        return variableRegistry;
    }

    public ControlledSystem getControlledSystem(String name) {
        Supplier<ControlledSystem> supplier = systemRegistry.get(name);
        if (supplier != null) {
            return supplier.get();
        } else {
            throw new IllegalArgumentException("Unknown system: "+name);
        }
    }

    public ToDoubleFunction<DataState> getPenaltyFunction(String name) {
        Supplier<DataStateExpression> supplier = penaltyFunctions.get(name);
        if (supplier != null) {
            return supplier.get();
        } else {
            throw new IllegalArgumentException("Unknown penalty function: "+name);
        }
    }

}
