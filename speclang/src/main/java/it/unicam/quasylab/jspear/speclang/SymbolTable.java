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

import it.unicam.quasylab.jspear.speclang.types.JSpearCustomType;
import it.unicam.quasylab.jspear.speclang.types.JSpearType;
import it.unicam.quasylab.jspear.speclang.types.TypeContext;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SymbolTable implements TypeContext {

    private final Map<String, ParserRuleContext> symbols = new HashMap<>();
    private final Map<String, JSpearSpecificationLanguageParser.FunctionDeclarationContext> functions = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.VariableDeclarationContext> variables = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.ConstantDeclarationContext> constants = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.ParameterDeclarationContext> parameters = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.PenaltyDeclarationContext> penalties = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.StateDeclarationContext> states = new HashMap<>();

    private final Map<String, JSpearSpecificationLanguageParser.SystemDeclarationContext> systems = new HashMap<>();

    private final Map<String, JSpearType> typesOfRefereneableElements = new HashMap<>();
    private final Map<String, JSpearType[]> functionArguments = new HashMap<>();
    private final Map<String, JSpearType> functionReturnTypes = new HashMap<>();
    private final Map<String, JSpearType> custumTypes = new HashMap<>();

    private final Set<String> elementsOfDeclaredTypes = new HashSet<>();

    public ParserRuleContext get(String name) {
        return symbols.get(name);
    }

    public void recordFunction(String name, JSpearType[] arguments, JSpearType returnType, JSpearSpecificationLanguageParser.FunctionDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.functions.put(name, ctx);
        this.functionArguments.put(name, arguments);
        this.functionReturnTypes.put(name, returnType);
    }

    public JSpearSpecificationLanguageParser.FunctionDeclarationContext getFunctionDeclaration(String name) {
        return this.functions.get(name);
    }


    @Override
    public boolean isDefined(String name) {
        return symbols.containsKey(name);
    }

    @Override
    public boolean isReferenceable(String name) {
        return isAVariable(name)||isAParameter(name)||isAConstant(name)||isTypeElement(name);
    }

    private boolean isTypeElement(String name) {
        return this.elementsOfDeclaredTypes.contains(name);
    }

    public boolean isAConstant(String name) {
        return constants.containsKey(name);
    }

    public boolean isAParameter(String name) {
        return parameters.containsKey(name);
    }

    public boolean isAVariable(String name) {
        return variables.containsKey(name);
    }

    @Override
    public JSpearType getTypeOf(String name) {
        return this.typesOfRefereneableElements.get(name);
    }

    @Override
    public boolean isAFunction(String functionName) {
        return this.functions.containsKey(functionName);
    }

    @Override
    public JSpearType[] getArgumentsType(String functionName) {
        return this.functionArguments.get(functionName);
    }

    @Override
    public JSpearType getReturnType(String functionName) {
        return this.functionReturnTypes.get(functionName);
    }

    public void recordConstant(String name, JSpearType type, JSpearSpecificationLanguageParser.ConstantDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.constants.put(name, ctx);
        this.typesOfRefereneableElements.put(name, type);
    }

    public void recordParameter(String name, JSpearType type, JSpearSpecificationLanguageParser.ParameterDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.parameters.put(name, ctx);
        this.typesOfRefereneableElements.put(name, type);
    }

    public void recordVariable(String name, JSpearType type, JSpearSpecificationLanguageParser.VariableDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.variables.put(name, ctx);
        this.typesOfRefereneableElements.put(name, type);
    }

    public void recordPenaltyFunction(String name, JSpearSpecificationLanguageParser.PenaltyDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.penalties.put(name, ctx);
    }

    public void recordControllerState(String name, JSpearSpecificationLanguageParser.StateDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
        this.states.put(name, ctx);
    }

    public boolean isAState(String name) {
        return this.states.containsKey(name);
    }

    public void recordSystemDeclaration(String name, JSpearSpecificationLanguageParser.SystemDeclarationContext ctx) {
        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(name, ctx);
    }

    public void recordCustomType(JSpearSpecificationLanguageParser.TypeDeclarationContext ctx) {
        String customTypeName = ctx.name.getText();
        String[] customTypeElements = ctx.elements.stream().map(e -> e.name.getText()).toArray(String[]::new);
        if (isDefined(customTypeName)|| Stream.of(customTypeElements).anyMatch(this::isDefined)) {
            throw new IllegalArgumentException();
        }
        this.symbols.put(customTypeName, ctx);
        ctx.elements.forEach(e -> this.symbols.put(e.name.getText(), e));
        JSpearType customType = new JSpearCustomType(customTypeName, customTypeElements);
        this.custumTypes.put(customTypeName, customType);
        Stream.of(customTypeElements).forEach(e -> {
            this.typesOfRefereneableElements.put(e, customType);
            this.elementsOfDeclaredTypes.add(e);
        });
    }

    public boolean isACustomType(String typeName) {
        return this.custumTypes.containsKey(typeName);
    }

    public JSpearType getCustomType(String typeName) {
        return this.custumTypes.get(typeName);
    }
}
