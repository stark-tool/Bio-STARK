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

package it.unicam.quasylab.jspear.speclang.semantics;

import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageBaseVisitor;
import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageParser;
import it.unicam.quasylab.jspear.speclang.variables.JSpearExpressionEvaluationContext;
import it.unicam.quasylab.jspear.speclang.variables.JSpearLetContextDataStore;
import it.unicam.quasylab.jspear.speclang.variables.JSpearNameResolver;
import it.unicam.quasylab.jspear.speclang.variables.Variable;
import it.unicam.quasylab.jspear.speclang.values.*;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JSpearExpressionEvaluator extends JSpearSpecificationLanguageBaseVisitor<JSpearExpressionEvaluationFunction> {


    private final static Map<String, BiFunction<JSpearValue, JSpearValue, JSpearValue>> binaryOperators = Map.of(
            "+", JSpearValue::sum,
            "*", JSpearValue::product,
            "-", JSpearValue::subtraction,
            "/", JSpearValue::division, //TODO: Check how to handle division by zero!
            "%", JSpearValue::modulo,
            "atan2", (x,y) -> JSpearValue.apply(Math::atan2, x, y),
            "hypot", (x,y) -> JSpearValue.apply(Math::hypot, x, y),
            "max", (x,y) -> JSpearValue.apply(Math::max, x, y),
            "min", (x,y) -> JSpearValue.apply(Math::min, x, y),
            "pow",   (x,y) -> JSpearValue.apply(Math::pow, x, y)
    );

    private final static Map<String, DoubleUnaryOperator> unaryOperators = Map.ofEntries(
            Map.entry("+", x -> +x),
            Map.entry("-", x -> -x),
            Map.entry("abs", Math::abs),
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

    private final JSpearExpressionEvaluationContext context;

    private final JSpearNameResolver registry;


    public JSpearExpressionEvaluator(JSpearExpressionEvaluationContext context, JSpearNameResolver registry) {
        this.context = context;
        this.registry = registry;
    }

    @Override
    public JSpearExpressionEvaluationFunction visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        JSpearExpressionEvaluationFunction arg = ctx.arg.accept(this);
        return (rg, s) -> JSpearValue.negate(arg.eval(rg, s));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        JSpearExpressionEvaluationFunction leftEvaluation = ctx.left.accept(this);
        JSpearExpressionEvaluationFunction rightEvaluation = ctx.right.accept(this);
        return (rg, s) -> JSpearValue.apply(Math::pow, leftEvaluation.eval(rg, s), rightEvaluation.eval(rg, s));
    }

    private JSpearExpressionEvaluationFunction evalBinary(BiFunction<JSpearValue, JSpearValue, JSpearValue> op, JSpearSpecificationLanguageParser.ExpressionContext firstArgument, JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        JSpearExpressionEvaluationFunction firstArgumentEvaluation = firstArgument.accept(this);
        JSpearExpressionEvaluationFunction secondArgumentEvaluation = secondArgument.accept(this);
        return (rg, s) -> op.apply(firstArgumentEvaluation.eval(rg, s), secondArgumentEvaluation.eval(rg, s));
    }


    @Override
    public JSpearExpressionEvaluationFunction visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.binaryMathFunction().start.getText()), ctx.left, ctx.right);
    }

    private BiFunction<JSpearValue, JSpearValue, JSpearValue> getBinaryOperator(String op) {
        return binaryOperators.getOrDefault(op, (x,y) -> JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return JSpearExpressionEvaluationFunction.of( JSpearBoolean.TRUE );
    }

    public JSpearExpressionEvaluationFunction evaluateArrayExpressions(String arrayName,
                                                                       JSpearSpecificationLanguageParser.ExpressionContext guard,
                                                                       BiFunction<JSpearValue, JSpearValue, JSpearValue> guardSelectionFunction
    ) {
        JSpearExpressionEvaluationFunction lambdaEvaluator = (guard==null?JSpearExpressionEvaluationFunction.of(JSpearArrayElementPredicate.TRUE):guard.accept(this));
        if (context.isDefined(arrayName)) {
            JSpearValue arrayValue = context.get(arrayName);
            return (rg, s) -> guardSelectionFunction.apply(arrayValue, lambdaEvaluator.eval(rg, s));
        }
        if (registry.isDeclared(arrayName)) {
            Variable variable = registry.get(arrayName);
            return (rg, s) -> guardSelectionFunction.apply(s.get(variable), lambdaEvaluator.eval(rg, s));
        }
        return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::maxElement);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
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
    public JSpearExpressionEvaluationFunction visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return JSpearExpressionEvaluationFunction.of(JSpearBoolean.FALSE);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        return evalBinary(JSpearValue::and, ctx.left, ctx.right);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return (rg, s) -> new JSpearArrayElementSelectionFunction();
    }

    @Override
    public JSpearExpressionEvaluationFunction visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.unaryMathFunction().start.getText()), ctx.argument);
    }

    private JSpearExpressionEvaluationFunction evalUnary(DoubleUnaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext argument) {
        JSpearExpressionEvaluationFunction argumentEvaluator = argument.accept(this);
        return (rg, s) -> JSpearValue.apply(op, argumentEvaluator.eval(rg, s));
    }


    @Override
    public JSpearExpressionEvaluationFunction visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.op.getText()), ctx.arg);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        JSpearExpressionEvaluationFunction[] elementEvaluators = ctx.elements.stream().sequential().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
        return (rg, s) -> new JSpearArray(Stream.of(elementEvaluators).map(e -> e.eval(rg, s)).toArray(JSpearValue[]::new));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        JSpearExpressionEvaluationFunction targetEvaluation = getTargetEvaluationFunction(name);
        if (ctx.first == null) {
            return targetEvaluation;
        }
        JSpearExpressionEvaluationFunction firstEvaluation = ctx.first.accept(this);
        if (ctx.last == null) {
            return (rg, s) -> JSpearValue.select(targetEvaluation.eval(rg, s), firstEvaluation.eval(rg, s));
        }
        JSpearExpressionEvaluationFunction lastEvaluation = ctx.last.accept(this);
        return (rg, s) -> JSpearValue.select(targetEvaluation.eval(rg, s), firstEvaluation.eval(rg, s), lastEvaluation.eval(rg, s));
    }

    private JSpearExpressionEvaluationFunction getTargetEvaluationFunction(String name) {
        if (context.isDefined(name)) {
            return JSpearExpressionEvaluationFunction.of(context.get(name));
        }
        if (registry.isDeclared(name)) {
            Variable variable = registry.get(name);
            return (rg, s) -> s.get(variable);
        }
        return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }


    @Override
    public JSpearExpressionEvaluationFunction visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        JSpearValue value = new JSPearInteger(Integer.parseInt(ctx.getText()));
        return JSpearExpressionEvaluationFunction.of(value);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        JSpearExpressionEvaluationFunction meanEvaluation = ctx.mean.accept(this);
        JSpearExpressionEvaluationFunction varianceEvaluation = ctx.variance.accept(this);
        return (rg, s) -> JSpearValue.sampleNormal(rg, meanEvaluation.eval(rg, s), varianceEvaluation.eval(rg, s));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        JSpearExpressionEvaluationFunction[] elements = ctx.expression().stream().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
        return (rg, s) -> {
            int selected = rg.nextInt(elements.length);
            return elements[selected].eval(rg, s);
        };
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::meanElement);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        JSpearExpressionEvaluationFunction leftEvaluation = ctx.left.accept(this);
        JSpearExpressionEvaluationFunction rightEvaluation = ctx.right.accept(this);
        return (rg, s) -> JSpearValue.or( leftEvaluation.eval(rg, s), rightEvaluation.eval(rg, s));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        JSpearExpressionEvaluationFunction guardEvaluation = ctx.guard.accept(this);
        JSpearExpressionEvaluationFunction thenEvaluation = ctx.thenBranch.accept(this);
        JSpearExpressionEvaluationFunction elseEvaluation = ctx.elseBranch.accept(this);
        return (rg, s) -> JSpearValue.ifThenElse(guardEvaluation.eval(rg, s), () -> thenEvaluation.eval(rg, s), () -> elseEvaluation.eval(rg, s));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        JSpearValue value = new JSpearReal(Double.parseDouble(ctx.getText()));
        return JSpearExpressionEvaluationFunction.of(value);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        String functionName = ctx.name.getText();
        if (context.isAFunction(functionName)) {
            JSpearExpressionEvaluationFunction[] arguments = ctx.callArguments.stream().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
            JSpearFunction function = context.getFunction(functionName);
            return (rg, s) -> function.apply(rg, Arrays.stream(arguments).map(e -> e.eval(rg, s)).toArray(JSpearValue[]::new) );
        } else {
            return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
        }
    }


    @Override
    public JSpearExpressionEvaluationFunction visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::count);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        if (ctx.from == null) {
            return (rg, s) -> new JSpearReal(rg.nextDouble());
        } else {
            JSpearExpressionEvaluationFunction fromEvaluation = ctx.from.accept(this);
            JSpearExpressionEvaluationFunction toEvaluation = ctx.to.accept(this);
            return (rg, s) -> JSpearValue.sample(rg, fromEvaluation.eval(rg, s), toEvaluation.eval(rg, s));
        }
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::minElement);
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
        return expression.accept(new JSpearExpressionEvaluator(context, registry));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitFunctionBlock(JSpearSpecificationLanguageParser.FunctionBlockContext ctx) {
        return ctx.functionStatement().accept(this);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitLetStatement(JSpearSpecificationLanguageParser.LetStatementContext ctx) {
        Variable variable = registry.getOrRegister(ctx.name.getText());
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
