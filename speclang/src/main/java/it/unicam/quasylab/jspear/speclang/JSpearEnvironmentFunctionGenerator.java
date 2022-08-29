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
import it.unicam.quasylab.jspear.ds.DataStateFunction;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.Function;

public class JSpearEnvironmentFunctionGenerator extends JSpearSpecificationLanguageBaseVisitor<DataStateFunction> {

    private final SymbolTable table;
    private DataStateFunction function;
    private final Function<String, JSpearValue> constants;
    private final Function<String, JSpearValue> parameters;
    private final Function<String, JSpearFunction> functions;
    private final VariableAllocation registry;
    private final SymbolTable symbolTable;
    private final HashSet<String> localVariables = new HashSet<>();
    private final HashMap<String, JSpearExpressionEvaluationFunction> localValuesGenerator = new HashMap<>();
    private final List<JSpearUpdateFunction> updates = new LinkedList<>();

    public JSpearEnvironmentFunctionGenerator(SymbolTable table, Function<String, JSpearValue> constants, Function<String, JSpearValue> parameters, Function<String, JSpearFunction> functions, VariableAllocation registry, SymbolTable symbolTable) {
        this.table = table;
        this.constants = constants;
        this.parameters = parameters;
        this.functions = functions;
        this.registry = registry;
        this.symbolTable = symbolTable;
    }

}
