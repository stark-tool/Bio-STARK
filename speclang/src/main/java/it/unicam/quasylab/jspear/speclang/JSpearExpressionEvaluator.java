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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JSpearExpressionEvaluator extends JSpearSpecificationLanguageBaseVisitor<JSpearExpressionEvaluationFunction> {




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

    private final Function<String, JSpearFunction> functions;

    private final SymbolTable table;
    private final VariableRegistry registry;
    private final Set<String> localVariables;


    public JSpearExpressionEvaluator(SymbolTable table, Function<String, JSpearValue> constants, Function<String, JSpearValue> parameters, Function<String, JSpearFunction> functions, VariableRegistry registry, Set<String> localVariables) {
        this.table = table;
        this.constants = constants;
        this.parameters = parameters;
        this.functions = functions;
        this.registry = registry;
        this.localVariables = localVariables;
    }

    @Override
    public JSpearExpressionEvaluationFunction visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        JSpearExpressionEvaluationFunction arg = ctx.arg.accept(this);
        return (rg, lv, ds) -> arg.eval(rg, lv, ds).negate();
    }

    @Override
    public JSpearExpressionEvaluationFunction visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return evalBinary((x,y) -> x.apply(Math::pow, y), ctx.left, ctx.right);
    }

    private JSpearExpressionEvaluationFunction evalBinary(BiFunction<JSpearValue, JSpearValue, JSpearValue> op, JSpearSpecificationLanguageParser.ExpressionContext firstArgument, JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        JSpearExpressionEvaluationFunction firstArgumentEvaluation = firstArgument.accept(this);
        JSpearExpressionEvaluationFunction secondArgumentEvaluation = secondArgument.accept(this);
        return (rg, lv, ds) -> op.apply(firstArgumentEvaluation.eval(rg,lv,ds), secondArgumentEvaluation.eval(rg, lv, ds));
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
        return (rg, lv, ds) -> JSpearBoolean.TRUE;
    }

    public JSpearExpressionEvaluationFunction evaluateArrayExpressions(String arrayName,
                                                                       JSpearSpecificationLanguageParser.ExpressionContext guard,
                                                                       BiFunction<JSpearValue, Predicate<JSpearValue>, JSpearValue> guardSelectionFunction,
                                                                       Function<JSpearValue, JSpearValue> selectionFunction
    ) {
        if (guard != null) {
            JSpearLambdaExpressionEvaluator lambdaEvaluator = new JSpearLambdaExpressionEvaluator(table, constants, parameters, functions, registry, localVariables);
            JSpearLambdaExpressionEvaluationFunction selection = guard.accept(lambdaEvaluator);
            if (localVariables.contains(arrayName)) {
                return (rg, lv, ds) -> guardSelectionFunction.apply(lv.get(arrayName), selection.generatePredicate(rg, lv, ds));
            }
            if (table.isAConstant(arrayName)) {
                JSpearValue cValue = constants.apply(arrayName);
                return (rg, lv, ds) -> guardSelectionFunction.apply(cValue, selection.generatePredicate(rg, lv, ds));
            }
            if (table.isAVariable(arrayName)) {
                Variable variable = registry.getVariable(arrayName);
                JSpearType type = table.getTypeOf(arrayName);
                return (rg, lv, ds) -> guardSelectionFunction.apply(JSpearValue.of(type, variable, ds), selection.generatePredicate(rg, lv, ds));
            }
        } else {
            if (localVariables.contains(arrayName)) {
                return (rg, lv, ds) -> selectionFunction.apply(lv.get(arrayName));
            }
            if (table.isAConstant(arrayName)) {
                JSpearValue cValue = constants.apply(arrayName);
                return (rg, lv, ds) -> selectionFunction.apply(cValue);
            }
            if (table.isAVariable(arrayName)) {
                Variable variable = registry.getVariable(arrayName);
                JSpearType type = table.getTypeOf(arrayName);
                return (rg, lv, ds) -> selectionFunction.apply(JSpearValue.of(type, variable, ds));
            }
        }
        return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::maxElement, JSpearValue::maxElement);
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
        return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.unaryMathFunction().start.getText()), ctx.argument);
    }

    private JSpearExpressionEvaluationFunction evalUnary(DoubleUnaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext argument) {
        JSpearExpressionEvaluationFunction argumentEvaluator = argument.accept(this);
        return (rg, lv, ds) -> argumentEvaluator.eval(rg, lv, ds).apply(op);
    }


    @Override
    public JSpearExpressionEvaluationFunction visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.op.getText()), ctx.arg);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        JSpearExpressionEvaluationFunction[] elementEvaluators = ctx.elements.stream().sequential().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
        return (rg, lv, ds) -> new JSpearArray(Stream.of(elementEvaluators).map(e -> e.eval(rg, lv, ds)).toArray(JSpearValue[]::new));
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
            return (rg, lv, ds) -> targetEvaluation.eval(rg, lv, ds).select(firstEvaluation.eval(rg, lv, ds));
        }
        JSpearExpressionEvaluationFunction lastEvaluation = ctx.last.accept(this);
        return (rg, lv, ds) -> targetEvaluation.eval(rg, lv, ds).select(firstEvaluation.eval(rg, lv, ds), lastEvaluation.eval(rg, lv, ds));
    }

    private JSpearExpressionEvaluationFunction getTargetEvaluationFunction(String name) {
        if (table.isAConstant(name)) {
            JSpearValue value = constants.apply(name);
            return JSpearExpressionEvaluationFunction.of(value);
        }
        if (table.isAParameter(name)) {
            JSpearValue value = parameters.apply(name);
            return JSpearExpressionEvaluationFunction.of(value);
        }
        if (localVariables.contains(name)) {
            return (rg, lv, ds) -> lv.get(name);
        }
        if (table.isAVariable(name)) {
            Variable variable = registry.getVariable(name);
            JSpearType type = table.getTypeOf(name);
            return (rg, lv, ds) -> JSpearValue.of(type, variable, ds);
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
        return (rg, lv, ds) -> JSpearReal.sampleNormal(rg, meanEvaluation.eval(rg, lv, ds), varianceEvaluation.eval(rg, lv, ds));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        JSpearExpressionEvaluationFunction[] elements = ctx.expression().stream().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
        return (rg, lv, ds) -> {
            int selected = rg.nextInt(elements.length);
            return elements[selected].eval(rg, lv, ds);
        };
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::meanElement, JSpearValue::meanElement);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        JSpearExpressionEvaluationFunction leftEvaluation = ctx.left.accept(this);
        JSpearExpressionEvaluationFunction rightEvaluation = ctx.right.accept(this);
        return (rg, lv, ds) -> leftEvaluation.eval(rg, lv, ds).or(rightEvaluation.eval(rg, lv, ds));
    }

    @Override
    public JSpearExpressionEvaluationFunction visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        JSpearExpressionEvaluationFunction guardEvaluation = ctx.guard.accept(this);
        JSpearExpressionEvaluationFunction thenEvaluation = ctx.thenBranch.accept(this);
        JSpearExpressionEvaluationFunction elseEvaluation = ctx.elseBranch.accept(this);
        return (rg, lv, ds) -> {
            if (guardEvaluation.eval(rg, lv, ds).booleanOf()) {
                return thenEvaluation.eval(rg, lv, ds);
            } else {
                return elseEvaluation.eval(rg, lv, ds);
            }
        };
    }

    @Override
    public JSpearExpressionEvaluationFunction visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        JSpearValue value = new JSpearReal(Double.parseDouble(ctx.getText()));
        return JSpearExpressionEvaluationFunction.of(value);
    }

    @Override
    public JSpearExpressionEvaluationFunction visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        String functionName = ctx.name.getText();
        if (table.isAFunction(functionName)) {
            String[] argNames = table.getFunctionDeclaration(functionName).arguments.stream().map(a -> a.name.getText()).toArray(String[]::new);
            JSpearExpressionEvaluationFunction[] arguments = ctx.callArguments.stream().map(e -> e.accept(this)).toArray(JSpearExpressionEvaluationFunction[]::new);
            Map<String, JSpearExpressionEvaluationFunction> evaluationMap = generateArgumentsMap(argNames, arguments);
            JSpearFunction function = functions.apply(functionName);
            return (rg, lv, ds) -> {
                Map<String, JSpearValue> actualArguments = evaluationMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().eval(rg, lv, ds)));
                return function.apply(rg, actualArguments);
            };
        } else {
            return JSpearExpressionEvaluationFunction.of(JSpearValue.ERROR_VALUE);
        }
    }

    private Map<String, JSpearExpressionEvaluationFunction> generateArgumentsMap(String[] argNames, JSpearExpressionEvaluationFunction[] arguments) {
        HashMap<String, JSpearExpressionEvaluationFunction> toReturn = new HashMap<>();
        for(int i=0; i<argNames.length; i++) {
            toReturn.put(argNames[i], arguments[i]);
        }
        return toReturn;
    }

    @Override
    public JSpearExpressionEvaluationFunction visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::count, JSpearValue::count);
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
            return (rg, lv, ds) -> new JSpearReal(rg.nextDouble());
        } else {
            JSpearExpressionEvaluationFunction fromEvaluation = ctx.from.accept(this);
            JSpearExpressionEvaluationFunction toEvaluation = ctx.to.accept(this);
            return (rg, lv, ds) -> JSpearReal.sample(rg, fromEvaluation.eval(rg, lv, ds), toEvaluation.eval(rg, lv, ds));
        }
    }

    @Override
    public JSpearExpressionEvaluationFunction visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        return evaluateArrayExpressions(ctx.target.getText(), ctx.guard, JSpearValue::minElement, JSpearValue::minElement);
    }


}
