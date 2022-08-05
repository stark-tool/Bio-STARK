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

import it.unicam.quasylab.jspear.SystemSpecification;
import it.unicam.quasylab.jspear.VariableRegistry;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Consumer;

public class SpecificationLoader {

    private final ParseErrorCollector errors = new ParseErrorCollector();

    public enum ElementType {
        VARIABLES_DECLARATION,
        ENVIRONMENT_DECLARATION, CONTROLLER_DECLARATION
    }


    private ParseTree getParseTree(CharStream source) {
        JSpearSpecificationLanguageLexer lexer = new JSpearSpecificationLanguageLexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        JSpearSpecificationLanguageParser parser = new JSpearSpecificationLanguageParser(tokens);
        ParseErrorListener errorListener = new ParseErrorListener(errors);
        parser.addErrorListener(errorListener);
        JSpearSpecificationLanguageParser.JSpearSpecificationModelContext parseTree = parser.jSpearSpecificationModel();
        if (errors.withErrors()) {
            return null;
        } else {
            return parseTree;
        }
    }

    public SystemSpecification loadSpecification(CharStream source) {
        ParseTree parseTree = getParseTree(source);
        if (parseTree != null) {
            return load(parseTree);
        } else {
            return null;
        }
    }

    public SystemSpecification loadSpecification(InputStream code) throws IOException {
        return loadSpecification(CharStreams.fromStream(code));
    }

    public SystemSpecification loadSpecification(String code) {
        return loadSpecification(CharStreams.fromString(code));
    }

    public SystemSpecification loadSpecification(File file) throws IOException {
        return loadSpecification(CharStreams.fromReader(new FileReader(file)));
    }

    private SystemSpecification load(ParseTree model) {
        doTask(this::checkSingleControllerDeclaration, model);
        doTask(this::checkSingleEnvironmentDeclaration, model);
        doTask(this::checkSingleVariableDefinition, model);
        doTask(this::validateModel, model);
        if (errors.withErrors()) {
            return null;
        }
        return new SystemSpecification(new VariableRegistry(), new HashMap<>(), new HashMap<>());
    }

    private void doTask(Consumer<ParseTree> task, ParseTree model) {
        if (!errors.withErrors()) {
            task.accept(model);
        }
    }

    private void validateModel(ParseTree model) {
        model.accept(new SpecificationLanguageValidator(this.errors));
    }

    private void checkSingleEnvironmentDeclaration(ParseTree model) {
        int elements = model.accept(new ElementBlockCounter(ElementType.ENVIRONMENT_DECLARATION));
        if (elements>1) {
            errors.record(ParseUtil.duplicatedEnvironmentDeclaration());
        }
    }

    private void checkSingleControllerDeclaration(ParseTree model) {
        int elements = model.accept(new ElementBlockCounter(ElementType.CONTROLLER_DECLARATION));
        if (elements>1) {
            errors.record(ParseUtil.duplicatedControllerDeclaration());
        }
    }

    private void checkSingleVariableDefinition(ParseTree model) {
        int elements = model.accept(new ElementBlockCounter(ElementType.VARIABLES_DECLARATION));
        if (elements == 0) {
            errors.record(ParseUtil.missingVariablesDeclaration());
        }
        if (elements>1) {
            errors.record(ParseUtil.duplicatedVariablesDeclaration());
        }
    }


    public static class ElementBlockCounter extends JSpearSpecificationLanguageBaseVisitor<Integer> {


        private final ElementType elementType;

        public ElementBlockCounter(ElementType elementType) {
            this.elementType = elementType;
        }

        @Override
        public Integer visitJSpearSpecificationModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext ctx) {
            int counter = 0;
            for (JSpearSpecificationLanguageParser.ElementContext e: ctx.element()) {
                counter += e.accept(this);
            }
            return counter;
        }

        @Override
        public Integer visitFunctionDeclaration(JSpearSpecificationLanguageParser.FunctionDeclarationContext ctx) {
            return 0;
        }

        @Override
        public Integer visitSystemDeclaration(JSpearSpecificationLanguageParser.SystemDeclarationContext ctx) {
            return 0;
        }

        @Override
        public Integer visitPenaltyDeclaration(JSpearSpecificationLanguageParser.PenaltyDeclarationContext ctx) {
            return 0;
        }

        @Override
        public Integer visitControllerDeclaration(JSpearSpecificationLanguageParser.ControllerDeclarationContext ctx) {
            return (elementType==ElementType.CONTROLLER_DECLARATION?1:0);
        }

        @Override
        public Integer visitVariablesDeclaration(JSpearSpecificationLanguageParser.VariablesDeclarationContext ctx) {
            return (elementType==ElementType.VARIABLES_DECLARATION ?1:0);
        }

        @Override
        public Integer visitParameterDeclaration(JSpearSpecificationLanguageParser.ParameterDeclarationContext ctx) {
            return 0;
        }

        @Override
        protected Integer defaultResult() {
            return 0;
        }

        @Override
        protected Integer aggregateResult(Integer aggregate, Integer nextResult) {
            return aggregate+nextResult;
        }
    }




}
