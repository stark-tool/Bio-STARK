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

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

//TODO: Change "DataStateRandomExpression" to "ExpressionEvaluationFunction"
public class RandomExpressionEvaluator extends JSpearSpecificationLanguageBaseVisitor<DataStateRandomExpression> {


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

    private final ToDoubleFunction<String> constants;

    private final ToDoubleFunction<String> parameters;

    private final VariableRegistry registry;
    private final SymbolTable symbolTable;

    public RandomExpressionEvaluator(ToDoubleFunction<String> constants, ToDoubleFunction<String> parameters, VariableRegistry registry, SymbolTable symbolTable) {
        this.constants = constants;
        this.parameters = parameters;
        this.registry = registry;
        this.symbolTable = symbolTable;
    }

    @Override
    public DataStateRandomExpression visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        DataStateRandomExpression argumentEvaluation = ctx.arg.accept(this);
        return (rg, ds) -> (argumentEvaluation.eval(rg,ds)!=1.0?1.0:0.0);
    }

    @Override
    public DataStateRandomExpression visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return evalBinary(Math::pow, ctx.left, ctx.right);
    }

    private DataStateRandomExpression evalBinary(DoubleBinaryOperator op, JSpearSpecificationLanguageParser.ExpressionContext firstArgument, JSpearSpecificationLanguageParser.ExpressionContext secondArgument) {
        DataStateRandomExpression firstArgumentEvaluation = firstArgument.accept(this);
        DataStateRandomExpression secondArgumentEvaluation = firstArgument.accept(this);
        return (rg,ds) -> op.applyAsDouble(firstArgumentEvaluation.eval(rg, ds), secondArgumentEvaluation.eval(rg, ds));
    }


    @Override
    public DataStateRandomExpression visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.binaryMathFunction().start.getText()), ctx.left, ctx.right);
    }

    private DoubleBinaryOperator getBinaryOperator(String op) {
        return binaryOperators.getOrDefault(op, (x,y) -> Double.NaN);
    }

    @Override
    public DataStateRandomExpression visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return (rg, ds) -> 1.0;
    }

    @Override
    public DataStateRandomExpression visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        Variable arrayReference = registry.getVariable(ctx.target.getText());
        if (ctx.guard == null) {
            return (rg, ds) -> registry.getMaxValueInArray(arrayReference, ds);
        } else {
            ArrayElementPredicateEvaluator evaluator = new ArrayElementPredicateEvaluator(constants, parameters, registry, symbolTable);
            ArrayElementPredicate valuePredicate = ctx.guard.accept(evaluator);
            return (rg, ds) -> registry.getMaxValueInArray(arrayReference, ds, v ->  valuePredicate.test(rg, ds, v));
        }
    }

    @Override
    public DataStateRandomExpression visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        DataStateRandomExpression leftEvaluation = ctx.left.accept(this);
        DataStateRandomExpression rightEvaluation = ctx.right.accept(this);
        String op = ctx.op.getText();
        return (rg, ds) -> (EvaluationUtil.evalRelation(op, leftEvaluation.eval(rg, ds), rightEvaluation.eval(rg, ds))?1.0:0.0);
    }

    @Override
    public DataStateRandomExpression visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public DataStateRandomExpression visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return (rg, ds) -> 0.0;
    }

    @Override
    public DataStateRandomExpression visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        DataStateRandomExpression firstArgumentEvaluation = ctx.left.accept(this);
        DataStateRandomExpression secondArgumentEvaluation = ctx.right.accept(this);
        return (rg, ds) -> Math.min(firstArgumentEvaluation.eval(rg, ds), secondArgumentEvaluation.eval(rg, ds));
    }

    @Override
    public DataStateRandomExpression visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return (rg, ds) -> Double.NaN;
    }

    @Override
    public DataStateRandomExpression visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        DataStateRandomExpression argumentEvaluation = ctx.argument.accept(this);
        DoubleUnaryOperator op = EvaluationUtil.getUnaryOperator(ctx.unaryMathFunction().start.getText());
        return (rg, ds) -> op.applyAsDouble(argumentEvaluation.eval(rg, ds));
    }

    @Override
    public DataStateRandomExpression visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        DataStateRandomExpression argumentEvaluation = ctx.arg.accept(this);
        DoubleUnaryOperator op = EvaluationUtil.getUnaryOperator(ctx.op.getText());
        return (rg, ds) -> op.applyAsDouble(argumentEvaluation.eval(rg, ds));
    }

    @Override
    public DataStateRandomExpression visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        return (rg, ds) -> Double.NaN;
    }

    @Override
    public DataStateRandomExpression visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (symbolTable.isAConstant(name)) {
            double v = constants.applyAsDouble(name);
            return (rg, ds) -> v;
        }
        if (symbolTable.isAParameter(name)) {
            double v = parameters.applyAsDouble(name);
            return (rg, ds) -> v;
        }
        if (symbolTable.isAVariable(name)) {
            Variable var = registry.getVariable(name);
            return (rg, ds) -> ds.getValue(var);
        }
        return (rg, ds) -> Double.NaN;
    }

    @Override
    public DataStateRandomExpression visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        int v = Integer.parseInt(ctx.getText());
        return (rg, ds) -> v;
    }

    @Override
    public DataStateRandomExpression visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        DataStateRandomExpression firstArgumentEvaluation = ctx.mean.accept(this);
        DataStateRandomExpression secondArgumentEvaluation = ctx.variance.accept(this);
        return (rg, ds) -> rg.nextGaussian()*firstArgumentEvaluation.eval(rg, ds)+secondArgumentEvaluation.eval(rg,ds); //TODO: CHECK!
    }

    @Override
    public DataStateRandomExpression visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        DataStateRandomExpression[] argumentsEvaluation = ctx.values.stream().map(e -> e.accept(this)).toArray(DataStateRandomExpression[]::new);
        return (rg, ds) -> argumentsEvaluation[rg.nextInt(argumentsEvaluation.length)].eval(rg, ds);
    }

    @Override
    public DataStateRandomExpression visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        Variable arrayReference = registry.getVariable(ctx.target.getText());
        if (ctx.guard == null) {
            return (rg, ds) -> registry.getMeanValueInArray(arrayReference, ds);
        } else {
            ArrayElementPredicateEvaluator evaluator = new ArrayElementPredicateEvaluator(constants, parameters, registry, symbolTable);
            ArrayElementPredicate valuePredicate = ctx.guard.accept(evaluator);
            return (rg, ds) -> registry.getMeanValueInArray(arrayReference, ds, v ->  valuePredicate.test(rg, ds, v));
        }
    }

    @Override
    public DataStateRandomExpression visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        DataStateRandomExpression firstArgumentEvaluation = ctx.left.accept(this);
        DataStateRandomExpression secondArgumentEvaluation = ctx.right.accept(this);
        return (rg, ds) -> Math.max(firstArgumentEvaluation.eval(rg, ds), secondArgumentEvaluation.eval(rg, ds));
    }

    @Override
    public DataStateRandomExpression visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        DataStateRandomExpression guardEvaluation = ctx.guard.accept(this);
        DataStateRandomExpression thenEvaluation = ctx.guard.accept(this);
        DataStateRandomExpression elseEvaluation = ctx.guard.accept(this);
        return (rg, ds) -> (guardEvaluation.eval(rg, ds)>0.0?thenEvaluation.eval(rg, ds):elseEvaluation.eval(rg, ds));
    }

    @Override
    public DataStateRandomExpression visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        double value = Double.parseDouble(ctx.getText());
        return (rg, ds) -> value;
    }

    @Override
    public DataStateRandomExpression visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        return (rg, ds) -> Double.NaN;
    }

    @Override
    public DataStateRandomExpression visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        Variable arrayReference = registry.getVariable(ctx.target.getText());
        if (ctx.guard == null) {
            return (rg, ds) -> arrayReference.getSize();
        } else {
            ArrayElementPredicateEvaluator evaluator = new ArrayElementPredicateEvaluator(constants, parameters, registry, symbolTable);
            ArrayElementPredicate valuePredicate = ctx.guard.accept(evaluator);
            return (rg, ds) -> registry.countValuesInArray(arrayReference, ds, v ->  valuePredicate.test(rg, ds, v));
        }
    }

    @Override
    public DataStateRandomExpression visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public DataStateRandomExpression visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        return evalBinary(getBinaryOperator(ctx.op.getText()), ctx.left, ctx.right);
    }

    @Override
    public DataStateRandomExpression visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        if (ctx.from == null) {
            return (rg, ds) -> rg.nextDouble();
        } else {
            DataStateRandomExpression fromEvaluation = ctx.from.accept(this);
            DataStateRandomExpression toEvaluation = ctx.to.accept(this);
            return (rg, ds) -> {
                double fromValue = fromEvaluation.eval(rg, ds);
                double toValue = toEvaluation.eval(rg, ds);
                return fromValue+(toValue-fromValue)*rg.nextDouble();
            };
        }
    }

    @Override
    public DataStateRandomExpression visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        Variable arrayReference = registry.getVariable(ctx.target.getText());
        if (ctx.guard == null) {
            return (rg, ds) -> registry.getMinValueInArray(arrayReference, ds);
        } else {
            ArrayElementPredicateEvaluator evaluator = new ArrayElementPredicateEvaluator(constants, parameters, registry, symbolTable);
            ArrayElementPredicate valuePredicate = ctx.guard.accept(evaluator);
            return (rg, ds) -> registry.getMinValueInArray(arrayReference, ds, v ->  valuePredicate.test(rg, ds, v));
        }
    }


}
