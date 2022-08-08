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

import it.unicam.quasylab.jspear.DefaultRandomGenerator;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class NumericalExpressionEvaluator extends JSpearSpecificationLanguageBaseVisitor<Double> {


    private final static Map<String, DoubleBinaryOperator> binaryOperators = Map.of(
            "+", (x,y) -> x+y,
            "*", (x,y) -> x*y,
            "-", (x,y) -> x-y,
            "/", (x,y) -> x/y, //TODO: Check how to handle division by zero!
            "%", (x,y) -> x%y,
            "atan2", Math::atan2,
            "hypot", Math::hypot,
            "max",   Math::max,
            "min",   Math::min,
            "pow",   Math::pow
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

    private final Map<String, Double> constants;

    private final Map<String, Double> parameters;


    public NumericalExpressionEvaluator(Map<String, Double> constants, Map<String, Double> parameters) {
        this.constants = constants;
        this.parameters = parameters;
    }

    @Override
    public Double visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        return (ctx.arg.accept(this)==0?1.0:0.0);
    }

    @Override
    public Double visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return evalBinary(Math::pow, ctx.left, ctx.right);
    }

    private Double evalBinary(DoubleBinaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext firstArgument, JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        return evalBinary(op, firstArgument.accept(this), secondArgument.accept(this));
    }

    private Double evalBinary(DoubleBinaryOperator op, double firstArgument, double secondArgument) {
        return op.applyAsDouble(firstArgument, secondArgument);
    }

    @Override
    public Double visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.binaryMathFunction().start.getText()), ctx.left, ctx.right);
    }

    private DoubleBinaryOperator getBinaryOperator(String op) {
        return binaryOperators.getOrDefault(op, (x,y) -> Double.NaN);
    }

    @Override
    public Double visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return 1.0;
    }

    @Override
    public Double visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        return evalBinary(getRelationOperator(ctx.op.getText()),ctx.left, ctx.right);
    }

    private DoubleBinaryOperator getRelationOperator(String op) {
        return switch (op) {
            case "<" -> (x, y) -> (x < y ? 1.0 : 0.0);
            case "<=" -> (x, y) -> (x <= y ? 1.0 : 0.0);
            case "==" -> (x, y) -> (x == y ? 1.0 : 0.0);
            case ">=" -> (x, y) -> (x >= y ? 1.0 : 0.0);
            case ">" -> (x, y) -> (x > y ? 1.0 : 0.0);
            default -> (x, y) -> 0.0;
        };
    }

    @Override
    public Double visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Double visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return 0.0;
    }

    @Override
    public Double visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        return evalBinary(Math::min, ctx.left, ctx.right);
    }

    @Override
    public Double visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.unaryMathFunction().start.getText()), ctx.argument);
    }

    private Double evalUnary(DoubleUnaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext argument) {
        return op.applyAsDouble(argument.accept(this));
    }


    @Override
    public Double visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return evalUnary(unaryOperators.get(ctx.op.getText()), ctx.arg);
    }

    @Override
    public Double visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (constants.containsKey(name)) {
            return constants.get(name);
        }
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        return Double.NaN;
    }

    @Override
    public Double visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        return (double) Integer.parseInt(ctx.getText());
    }

    @Override
    public Double visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        return evalBinary(Math::max, ctx.left, ctx.right);
    }

    @Override
    public Double visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        return (ctx.guard.accept(this)>0?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    @Override
    public Double visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public Double visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public Double visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        return Double.NaN;
    }

    @Override
    public Double visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        return Double.NaN;
    }


}
