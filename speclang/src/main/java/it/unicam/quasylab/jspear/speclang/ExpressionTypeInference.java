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

import org.antlr.v4.runtime.ParserRuleContext;

public class ExpressionTypeInference extends JSpearSpecificationLanguageBaseVisitor<JSpearType> {
    private final TypeContext context;
    private final ParseErrorCollector errors;

    public ExpressionTypeInference(TypeContext context, ParseErrorCollector errors) {
        this.context = context;
        this.errors = errors;
    }

    public boolean checkType(JSpearType expectedType, ParserRuleContext ctx) {
        JSpearType actualType = ctx.accept(this);
        if (!expectedType.isCompatibleWith(actualType)) {
            errors.record(ParseUtil.typeError(expectedType, actualType, ctx.start));
            return false;
        }
        return true;
    }

    @Override
    public JSpearType visitNegationExpression(JSpearSpecificationLanguageParser.NegationExpressionContext ctx) {
        if (checkType(JSpearType.BOOLEAN_TYPE, ctx.arg)) {
            return JSpearType.BOOLEAN_TYPE;
        }
        return JSpearType.ERROR_TYPE;
    }

    @Override
    public JSpearType visitExponentExpression(JSpearSpecificationLanguageParser.ExponentExpressionContext ctx) {
        return combineToRealType(ctx.left, ctx.right);
    }

    private JSpearType combineToRealType(ParserRuleContext left, ParserRuleContext right) {
        checkNumerical(left);
        checkNumerical(right);
        return JSpearType.REAL_TYPE;
    }

    private JSpearType checkNumerical(ParserRuleContext ctx) {
        JSpearType type = ctx.accept(this);
        if (!type.isNumerical()) {
            errors.record(ParseUtil.expectedNumericalType(type, ctx.start));
            return JSpearType.ERROR_TYPE;
        }
        return type;
    }

    @Override
    public JSpearType visitBinaryMathCallExpression(JSpearSpecificationLanguageParser.BinaryMathCallExpressionContext ctx) {
        return combineToRealType(ctx.left, ctx.right);
    }

    @Override
    public JSpearType visitArrayExpression(JSpearSpecificationLanguageParser.ArrayExpressionContext ctx) {
        for (JSpearSpecificationLanguageParser.ExpressionContext element: ctx.elements) {
            checkType(JSpearType.REAL_TYPE, element);
        }
        return JSpearType.ARRAY_TYPE;
    }

    @Override
    public JSpearType visitReferenceExpression(JSpearSpecificationLanguageParser.ReferenceExpressionContext ctx) {
        String name = ctx.name.getText();
        if (!context.isDefined(name)) {
            errors.record(ParseUtil.unknownSymbol(ctx.name));
            return JSpearType.ERROR_TYPE;
        }
        if (!context.isReferenceable(name)) {
            errors.record(ParseUtil.illegalUseOfName(ctx.name));
            return JSpearType.ERROR_TYPE;
        }
        JSpearType type = context.getTypeOf(name);
        if (!type.isAnArray()&&(ctx.first != null)) {
            errors.record(ParseUtil.illegalUseOfArraySyntax(ctx.name));
            return JSpearType.ERROR_TYPE;
        }
        if ((ctx.first !=null)&&(!checkType(JSpearType.INTEGER_TYPE, ctx.first))) {
            return JSpearType.ERROR_TYPE;
        }
        if ((ctx.last !=null)&&(!checkType(JSpearType.INTEGER_TYPE, ctx.last))) {
            return JSpearType.ERROR_TYPE;
        }
        if (type.isAnArray()&&(ctx.first != null)&&(ctx.last==null)) {
            return JSpearType.REAL_TYPE;
        }
        return type;
    }

    @Override
    public JSpearType visitIntValue(JSpearSpecificationLanguageParser.IntValueContext ctx) {
        return JSpearType.INTEGER_TYPE;
    }

    @Override
    public JSpearType visitTrueValue(JSpearSpecificationLanguageParser.TrueValueContext ctx) {
        return JSpearType.BOOLEAN_TYPE;
    }

