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

import it.unicam.quasylab.jspear.controller.*;
import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageBaseVisitor;
import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageParser;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;
import it.unicam.quasylab.jspear.speclang.variables.JSpearExpressionEvaluationContext;
import it.unicam.quasylab.jspear.speclang.variables.JSpearNameResolver;
import it.unicam.quasylab.jspear.speclang.variables.JSpearStore;
import it.unicam.quasylab.jspear.speclang.variables.Variable;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;

import org.apache.commons.math3.random.RandomGenerator;

public class JSpearControllerBehaviourGenerator extends JSpearSpecificationLanguageBaseVisitor<Controller> {

    private final ControllerRegistry registy;

    private final VariableAllocation allocation;

    private final JSpearExpressionEvaluationContext context;

    private final JSpearNameResolver resolver;



    public JSpearControllerBehaviourGenerator(ControllerRegistry registy, VariableAllocation allocation, JSpearExpressionEvaluationContext context, JSpearNameResolver resolver) {
        this.registy = registy;
        this.allocation = allocation;
        this.context = context;
        this.resolver = resolver;
    }


    @Override
    public Controller visitParallelController(JSpearSpecificationLanguageParser.ParallelControllerContext ctx) {
        Controller result = null;
        for(Token controllerName: ctx.components) {
            Controller nextParallel = registy.reference(controllerName.getText());
            result = (result == null?nextParallel:new ParallelController(result, nextParallel));
        }
        return result;
    }

    @Override
    public Controller visitExecBehaviour(JSpearSpecificationLanguageParser.ExecBehaviourContext ctx) {
        return new ExecController(registy.reference(ctx.target.getText()));
    }

    @Override
    public Controller visitStepBehaviour(JSpearSpecificationLanguageParser.StepBehaviourContext ctx) {
        return new StepController(getStepFunction(ctx.steps), registy.reference(ctx.target.getText()));
    }

    @Override
    public Controller visitIfThenElseBehaviour(JSpearSpecificationLanguageParser.IfThenElseBehaviourContext ctx) {
        return Controller.ifThenElse(getPredicate(ctx.guard), ctx.thenBranch.accept(this), ctx.elseBranch.accept(this));
    }

    private BiPredicate<RandomGenerator, DataState> getPredicate(JSpearSpecificationLanguageParser.ExpressionContext guard) {
        if (guard == null) {
            return (rg, ds) -> true;
        }
        JSpearExpressionEvaluator evaluator = new JSpearExpressionEvaluator(context, resolver);
        JSpearExpressionEvaluationFunction guardPredicate = guard.accept(evaluator);
        return (rg, ds) -> JSpearValue.isTrue(guardPredicate.eval(rg, allocation.getStore(ds)));
    }

    @Override
    public Controller visitVariableAssignmentBehaviour(JSpearSpecificationLanguageParser.VariableAssignmentBehaviourContext ctx) {
        BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> assignment = generateAssignment(ctx.guard, ctx.target, ctx.value);
        return Controller.doAssignment((rg, ds) -> assignment.apply(rg, allocation.getStore(ds)), ctx.next.accept(this));
    }


    public ToIntBiFunction<RandomGenerator, DataState> getStepFunction(JSpearSpecificationLanguageParser.ExpressionContext steps) {
        if (steps == null) {
            return (rg, ds) -> 0;
        }
        JSpearExpressionEvaluator evaluator = new JSpearExpressionEvaluator(context, resolver);
        JSpearExpressionEvaluationFunction stepFunction = steps.accept(evaluator);
        return (rg, ds) -> JSpearValue.intValue(stepFunction.eval(rg, allocation.getStore(ds)));
    }


    private BiFunction<RandomGenerator, JSpearStore, List<DataStateUpdate>> generateAssignment(JSpearSpecificationLanguageParser.ExpressionContext guard, JSpearSpecificationLanguageParser.VarExpressionContext target, JSpearSpecificationLanguageParser.ExpressionContext value) {
        JSpearExpressionEvaluator expressionEvaluator = new JSpearExpressionEvaluator(context, resolver);
        Variable targetVariable = resolver.get(getVariableName(target.name.getText()));
        if (target.first == null) {
            return allocation.generateAssignment(guard.accept(expressionEvaluator), targetVariable, value.accept(expressionEvaluator));
        }
        if (target.last == null) {
            return allocation.generateAssignment(guard.accept(expressionEvaluator), targetVariable, target.first.accept(expressionEvaluator), value.accept(expressionEvaluator));
        }
        return allocation.generateAssignment(guard.accept(expressionEvaluator), targetVariable, target.first.accept(expressionEvaluator), target.last.accept(expressionEvaluator), value.accept(expressionEvaluator));
    }

    private String getVariableName(String name) {
        return name.substring(0, name.length()-1);
    }
}
