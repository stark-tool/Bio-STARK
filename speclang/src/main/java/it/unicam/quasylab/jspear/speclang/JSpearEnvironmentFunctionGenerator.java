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

import it.unicam.quasylab.jspear.*;
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
    private final VariableRegistry registry;
    private final SymbolTable symbolTable;
    private final HashSet<String> localVariables = new HashSet<>();
    private final HashMap<String, JSpearExpressionEvaluationFunction> localValuesGenerator = new HashMap<>();
    private final List<JSpearUpdateFunction> updates = new LinkedList<>();

    public JSpearEnvironmentFunctionGenerator(SymbolTable table, Function<String, JSpearValue> constants, Function<String, JSpearValue> parameters, Function<String, JSpearFunction> functions, VariableRegistry registry, SymbolTable symbolTable) {
        this.table = table;
        this.constants = constants;
        this.parameters = parameters;
        this.functions = functions;
        this.registry = registry;
        this.symbolTable = symbolTable;
    }


    @Override
    public DataStateFunction visitJSpearSpecificationModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return function;
    }

    @Override
    public DataStateFunction visitEnvironmentDeclaration(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
        generateLocalValues(ctx);
        generateUpdateFunctions(ctx);
        function = generateDataStateFunction();
        return function;
    }

    private DataStateFunction generateDataStateFunction() {
        return (rg, ds) -> {
            Map<String, JSpearValue> localValues = evalLocalValues(rg, new HashMap<>(), ds);
            return ds.set(updates.stream().map(u -> u.eval(rg, localValues, ds)).filter(Objects::nonNull).toList());
        };
    }

    private Map<String, JSpearValue> evalLocalValues(RandomGenerator rg, Map<String, JSpearValue> localValues, DataState ds) {
        for (Map.Entry<String, JSpearExpressionEvaluationFunction> lv: localValuesGenerator.entrySet()) {
            localValues.put(lv.getKey(), lv.getValue().eval(rg, ds));
        }
        return localValues;
    }

    private void generateUpdateFunctions(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
        for (JSpearSpecificationLanguageParser.VariableAssignmentContext variableUpdate: ctx.assignments) {
            generateUpdateFunction(variableUpdate);
        }
    }

    private void generateUpdateFunction(JSpearSpecificationLanguageParser.VariableAssignmentContext variableUpdate) {
        JSpearExpressionEvaluationFunction guardFunction = null;
        if (variableUpdate.guard != null) {
            guardFunction = variableUpdate.guard.accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables));
        }
        String name = variableUpdate.target.name.getText();
        boolean isArray = table.getTypeOf(name).isAnArray();
        JSpearExpressionEvaluationFunction firstTargetIndex = null;
        if (variableUpdate.target.first != null) {
            firstTargetIndex = variableUpdate.target.first.accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables));
        }
        JSpearExpressionEvaluationFunction lastTargetIndex = null;
        if (variableUpdate.target.first != null) {
            lastTargetIndex = variableUpdate.target.last.accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables));
        }
        JSpearExpressionEvaluationFunction newValue = variableUpdate.value.accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables));
        updates.add(new JSpearUpdateFunction(isArray, registry.getVariable(name), guardFunction, firstTargetIndex, lastTargetIndex, newValue ));
    }

    private void generateLocalValues(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
        for (JSpearSpecificationLanguageParser.LocalVariableContext localVariable: ctx.localVariables) {
            String name = localVariable.name.getText();
            localVariables.add(name);
            localValuesGenerator.put(name, localVariable.expression().accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables)));
        }
    }


    @Override
    protected DataStateFunction defaultResult() {
        return function;
    }

    @Override
    protected DataStateFunction aggregateResult(DataStateFunction aggregate, DataStateFunction nextResult) {
        return function;
    }
}