    @Override
    public JSpearType visitRelationExpression(JSpearSpecificationLanguageParser.RelationExpressionContext ctx) {
        JSpearType leftType = ctx.left.accept(this);
        JSpearType rightType = ctx.right.accept(this);
        if (!leftType.canBeMergedWith(rightType)) {
            this.errors.record(ParseUtil.typeError(leftType,rightType, ctx.right.start));
        }
        return JSpearType.BOOLEAN_TYPE;
    }

    @Override
    public JSpearType visitBracketExpression(JSpearSpecificationLanguageParser.BracketExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public JSpearType visitOrExpression(JSpearSpecificationLanguageParser.OrExpressionContext ctx) {
        checkType(JSpearType.BOOLEAN_TYPE, ctx.left);
        checkType(JSpearType.BOOLEAN_TYPE, ctx.right);
        return JSpearType.BOOLEAN_TYPE;
    }

    @Override
    public JSpearType visitIfThenElseExpression(JSpearSpecificationLanguageParser.IfThenElseExpressionContext ctx) {
        checkType(JSpearType.BOOLEAN_TYPE, ctx.guard);
        JSpearType thenType = ctx.thenBranch.accept(this);
        JSpearType elseType = ctx.elseBranch.accept(this);
        if (!thenType.canBeMergedWith(elseType)) {
            errors.record(ParseUtil.typeError(thenType, elseType, ctx.elseBranch.start));
            return JSpearType.ERROR_TYPE;
        }
        return JSpearType.merge(thenType, elseType);
    }

    @Override
    public JSpearType visitFalseValue(JSpearSpecificationLanguageParser.FalseValueContext ctx) {
        return JSpearType.BOOLEAN_TYPE;
    }

    @Override
    public JSpearType visitRealValue(JSpearSpecificationLanguageParser.RealValueContext ctx) {
        return JSpearType.REAL_TYPE;
    }

    @Override
    public JSpearType visitAndExpression(JSpearSpecificationLanguageParser.AndExpressionContext ctx) {
        checkType(JSpearType.BOOLEAN_TYPE, ctx.left);
        checkType(JSpearType.BOOLEAN_TYPE, ctx.right);
        return JSpearType.BOOLEAN_TYPE;
    }

    @Override
    public JSpearType visitCallExpression(JSpearSpecificationLanguageParser.CallExpressionContext ctx) {
        String functionName = ctx.name.getText();
        if (!context.isAFunction(functionName)) {
            errors.record(ParseUtil.isNotAFunction(ctx.name));
            return JSpearType.ERROR_TYPE;
        }
        JSpearType[] expectedArguments = context.getArgumentsType(functionName);
        if (ctx.callArguments.size() != expectedArguments.length) {
            errors.record(ParseUtil.illegalNumberOfArguments(ctx.name, expectedArguments.length, ctx.callArguments.size()));
            return JSpearType.ERROR_TYPE;
        }
        for(int i=0; i<expectedArguments.length; i++) {
            checkType(expectedArguments[i], ctx.callArguments.get(i));
        }
        return context.getReturnType(functionName);
    }

    @Override
    public JSpearType visitMulDivExpression(JSpearSpecificationLanguageParser.MulDivExpressionContext ctx) {
        JSpearType leftType = checkNumerical(ctx.left);
        JSpearType rightType = checkNumerical(ctx.right);
        return JSpearType.merge(leftType, rightType);
    }

    @Override
    public JSpearType visitAddSubExpression(JSpearSpecificationLanguageParser.AddSubExpressionContext ctx) {
        JSpearType leftType = checkNumerical(ctx.left);
        JSpearType rightType = checkNumerical(ctx.right);
        return JSpearType.merge(leftType, rightType);
    }

    @Override
    public JSpearType visitUnaryMathCallExpression(JSpearSpecificationLanguageParser.UnaryMathCallExpressionContext ctx) {
        checkNumerical(ctx.argument);
        return JSpearType.REAL_TYPE;
    }

    @Override
    public JSpearType visitUnaryExpression(JSpearSpecificationLanguageParser.UnaryExpressionContext ctx) {
        return checkNumerical(ctx.arg);
    }

    //TODO: Check the correct usage of random expressions!

    @Override
    public JSpearType visitNormalExpression(JSpearSpecificationLanguageParser.NormalExpressionContext ctx) {
        if (checkType(JSpearType.REAL_TYPE, ctx.mean)&checkType(JSpearType.REAL_TYPE, ctx.variance)) {
            return JSpearType.REAL_TYPE;
        } else {
            return JSpearType.ERROR_TYPE;
        }
    }

    @Override
    public JSpearType visitUniformExpression(JSpearSpecificationLanguageParser.UniformExpressionContext ctx) {
        JSpearType type = JSpearType.ANY_TYPE;
        for (JSpearSpecificationLanguageParser.ExpressionContext v: ctx.values) {
            JSpearType current = v.accept(this);
            if (type.canBeMergedWith(current)) {
                type = JSpearType.merge(type, current);
            } else {
                this.errors.record(ParseUtil.typeError(type, current, v.start));
                return JSpearType.ERROR_TYPE;
            }
        }
        return type;
    }

    @Override
    public JSpearType visitRandomExpression(JSpearSpecificationLanguageParser.RandomExpressionContext ctx) {
        if (ctx.from != null) {
            if (checkType(JSpearType.REAL_TYPE, ctx.from)&checkType(JSpearType.REAL_TYPE, ctx.to)) {
                return JSpearType.REAL_TYPE;
            } else {
                return JSpearType.ERROR_TYPE;
            }
        } else {
            return JSpearType.REAL_TYPE;
        }
    }

    @Override
    public JSpearType visitMaxArrayElementExpression(JSpearSpecificationLanguageParser.MaxArrayElementExpressionContext ctx) {
        String targetName = ctx.target.getText();
        if (!context.isReferenceable(targetName)) {
            errors.record(ParseUtil.illegalUseOfName(ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        JSpearType type = context.getTypeOf(targetName);
        if (!type.isAnArray()) {
            errors.record(ParseUtil.typeError(JSpearType.ARRAY_TYPE, type, ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        if ((ctx.guard==null)||checkType(JSpearType.BOOLEAN_TYPE, ctx.guard)) {
            return JSpearType.REAL_TYPE;
        } else {
            return JSpearType.ERROR_TYPE;
        }
    }

    @Override
    public JSpearType visitLambdaParameterExpression(JSpearSpecificationLanguageParser.LambdaParameterExpressionContext ctx) {
        return JSpearType.REAL_TYPE; //TODO: Handle nested context...
    }

    @Override
    public JSpearType visitMeanArrayElementExpression(JSpearSpecificationLanguageParser.MeanArrayElementExpressionContext ctx) {
        return super.visitMeanArrayElementExpression(ctx);
    }

    @Override
    public JSpearType visitCountArrayElementExpression(JSpearSpecificationLanguageParser.CountArrayElementExpressionContext ctx) {
        String targetName = ctx.target.getText();
        if (!context.isReferenceable(targetName)) {
            errors.record(ParseUtil.illegalUseOfName(ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        JSpearType type = context.getTypeOf(targetName);
        if (!type.isAnArray()) {
            errors.record(ParseUtil.typeError(JSpearType.ARRAY_TYPE, type, ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        if ((ctx.guard==null)||checkType(JSpearType.BOOLEAN_TYPE, ctx.guard)) {
            return JSpearType.INTEGER_TYPE;
        } else {
            return JSpearType.ERROR_TYPE;
        }
    }

    @Override
    public JSpearType visitMinArrayElementExpression(JSpearSpecificationLanguageParser.MinArrayElementExpressionContext ctx) {
        String targetName = ctx.target.getText();
        if (!context.isReferenceable(targetName)) {
            errors.record(ParseUtil.illegalUseOfName(ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        JSpearType type = context.getTypeOf(targetName);
        if (!type.isAnArray()) {
            errors.record(ParseUtil.typeError(JSpearType.ARRAY_TYPE, type, ctx.target));
            return JSpearType.ERROR_TYPE;
        }
        if ((ctx.guard==null)||checkType(JSpearType.BOOLEAN_TYPE, ctx.guard)) {
            return JSpearType.REAL_TYPE;
        } else {
            return JSpearType.ERROR_TYPE;
        }
    }
}
