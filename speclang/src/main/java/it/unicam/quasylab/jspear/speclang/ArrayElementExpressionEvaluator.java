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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public class ArrayElementExpressionEvaluator  extends JSpearSpecificationLanguageBaseVisitor<ArrayElementExpression> {

    private final ArrayElementPredicateEvaluator predicateEvaluator;
    private final ToDoubleFunction<String> constants;
    private final ToDoubleFunction<String> parameters;

    private final VariableRegistry registy;
    private final SymbolTable symbolTable;


    public ArrayElementExpressionEvaluator(ToDoubleFunction<String> constants, ToDoubleFunction<String> parameters, ArrayElementPredicateEvaluator predicateEvaluator, VariableRegistry registy, SymbolTable symbolTable) {
        this.constants = constants;
        this.parameters = parameters;
        this.predicateEvaluator = predicateEvaluator;
        this.registy = registy;
        this.symbolTable = symbolTable;
    }

    @Override
    public ArrayElementExpression visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    private ArrayElementExpression evalBinary(String functionName,
                                              JSpearSpecificationLanguageParser.ExpressionContext firstArgument,
                                              JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        ArrayElementExpression firstArgumentEvaluation = firstArgument.accept(this);
        ArrayElementExpression secondArgumentEvaluation = secondArgument.accept(this);
        DoubleBinaryOperator op = EvaluationUtil.getBinaryOperator(functionName);
        return (rg, ds, v) -> op.applyAsDouble(firstArgumentEvaluation.apply(rg, ds, v), secondArgumentEvaluation.apply(rg, ds, v));
    }

    private ArrayElementExpression evalUnary(String functionName,
                                              JSpearSpecificationLanguageParser.ExpressionContext argument) {
        ArrayElementExpression argumentEvaluation = argument.accept(this);
        DoubleUnaryOperator op = EvaluationUtil.getUnaryOperator(functionName);
        return (rg, ds, v) -> op.applyAsDouble(argumentEvaluation.apply(rg, ds, v));
    }


    @Override
    public ArrayElementExpression visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return evalBinary("pow", ctx.left, ctx.right);
    }

    @Override
    public ArrayElementExpression visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return evalBinary(ctx.binaryMathFunction().start.getText(), ctx.left, ctx.right);
    }

    @Override
    public ArrayElementExpression visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public ArrayElementExpression visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return (rg, ds, v) -> v;
    }

    @Override
    public ArrayElementExpression visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        return evalUnary(ctx.unaryMathFunction().start.getText(), ctx.argument);
    }

    @Override
    public ArrayElementExpression visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return evalUnary(ctx.op.getText(), ctx.arg);
    }

    @Override
    public ArrayElementExpression visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (symbolTable.isAConstant(name)) {
            double v2 = constants.applyAsDouble(name);
            return (rg, ds, v) -> v2;
        }
        if (symbolTable.isAParameter(name)) {
            double v2 = parameters.applyAsDouble(name);
            return (rg, ds, v) -> v2;
        }
        if (symbolTable.isAVariable(name)) {
            Variable var = registy.getVariable(name);
            return (rg, ds, v) -> ds.getValue(var);
        }
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        int value = Integer.parseInt(ctx.getText());
        return (rg, ds, v) -> value;
    }

    @Override
    public ArrayElementExpression visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        ArrayElementPredicate guardEvaluation = ctx.guard.accept(this.predicateEvaluator);
        ArrayElementExpression thenEvaluation = ctx.thenBranch.accept(this);
        ArrayElementExpression elseEvaluation = ctx.elseBranch.accept(this);
        return (rg, ds, v) -> (guardEvaluation.test(rg, ds, v)?thenEvaluation.apply(rg, ds, v): elseEvaluation.apply(rg, ds, v));
    }

    @Override
    public ArrayElementExpression visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        double realValue = Double.parseDouble(ctx.getText());
        return (rg, ds, v) -> realValue;
    }

    @Override
    public ArrayElementExpression visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        return evalBinary(ctx.op.getText(), ctx.left, ctx.right);
    }

    @Override
    public ArrayElementExpression visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        return evalBinary(ctx.op.getText(), ctx.left, ctx.right);
    }

    @Override
    public ArrayElementExpression visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }

    @Override
    public ArrayElementExpression visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        return (rg, ds, v) -> Double.NaN;
    }
}
