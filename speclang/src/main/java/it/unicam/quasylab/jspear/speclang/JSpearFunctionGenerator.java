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

import it.unicam.quasylab.jspear.speclang.values.JSpearArrayElementPredicate;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;
import it.unicam.quasylab.jspear.speclang.variables.JSpearExpressionEvaluationContext;
import it.unicam.quasylab.jspear.speclang.variables.JSpearLetContextDataStore;
import it.unicam.quasylab.jspear.speclang.variables.JSpearNameResolver;
import it.unicam.quasylab.jspear.speclang.variables.Variable;

import java.util.Map;
import java.util.stream.Collectors;

public class JSpearFunctionGenerator extends JSpearSpecificationLanguageBaseVisitor<JSpearExpressionEvaluationFunction> {

    private final JSpearExpressionEvaluationContext context;

    private final JSpearNameResolver resolver;


    public JSpearFunctionGenerator(JSpearExpressionEvaluationContext context, JSpearNameResolver resolver) {
        this.context = context;
        this.resolver = resolver;
    }

    @Override
    public JSpearExpressionEvaluationFunction visitSwitchStatement(JSpearSpecificationLanguageParser.SwitchStatementContext ctx) {
        JSpearExpressionEvaluationFunction valueEvaluation = getExpressionEvaluationFunction(ctx.value);
        Map<JSpearValue, JSpearExpressionEvaluationFunction> cases = ctx.switchCases.stream().collect(Collectors.toMap(c -> context.get(c.name.getText()), c -> c.bpdy.accept(this)));
        JSpearExpressionEvaluationFunction defaultCase = (ctx.defaultStatement!=null?ctx.defaultStatement.accept(this):JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE));
        return (rg, s) -> {
            JSpearValue value = valueEvaluation.eval(rg, s);
            JSpearExpressionEvaluationFunction selected = cases.getOrDefault(value, defaultCase);
            return selected.eval(rg, s);
        };
    }

    @Override
    public JSpearExpressionEvaluationFunction visitIfThenElseStatement(JSpearSpecificationLanguageParser.IfThenElseStatementContext ctx) {
        JSpearExpressionEvaluationFunction guardEvaluation = getExpressionEvaluationFunction(ctx.guard);
        JSpearExpressionEvaluationFunction thenEvaluation = ctx.thenStatement.accept(this);
        JSpearExpressionEvaluationFunction elseEvaluation = ctx.elseStatement.accept(this);
        return (rg, s) -> JSpearValue.ifThenElse(guardEvaluation.eval(rg, s), () -> thenEvaluation.eval(rg, s), () -> elseEvaluation.eval(rg, s));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitReturnStatement(JSpearSpecificationLanguageParser.ReturnStatementContext ctx) {
        return getExpressionEvaluationFunction(ctx.expression());
    }

    private JSpearExpressionEvaluationFunction getExpressionEvaluationFunction(JSpearSpecificationLanguageParser.ExpressionContext expression) {
        return expression.accept(new JSpearExpressionEvaluator(context, resolver));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitFunctionBlock(JSpearSpecificationLanguageParser.FunctionBlockContext ctx) {
        return ctx.functionStatement().accept(this);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitLetStatement(JSpearSpecificationLanguageParser.LetStatementContext ctx) {
        Variable variable = resolver.getOrRegister(ctx.name.getText());
        if (variable != null) {
            JSpearExpressionEvaluationFunction expressionEvaluation = getExpressionEvaluationFunction(ctx.value);
            JSpearExpressionEvaluationFunction bodyEvaluation = ctx.body.accept(this);
            return (rg, s) -> {
                JSpearValue value = expressionEvaluation.eval(rg, s);
                return bodyEvaluation.eval(rg, new JSpearLetContextDataStore(variable, value, s));
            };
        } else {
            return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
        }
    }
}
