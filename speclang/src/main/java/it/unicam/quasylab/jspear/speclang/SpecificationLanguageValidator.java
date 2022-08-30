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

import it.unicam.quasylab.jspear.speclang.types.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.stream.Collectors;

public class SpecificationLanguageValidator extends JSpearSpecificationLanguageBaseVisitor<Boolean> {
    private final ParseErrorCollector errors;
    private final SymbolTable symbols = new SymbolTable();

    private final Set<String> environmentVariables = new HashSet<>();

    public SpecificationLanguageValidator(ParseErrorCollector errors) {
        this.errors = errors;
    }

    @Override
    public Boolean visitJSpearSpecificationModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext ctx) {
        boolean flag = true;
        for (JSpearSpecificationLanguageParser.ElementContext element: ctx.element()) {
            flag &= element.accept(this);
        }
        return flag;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate&nextResult;
    }

    @Override
    public Boolean visitFunctionDeclaration(JSpearSpecificationLanguageParser.FunctionDeclarationContext ctx) {
        if (checkIfNotDuplicated(ctx.name.getText(), ctx)) {
            JSpearType[] argumentType = ctx.arguments.stream().map(a -> typeOf(a.type())).toArray(JSpearType[]::new);
            TypeContext context = new NestedTypeContext(
                    new LocalTypeContext(getLocalDeclarations(ctx.arguments)),
                    this.symbols);
            JSpearType returnType = ctx.accept(new JSpearFunctionTypeInference(context, this.errors));
            if (!returnType.isError()) {
                this.symbols.recordFunction(ctx.name.getText(), argumentType, returnType, ctx);
                return true;
            }
        }
        return false;
    }


    private Map<String, JSpearType> getLocalDeclarations(List<JSpearSpecificationLanguageParser.FunctionArgumentContext> arguments) {
        return arguments.stream().collect(Collectors.toMap(a -> a.name.getText(), a -> typeOf(a.type())));
    }

    private JSpearType typeOf(JSpearSpecificationLanguageParser.TypeContext type) {
        if (type instanceof JSpearSpecificationLanguageParser.BooleanTypeContext) {
            return JSpearType.BOOLEAN_TYPE;
        }
        if (type instanceof JSpearSpecificationLanguageParser.IntegerTypeContext) {
            return JSpearType.INTEGER_TYPE;
        }
        if (type instanceof JSpearSpecificationLanguageParser.RealTypeContext) {
            return JSpearType.REAL_TYPE;
        }
        if (type instanceof JSpearSpecificationLanguageParser.ArrayTypeContext) {
            //TODO: Check if size  is an integer!
            return JSpearType.ARRAY_TYPE;
        }
        if (type instanceof JSpearSpecificationLanguageParser.CustomTypeContext) {
            String typeName = ((JSpearSpecificationLanguageParser.CustomTypeContext) type).name.getText();
            if (!symbols.isACustomType(typeName)) {
                this.errors.record(ParseUtil.unknownType(((JSpearSpecificationLanguageParser.CustomTypeContext) type).name));
                return JSpearType.ERROR_TYPE;
            }
            return this.symbols.getCustomType(typeName);
        }
        return JSpearType.ERROR_TYPE;
    }

    private boolean checkIfNotDuplicated(String name, ParserRuleContext ctx) {
        if (this.symbols.isDefined(name)) {
            errors.record(ParseUtil.duplicatedSymbol(name, this.symbols.get(name), ctx));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitSystemDeclaration(JSpearSpecificationLanguageParser.SystemDeclarationContext ctx) {
        if (checkIfNotDuplicated(ctx.name.getText(), ctx)) {
            this.symbols.recordSystemDeclaration(ctx.name.getText(), ctx);
            boolean flag = true;
            for (JSpearSpecificationLanguageParser.InitialAssignmentContext init: ctx.initialAssignments) {
                flag &= init.accept(this);
            }
            return flag;
        }
        return false;
    }

    @Override
    public Boolean visitInitialAssignment(JSpearSpecificationLanguageParser.InitialAssignmentContext ctx) {
        if (symbols.isAVariable(ctx.name.getText())) {
            return new ExpressionTypeInference(this.symbols, this.errors).checkType(symbols.getTypeOf(ctx.name.getText()), ctx.value);
        }
        this.errors.record(ParseUtil.unknownVariable(ctx.name));
        return false;
    }

    @Override
    public Boolean visitPenaltyDeclaration(JSpearSpecificationLanguageParser.PenaltyDeclarationContext ctx) {
        if (checkIfNotDuplicated(ctx.name.getText(), ctx)) {
            this.symbols.recordPenaltyFunction(ctx.name.getText(), ctx);
            return !new ExpressionTypeInference(symbols,errors).checkType(JSpearType.REAL_TYPE, ctx.value);
        }
        return false;
    }

    @Override
    public Boolean visitControllerDeclaration(JSpearSpecificationLanguageParser.ControllerDeclarationContext ctx) {
        boolean flag = recordControllerStates(ctx.stateDeclaration());
        for (JSpearSpecificationLanguageParser.StateDeclarationContext stateDeclaration: ctx.stateDeclaration()) {
            flag &= visitStateDeclaration(stateDeclaration);
        }
        return flag;
    }

    private boolean recordControllerStates(List<JSpearSpecificationLanguageParser.StateDeclarationContext> states) {
        boolean flag = true;
        for (JSpearSpecificationLanguageParser.StateDeclarationContext state: states) {
            if (checkIfNotDuplicated(state.name.getText(), state)) {
                this.symbols.recordControllerState(state.name.getText(), state);
            } else {
                flag = false;
            }
        }
        return flag;
    }

    @Override
    public Boolean visitStateDeclaration(JSpearSpecificationLanguageParser.StateDeclarationContext ctx) {
        return ctx.stateBody().accept(this);
    }

    @Override
    public Boolean visitParallelController(JSpearSpecificationLanguageParser.ParallelControllerContext ctx) {
        boolean flag = true;
        for (Token component: ctx.components) {
            if (!symbols.isAState(component.getText())) {
                this.errors.record(ParseUtil.unknownState(component));
            } else {
                flag = false;
            }
        }
        return flag;
    }

    @Override
    public Boolean visitExecBehaviour(JSpearSpecificationLanguageParser.ExecBehaviourContext ctx) {
        if (!this.symbols.isAState(ctx.target.getText())) {
            errors.record(ParseUtil.unknownState(ctx.target));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitStepBehaviour(JSpearSpecificationLanguageParser.StepBehaviourContext ctx) {
        boolean flag = true;
        if (!this.symbols.isAState(ctx.target.getText())) {
            errors.record(ParseUtil.unknownState(ctx.target));
            flag = false;
        }
        flag &= (ctx.steps == null)||(!new ExpressionTypeInference(this.symbols, errors).checkType(JSpearType.INTEGER_TYPE, ctx.steps));
        return flag;
    }

    @Override
    public Boolean visitBlockBehaviour(JSpearSpecificationLanguageParser.BlockBehaviourContext ctx) {
        return ctx.controllerBehaviour().accept(this);
    }


    @Override
    public Boolean visitSequentialController(JSpearSpecificationLanguageParser.SequentialControllerContext ctx) {
        return ctx.body.accept(this);
    }

    @Override
    public Boolean visitVariableAssignmentBehaviour(JSpearSpecificationLanguageParser.VariableAssignmentBehaviourContext ctx) {
        ExpressionTypeInference inference = new ExpressionTypeInference(this.symbols, this.errors);
        boolean flag = (ctx.guard == null || inference.checkType(JSpearType.BOOLEAN_TYPE, ctx.guard));
        Optional<JSpearType> expectedType = retrieveAndCheckVariableExpression(inference, ctx.target);
        return flag&expectedType.map(t -> inference.checkType(t, ctx.value)).orElse(false);
    }

    @Override
    public Boolean visitProbabilisticChoiceBehaviour(JSpearSpecificationLanguageParser.ProbabilisticChoiceBehaviourContext ctx) {
        boolean flag = true;
        for (JSpearSpecificationLanguageParser.ProbabilisticItemContext item: ctx.probabilisticItem()) {
            flag &= item.accept(this);
        }
        return flag;
    }

    @Override
    public Boolean visitProbabilisticItem(JSpearSpecificationLanguageParser.ProbabilisticItemContext ctx) {
        ExpressionTypeInference inference = new ExpressionTypeInference(this.symbols, errors);
        return ((ctx.guard == null)||inference.checkType(JSpearType.BOOLEAN_TYPE, ctx.guard))
                &&inference.checkType(JSpearType.REAL_TYPE, ctx.probability)
                &&visitBlockBehaviour(ctx.blockBehaviour());
    }

    @Override
    public Boolean visitIfThenElseBehaviour(JSpearSpecificationLanguageParser.IfThenElseBehaviourContext ctx) {
        ExpressionTypeInference inference = new ExpressionTypeInference(this.symbols, this.errors);
        return inference.checkType(JSpearType.BOOLEAN_TYPE, ctx.guard)
                &&ctx.thenBranch.accept(this)
                &&((ctx.elseBranch == null)||ctx.elseBranch.accept(this));
    }

    @Override
    public Boolean visitEnvironmentDeclaration(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
        TypeContext localContext = getLocalContext(ctx.localVariables);
        boolean flag = true;
        ExpressionTypeInference inference = new ExpressionTypeInference(new NestedTypeContext(localContext, symbols), errors, true);
        for(JSpearSpecificationLanguageParser.VariableAssignmentContext varAssignment: ctx.assignments) {
            flag &= checkEnvironmentVariableUpdate(inference, varAssignment);
        }
        return flag;
    }

    private boolean checkEnvironmentVariableUpdate(ExpressionTypeInference inference, JSpearSpecificationLanguageParser.VariableAssignmentContext varAssignment) {
        boolean flag = (varAssignment.guard == null || inference.checkType(JSpearType.BOOLEAN_TYPE, varAssignment.guard));
        Optional<JSpearType> expectedType = retrieveAndCheckVariableExpression(inference, varAssignment.target);
        return flag&expectedType.map(t -> inference.checkType(t, varAssignment.value)).orElse(false);
    }

    private Optional<JSpearType> retrieveAndCheckVariableExpression(ExpressionTypeInference inference, JSpearSpecificationLanguageParser.VarExpressionContext target) {
        String name = getAssignedVariable(target.name.getText());
        if (!symbols.isAVariable(name)) {
            errors.record(ParseUtil.unknownVariable(target.name));
            return Optional.empty();
        }
        this.environmentVariables.add(name);
        JSpearType type = symbols.getTypeOf(name);
        if (!type.isAnArray()&&(target.first != null)) {
            errors.record(ParseUtil.illegalUseOfArraySyntax(target.name));
            return Optional.empty();
        }
        if ((target.first !=null)&&(!inference.checkType(JSpearType.INTEGER_TYPE, target.first))) {
            return Optional.empty();
        }
        if ((target.last !=null)&&(!inference.checkType(JSpearType.INTEGER_TYPE, target.last))) {
            return Optional.empty();
        }
        if (type.isAnArray()&&(target.first != null)&&(target.last==null)) {
            return Optional.of(JSpearType.REAL_TYPE);
        }
        return Optional.of(type);
    }

    private String getAssignedVariable(String assignedName) {
        return assignedName.substring(0, assignedName.length()-1);
    }

    private TypeContext getLocalContext(List<JSpearSpecificationLanguageParser.LocalVariableContext> localVariables) {
        //TODO: Manage errors in local inference.
        TypeContext context = new EmptyTypeContext();
        for (JSpearSpecificationLanguageParser.LocalVariableContext var: localVariables) {
            JSpearType varType = var.expression().accept(new ExpressionTypeInference(new NestedTypeContext(context, symbols), errors));
            if (!varType.isError()) {
                context = new NestedTypeContext(new LocalVariableTypeContext(var.name.getText(), varType), context);
            }
        }
        return context;
    }

    @Override
    public Boolean visitVariablesDeclaration(JSpearSpecificationLanguageParser.VariablesDeclarationContext ctx) {
        boolean flag = true;
        ExpressionTypeInference inference = new ExpressionTypeInference(symbols, errors);
        for (JSpearSpecificationLanguageParser.VariableDeclarationContext var: ctx.variableDeclaration()) {
            if (checkIfNotDuplicated(var.name.getText(), var)) {
                JSpearType type = typeOf(var.type());
                symbols.recordVariable(var.name.getText(), type, var);
                if (type.isNumerical()||type.isAnArray()) {
                    if (var.from == null) {
                        errors.record(ParseUtil.rangeIntervalIsMissing(var.name));
                        flag = false;
                    } else {
                        type = (type.isAnArray()?JSpearType.REAL_TYPE:type);
                        flag = inference.checkType(type, var.from)&&inference.checkType(type, var.to);
                    }
                } else {
                    if (var.from != null) {
                        errors.record(ParseUtil.illegalRangeInterval(var.name));
                        flag = false;
                    }
                }
            } else {
                flag = false;
            }
        }
        return flag;
    }

    @Override
    public Boolean visitParameterDeclaration(JSpearSpecificationLanguageParser.ParameterDeclarationContext ctx) {
        if (checkIfNotDuplicated(ctx.name.getText(), ctx)) {
            JSpearType type = ctx.expression().accept(new ExpressionTypeInference(symbols, errors));
            if (!type.isError()) {
                symbols.recordParameter(ctx.name.getText(), type, ctx);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public Boolean visitConstantDeclaration(JSpearSpecificationLanguageParser.ConstantDeclarationContext ctx) {
        if (checkIfNotDuplicated(ctx.name.getText(), ctx)) {
            JSpearType type = ctx.expression().accept(new ExpressionTypeInference(symbols, errors));
            if (!type.isError()) {
                symbols.recordConstant(ctx.name.getText(), type, ctx);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public Boolean visitTypeDeclaration(JSpearSpecificationLanguageParser.TypeDeclarationContext ctx) {
        String customTypeName = ctx.name.getText();
        if (checkIfNotDuplicated(customTypeName, ctx)
            &&ctx.elements.stream().allMatch(e -> checkCustomTypeElement(ctx, e))) {
            symbols.recordCustomType(ctx);
            return true;
        }
        return false;
    }

    private boolean checkCustomTypeElement(JSpearSpecificationLanguageParser.TypeDeclarationContext ctx, JSpearSpecificationLanguageParser.TypeElementDeclarationContext e) {
        if (checkIfNotDuplicated(e.name.getText(), e)) {
            if (ctx.name.getText().equals(e.name.getText())) {
                this.errors.record(ParseUtil.duplicatedSymbol(ctx.name.getText(), ctx, e));
            } else {
                return true;
            }
        }
        return false;
    }
}
