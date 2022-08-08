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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class EnvironmentFunctionGenerator extends JSpearSpecificationLanguageBaseVisitor<DataStateFunction> {

    private DataStateFunction function;
    private final ToDoubleFunction<String> constants;
    private final ToDoubleFunction<String> parameters;
    private final VariableRegistry registry;
    private final SymbolTable symbolTable;

    public EnvironmentFunctionGenerator(ToDoubleFunction<String> constants, ToDoubleFunction<String> parameters, VariableRegistry registry, SymbolTable symbolTable) {
        this.constants = constants;
        this.parameters = parameters;
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
        return null;//TODO: FIX!
    }

    private Map<String, DataStateRandomExpression> generateLocalValues(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
//        RandomExpressionEvaluator evaluator = new RandomExpressionEvaluator(constants, parameters, registry, symbolTable, localValues);
        Map<String, DataStateRandomExpression> localValuesGenerator = new HashMap<>();
        //TODO: FIX!
//        for (JSpearSpecificationLanguageParser.LocalVariableContext var: ctx.localVariables) {
//            localValuesGenerator.put(var.name.getText(), var.expression().accept(evaluator));
//        }
        return localValuesGenerator;
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
