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

import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.function.ToDoubleFunction;

public class ArrayElementPredicateEvaluator extends JSpearSpecificationLanguageBaseVisitor<ArrayElementPredicate> {

    private final ArrayElementExpressionEvaluator expressionEvaluator;

    public ArrayElementPredicateEvaluator(ToDoubleFunction<String> constants, ToDoubleFunction<String> parameters, VariableRegistry registy, SymbolTable symbolTable) {
        this.expressionEvaluator = new ArrayElementExpressionEvaluator(constants,parameters, this, registy, symbolTable);
    }

    @Override
    public ArrayElementPredicate visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        ArrayElementPredicate argumentEvaluation = ctx.arg.accept(this);
        return (rg, ds, v) -> !argumentEvaluation.test(rg, ds, v);
    }

    @Override
    public ArrayElementPredicate visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return (rg, ds, v) -> true;
    }

    @Override
    public ArrayElementPredicate visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        ArrayElementExpression leftArgumentEvaluation = ctx.left.accept(expressionEvaluator);
        ArrayElementExpression rightArgumentEvaluation = ctx.right.accept(expressionEvaluator);
        String op = ctx.op.getText();
        return (rg, ds, v) -> EvaluationUtil.evalRelation(op, leftArgumentEvaluation.apply(rg, ds, v), rightArgumentEvaluation.apply(rg, ds, v));
    }

    @Override
    public ArrayElementPredicate visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public ArrayElementPredicate visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return (rg, ds, v) -> false;
    }

    @Override
    public ArrayElementPredicate visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        return ctx.left.accept(this).and(ctx.right.accept(this));
    }

    @Override
    public ArrayElementPredicate visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        return ctx.left.accept(this).or(ctx.right.accept(this));
    }

    @Override
    public ArrayElementPredicate visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        ArrayElementPredicate guardEvaluation = ctx.guard.accept(this);
        ArrayElementPredicate thenEvaluation = ctx.thenBranch.accept(this);
        ArrayElementPredicate elseEvaluation = ctx.elseBranch.accept(this);
        return (rg,ds,v) -> (guardEvaluation.test(rg,ds,v)?thenEvaluation.test(rg,ds,v):elseEvaluation.test(rg,ds,v));
    }
}
