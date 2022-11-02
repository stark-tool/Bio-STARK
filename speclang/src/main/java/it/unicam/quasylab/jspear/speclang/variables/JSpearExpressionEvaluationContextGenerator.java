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

package it.unicam.quasylab.jspear.speclang.variables;

import it.unicam.quasylab.jspear.speclang.semantics.JSpearExpressionEvaluationFunction;
import it.unicam.quasylab.jspear.speclang.semantics.JSpearExpressionEvaluator;
import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageBaseVisitor;
import it.unicam.quasylab.jspear.speclang.JSpearSpecificationLanguageParser;
import it.unicam.quasylab.jspear.speclang.types.JSpearCustomType;
import it.unicam.quasylab.jspear.speclang.values.JSpearValue;

import java.util.Arrays;
import java.util.Map;

public class JSpearExpressionEvaluationContextGenerator extends JSpearSpecificationLanguageBaseVisitor<JSpearExpressionEvaluationContext> {

    private final JSpearExpressionEvaluationContext context;

    public JSpearExpressionEvaluationContextGenerator(Map<String, JSpearValue> parameters) {
        this.context = new JSpearExpressionEvaluationContext(parameters);
    }

    @Override
    public JSpearExpressionEvaluationContext visitJSpearSpecificationModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return context;
    }

    @Override
    public JSpearExpressionEvaluationContext visitFunctionDeclaration(JSpearSpecificationLanguageParser.FunctionDeclarationContext ctx) {
        JSpearNameResolver resolver = new JSpearNameResolver();
        ctx.arguments.forEach(a -> resolver.getOrRegister(a.name.getText()));
        JSpearExpressionEvaluationFunction functionBody = ctx.functionBlock().accept(new JSpearExpressionEvaluator(context, resolver));
        context.recordFunction(ctx.name.getText(), (rg, args) -> functionBody.eval(rg, new JSpearArrayDataStore(args)));
        return context;
    }

    @Override
    public JSpearExpressionEvaluationContext visitTypeDeclaration(JSpearSpecificationLanguageParser.TypeDeclarationContext ctx) {
        String typeName = ctx.name.getText();
        String[] elements = ctx.elements.stream().map(t -> t.name.getText()).toArray(String[]::new);
        JSpearCustomType type = new JSpearCustomType(typeName, elements);
        context.recordType(typeName, type);
        Arrays.stream(elements).forEach(name -> context.set(name, type.getValueOf(name)));
        return context;
    }

    @Override
    public JSpearExpressionEvaluationContext visitParameterDeclaration(JSpearSpecificationLanguageParser.ParameterDeclarationContext ctx) {
        String name = ctx.name.getText();
        if (!context.isDefined(name)) {
            this.context.set(name, ctx.expression().accept(new JSpearExpressionEvaluator(context, new JSpearNameResolver())).eval());
        }
        return context;
    }

    @Override
    public JSpearExpressionEvaluationContext visitConstantDeclaration(JSpearSpecificationLanguageParser.ConstantDeclarationContext ctx) {
        this.context.set(ctx.name.getText(), ctx.expression().accept(new JSpearExpressionEvaluator(context, new JSpearNameResolver())).eval());
        return context;
    }
}
