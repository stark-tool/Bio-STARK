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

import it.unicam.quasylab.jspear.Variable;
import it.unicam.quasylab.jspear.VariableRegistry;
import it.unicam.quasylab.jspear.speclang.types.JSpearType;
import it.unicam.quasylab.jspear.speclang.values.*;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class JSpearLambdaExpressionEvaluator extends JSpearSpecificationLanguageBaseVisitor<JSpearLambdaExpressionEvaluationFunction> {


    private final static Map<String, BiFunction<JSpearValue, JSpearValue, JSpearValue>> binaryOperators = Map.of(
            "+", JSpearValue::sum,
            "*", JSpearValue::product,
            "-", JSpearValue::subtraction,
            "/", JSpearValue::division, //TODO: Check how to handle division by zero!
            "%", JSpearValue::modulo,
            "atan2", (x,y) -> x.apply(Math::atan2, y),
            "hypot", (x,y) -> x.apply(Math::hypot, y),
            "max", (x,y) -> x.apply(Math::max, y),
            "min", (x,y) -> x.apply(Math::min, y),
            "pow",   (x,y) -> x.apply(Math::pow, y)
    );

    private final static Map<String, DoubleUnaryOperator> unaryOperators = Map.ofEntries(
            Map.entry("+", x -> +x),
            Map.entry("-", x -> -x),
            Map.entry("acos", Math::acos),
            Map.entry("asin", Math::asin),
            Map.entry("atan", Math::atan),
            Map.entry("cbrt", Math::cbrt),
            Map.entry("ceil", Math::ceil),
            Map.entry("cos", Math::cos),
            Map.entry("cosh", Math::cosh),
            Map.entry("exp", Math::exp),
            Map.entry("expm1", Math::expm1),
            Map.entry("floor", Math::floor),
            Map.entry("log", Math::log),
            Map.entry("log10", Math::log10),
            Map.entry("log1p", Math::log1p),
            Map.entry("signum", Math::signum),
            Map.entry("sin", Math::sin),
            Map.entry("sinh", Math::sinh),
            Map.entry("sqrt", Math::sqrt),
            Map.entry("tan", Math::tan)
    );

    private final Function<String, JSpearValue> constants;

    private final Function<String, JSpearValue> parameters;
    private final SymbolTable table;
    private final VariableRegistry registry;
    private final Set<String> localVariables;


    public JSpearLambdaExpressionEvaluator(SymbolTable table, Function<String, JSpearValue> constants, Function<String, JSpearValue> parameters, VariableRegistry registry, Set<String> localVariables) {
        this.table = table;
        this.constants = constants;
        this.parameters = parameters;
        this.registry = registry;
        this.localVariables = localVariables;
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        JSpearLambdaExpressionEvaluationFunction arg = ctx.arg.accept(this);
        return (rg, lv, ds, v) -> arg.eval(rg, lv, ds, v).negate();
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return evalBinary((x,y) -> x.apply(Math::pow, y), ctx.left, ctx.right);
    }

    private JSpearLambdaExpressionEvaluationFunction evalBinary(BiFunction<JSpearValue, JSpearValue, JSpearValue> op, JSpearSpecificationLanguageParser.ExpressionContext firstArgument, JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        JSpearLambdaExpressionEvaluationFunction firstArgumentEvaluation = firstArgument.accept(this);
        JSpearLambdaExpressionEvaluationFunction secondArgumentEvaluation = secondArgument.accept(this);
        return (rg, lv, ds, v) -> op.apply(firstArgumentEvaluation.eval(rg,lv,ds, v), secondArgumentEvaluation.eval(rg, lv, ds, v));
    }


    @Override
    public JSpearLambdaExpressionEvaluationFunction visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.binaryMathFunction().start.getText()), ctx.left, ctx.right);
    }

    private BiFunction<JSpearValue, JSpearValue, JSpearValue> getBinaryOperator(String op) {
        return binaryOperators.getOrDefault(op, (x,y) -> JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearBoolean.TRUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        return evalBinary(getRelationOperator(ctx.op.getText()),ctx.left, ctx.right);
    }

    private BiFunction<JSpearValue, JSpearValue, JSpearValue> getRelationOperator(String op) {
        return switch (op) {
            case "<" -> JSpearValue::isLessThan;
            case "<=" -> JSpearValue::isLessOrEqualThan;
            case "==" -> JSpearValue::isEqualTo;
            case ">=" -> JSpearValue::isGreaterOrEqualThan;
            case ">" -> JSpearValue::isGreaterThan;
            default -> (x, y) -> JSpearValue.ERROR_VALUE;
        };
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearBoolean.FALSE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        return evalBinary(JSpearValue::and, ctx.left, ctx.right);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return (rg, lv, ds, v) -> v;
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.unaryMathFunction().start.getText()), ctx.argument);
    }

    private JSpearLambdaExpressionEvaluationFunction evalUnary(DoubleUnaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext argument) {
        JSpearLambdaExpressionEvaluationFunction argumentEvaluator = argument.accept(this);
        return (rg, lv, ds, v) -> argumentEvaluator.eval(rg, lv, ds, v).apply(op);
    }


    @Override
    public JSpearLambdaExpressionEvaluationFunction visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.op.getText()), ctx.arg);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        JSpearLambdaExpressionEvaluationFunction targetEvaluation = getTargetEvaluationFunction(name);
        if (ctx.first == null) {
            return targetEvaluation;
        }
        JSpearLambdaExpressionEvaluationFunction firstEvaluation = ctx.first.accept(this);
        if (ctx.last == null) {
            return (rg, lv, ds, v) -> targetEvaluation.eval(rg, lv, ds, v).select(firstEvaluation.eval(rg, lv, ds, v));
        }
        JSpearLambdaExpressionEvaluationFunction lastEvaluation = ctx.last.accept(this);
        return (rg, lv, ds, v) -> targetEvaluation.eval(rg, lv, ds, v).select(firstEvaluation.eval(rg, lv, ds, v), lastEvaluation.eval(rg, lv, ds, v));
    }

    private JSpearLambdaExpressionEvaluationFunction getTargetEvaluationFunction(String name) {
        if (table.isAConstant(name)) {
            JSpearValue value = constants.apply(name);
            return JSpearLambdaExpressionEvaluationFunction.of(value);
        }
        if (table.isAParameter(name)) {
            JSpearValue value = constants.apply(name);
            return JSpearLambdaExpressionEvaluationFunction.of(value);
        }
        if (localVariables.contains(name)) {
            return (rg, lv, ds, v) -> lv.get(name);
        }
        if (table.isAVariable(name)) {
            Variable variable = registry.getVariable(name);
            JSpearType type = table.getTypeOf(name);
            return (rg, lv, ds, v) -> JSpearValue.of(type, variable, ds);
        }
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }


    @Override
    public JSpearLambdaExpressionEvaluationFunction visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        JSpearValue value = new JSPearInteger(Integer.parseInt(ctx.getText()));
        return JSpearLambdaExpressionEvaluationFunction.of(value);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        JSpearLambdaExpressionEvaluationFunction meanEvaluation = ctx.mean.accept(this);
        JSpearLambdaExpressionEvaluationFunction varianceEvaluation = ctx.variance.accept(this);
        return (rg, lv, ds, v) -> JSpearReal.sampleNormal(rg, meanEvaluation.eval(rg, lv, ds, v), varianceEvaluation.eval(rg, lv, ds, v));
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        JSpearLambdaExpressionEvaluationFunction[] elements = ctx.expression().stream().map(e -> e.accept(this)).toArray(JSpearLambdaExpressionEvaluationFunction[]::new);
        return (rg, lv, ds, v) -> {
            int selected = rg.nextInt(elements.length);
            return elements[selected].eval(rg, lv, ds, v);
        };
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        JSpearLambdaExpressionEvaluationFunction leftEvaluation = ctx.left.accept(this);
        JSpearLambdaExpressionEvaluationFunction rightEvaluation = ctx.right.accept(this);
        return (rg, lv, ds, v) -> leftEvaluation.eval(rg, lv, ds, v).or(rightEvaluation.eval(rg, lv, ds, v));
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        JSpearLambdaExpressionEvaluationFunction guardEvaluation = ctx.guard.accept(this);
        JSpearLambdaExpressionEvaluationFunction thenEvaluation = ctx.thenBranch.accept(this);
        JSpearLambdaExpressionEvaluationFunction elseEvaluation = ctx.elseBranch.accept(this);
        return (rg, lv, ds, v) -> {
            if (guardEvaluation.eval(rg, lv, ds, v).booleanOf()) {
                return thenEvaluation.eval(rg, lv, ds, v);
            } else {
                return elseEvaluation.eval(rg, lv, ds, v);
            }
        };
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        JSpearValue value = new JSpearReal(Double.parseDouble(ctx.getText()));
        return JSpearLambdaExpressionEvaluationFunction.of(value);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        //TODO: FIXME!
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        if (ctx.from == null) {
            return (rg, lv, ds, v) -> new JSpearReal(rg.nextDouble());
        } else {
            JSpearLambdaExpressionEvaluationFunction fromEvaluation = ctx.from.accept(this);
            JSpearLambdaExpressionEvaluationFunction toEvaluation = ctx.to.accept(this);
            return (rg, lv, ds, v) -> JSpearReal.sample(rg, fromEvaluation.eval(rg, lv, ds, v), toEvaluation.eval(rg, lv, ds, v));
        }
    }

    @Override
    public JSpearLambdaExpressionEvaluationFunction visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        return JSpearLambdaExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }


}
