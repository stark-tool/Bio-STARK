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

import it.unicam.quasylab.jspear.VariableRegistry;
import it.unicam.quasylab.jspear.speclang.types.JSpearType;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class JSpearFunctionGenerator extends JSpearSpecificationLanguageBaseVisitor<JSpearFunction> {

    private final Function<String, JSpearValue> constants;

    private final Function<String, JSpearValue> parameters;

    private final Function<String, JSpearFunction> functions;

    private final Map<String, Integer> arguments;

    private final SymbolTable table;

    private final VariableRegistry registry;

    private final Set<String> localVariables;

    public JSpearFunctionGenerator(Function<String, JSpearValue> constants, Function<String, JSpearValue> parameters, Function<String, JSpearFunction> functions, Map<String, Integer> arguments, SymbolTable table, VariableRegistry registry, Set<String> localVariables) {
        this.constants = constants;
        this.parameters = parameters;
        this.functions = functions;
        this.arguments = arguments;
        this.table = table;
        this.registry = registry;
        this.localVariables = localVariables;
    }

    @Override
    public JSpearFunction visitSwitchStatement(JSpearSpecificationLanguageParser.SwitchStatementContext ctx) {
        //TODO: Complete!
        return (rg, lv) -> JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearFunction visitCaseStatement(JSpearSpecificationLanguageParser.CaseStatementContext ctx) {
        //TODO: Complete!
        return (rg, lv) -> JSpearValue.ERROR_VALUE;
    }

    @Override
    public JSpearFunction visitIfThenElseStatement(JSpearSpecificationLanguageParser.IfThenElseStatementContext ctx) {
        JSpearExpressionEvaluationFunction guardEvaluation = getExpressionEvaluationFunction(ctx.guard);
        JSpearFunction thenEvaluation = ctx.thenStatement.accept(this);
        JSpearFunction elseEvaluation = ctx.elseStatement.accept(this);
        return (rg, args) -> {
            if (guardEvaluation.eval(rg, args).booleanOf()) {
                return thenEvaluation.apply(rg, args);
            } else {
                return elseEvaluation.apply(rg, args);
            }
        };
    }

    @Override
    public JSpearFunction visitReturnStatement(JSpearSpecificationLanguageParser.ReturnStatementContext ctx) {
        JSpearExpressionEvaluationFunction expressionEvaluator = getExpressionEvaluationFunction(ctx.expression());
        return expressionEvaluator::eval;
    }

    private JSpearExpressionEvaluationFunction getExpressionEvaluationFunction(JSpearSpecificationLanguageParser.ExpressionContext expression) {
        return expression.accept(new JSpearExpressionEvaluator(table, constants, parameters, functions, registry, localVariables));
    }

    @Override
    public JSpearFunction visitFunctionBlock(JSpearSpecificationLanguageParser.FunctionBlockContext ctx) {
        return ctx.functionStatement().accept(this);
    }
}
