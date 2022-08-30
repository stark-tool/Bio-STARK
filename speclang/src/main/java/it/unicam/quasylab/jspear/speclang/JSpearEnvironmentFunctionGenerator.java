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

import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.speclang.values.JSpearBoolean;
import it.unicam.quasylab.jspear.speclang.variables.*;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.BiFunction;

public class JSpearEnvironmentFunctionGenerator extends JSpearSpecificationLanguageBaseVisitor<BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>>> {

    private final JSpearNameResolver resolver;

    private final JSpearExpressionEvaluationContext context;

    private final VariableAllocation allocation;
    private final JSpearExpressionEvaluator expressionEvaluator;

    private BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> environmentFunction;


    public JSpearEnvironmentFunctionGenerator(JSpearNameResolver resolver, JSpearExpressionEvaluationContext context, VariableAllocation allocation) {
        this.resolver = resolver;
        this.context = context;
        this.allocation = allocation;
        this.expressionEvaluator = new JSpearExpressionEvaluator(context, resolver);
    }

    @Override
    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> visitChildren(RuleNode node) {
        return environmentFunction;
    }

    @Override
    protected BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> defaultResult() {
        return environmentFunction;
    }

    @Override
    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> visitJSpearSpecificationModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return environmentFunction;
    }

    @Override
    public BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> visitEnvironmentDeclaration(JSpearSpecificationLanguageParser.EnvironmentDeclarationContext ctx) {
        List<LocalVariableAssignment> localVariables = generateLocalVariablesInitialisationFunctions(ctx.localVariables);
        List<BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>>> assigments = ctx.assignments.stream().map(this::generateAssignment).toList();
        environmentFunction = (rg, s) -> {
            JSpearStore currentStore = s;
            for (LocalVariableAssignment lva: localVariables) {
                currentStore = new JSpearLetContextDataStore(lva.variable, lva.expressionEvaluationFunction.eval(rg, currentStore), currentStore);
            }
            List<DataStateUpdate> result = new LinkedList<>();
            for (BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> assigment : assigments) {
                result.addAll(assigment.apply(rg, currentStore));
            }
            return result;
        };
        return environmentFunction;
    }

    private BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(JSpearSpecificationLanguageParser.VariableAssignmentContext assignment) {
        JSpearSpecificationLanguageParser.VarExpressionContext varExpression = assignment.target;
        Variable targetVariable = resolver.get(getVariableName(varExpression.name.getText()));
        JSpearExpressionEvaluationFunction guardEvaluation = (assignment.guard==null?JSpearExpressionEvaluationFunction.of(JSpearBoolean.TRUE):assignment.guard.accept(expressionEvaluator));
        if (varExpression.first == null) {
            return allocation.generateAssignment(guardEvaluation, targetVariable, assignment.value.accept(expressionEvaluator));
        }
        if (varExpression.last == null) {
            return allocation.generateAssignment(guardEvaluation, targetVariable, varExpression.first.accept(expressionEvaluator), assignment.value.accept(expressionEvaluator));
        }
        return allocation.generateAssignment(guardEvaluation, targetVariable, varExpression.first.accept(expressionEvaluator), varExpression.last.accept(expressionEvaluator), assignment.value.accept(expressionEvaluator));
    }

    private String getVariableName(String name) {
        return name.substring(0, name.length()-1);
    }

    private List<LocalVariableAssignment> generateLocalVariablesInitialisationFunctions(List<JSpearSpecificationLanguageParser.LocalVariableContext> localVariables) {
        List<LocalVariableAssignment> result = new LinkedList<>();
        for (JSpearSpecificationLanguageParser.LocalVariableContext lv: localVariables) {
            result.add(generateLocalVariableInitialisationFunction(lv));
        }
        return result;
    }

    private LocalVariableAssignment generateLocalVariableInitialisationFunction(JSpearSpecificationLanguageParser.LocalVariableContext lv) {
        return new LocalVariableAssignment(this.resolver.getOrRegister(lv.name.getText()), lv.expression().accept(expressionEvaluator));
    }


    private static class LocalVariableAssignment {

        private final Variable variable;
        private final JSpearExpressionEvaluationFunction expressionEvaluationFunction;

        private LocalVariableAssignment(Variable variable, JSpearExpressionEvaluationFunction expressionEvaluationFunction) {
            this.variable = variable;
            this.expressionEvaluationFunction = expressionEvaluationFunction;
        }

        public Variable getVariable() {
            return variable;
        }

        public JSpearExpressionEvaluationFunction getExpressionEvaluationFunction() {
            return expressionEvaluationFunction;
        }

    }

}
