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

public class FunctionTypeInference extends JSpearSpecificationLanguageBaseVisitor<JSpearType>  {
    private final ParseErrorCollector errors;
    private final ExpressionTypeInference expressionTypeInference;

    public FunctionTypeInference(TypeContext context, ParseErrorCollector errors) {
        this.errors = errors;
        this.expressionTypeInference = new ExpressionTypeInference(context, errors);
    }


    @Override
    public JSpearType visitSwitchStatement(JSpearSpecificationLanguageParser.SwitchStatementContext ctx) {
        JSpearType switchType = JSpearType.ANY_TYPE;
        for (JSpearSpecificationLanguageParser.CaseStatementContext switchCase: ctx.switchCases) {
            JSpearType caseType = switchCase.functionStatement().accept(this);
            if (switchType.canBeMergedWith(caseType)) {
                switchType = JSpearType.merge(switchType, caseType);
            } else {
                errors.record(ParseUtil.typeError(switchType, caseType, switchCase.functionStatement().start));
                return JSpearType.ERROR_TYPE;
            }
        }
        return switchType;
    }

    @Override
    public JSpearType visitIfThenElseStatement(JSpearSpecificationLanguageParser.IfThenElseStatementContext ctx) {
        expressionTypeInference.checkType(JSpearType.BOOLEAN_TYPE, ctx.guard);
        JSpearType ifType = ctx.thenStatement.accept(this);
        if (ctx.elseStatement != null) {
            JSpearType elseType = ctx.elseStatement.accept(this);
            if (ifType.canBeMergedWith(elseType)) {
                ifType = JSpearType.merge(ifType, elseType);
            } else {
                errors.record(ParseUtil.typeError(ifType, elseType, ctx.elseStatement.start));
                return JSpearType.ERROR_TYPE;
            }
        }
        return ifType;
    }

    @Override
    public JSpearType visitReturnStatement(JSpearSpecificationLanguageParser.ReturnStatementContext ctx) {
        return ctx.expression().accept(expressionTypeInference);
    }

    @Override
    public JSpearType visitFunctionBlock(JSpearSpecificationLanguageParser.FunctionBlockContext ctx) {
        return ctx.functionStatement().accept(this);
    }
}
