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

public class SpecificationLoader {

    private final ParseErrorCollector errors = new ParseErrorCollector();

    public enum ElementType {
        VARIABLES_DECLARATION,
        ENVIRONMENT_DECLARATION, CONTROLLER_DECLARATION
    }



    SystemSpecification load(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext model) {
        checkSingleVariableDefinition(model);
        checkSingleEnvironmentDeclaration(model);
        checkSingleControllerDeclaration(model);
        validateModel(model);
        return null;
    }

    private void validateModel(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext model) {
        model.accept(new SpecificationLanguageValidator(this.errors));
    }

    private void checkSingleEnvironmentDeclaration(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext model) {
        int elements = model.accept(new ElementBlockCounter(ElementType.ENVIRONMENT_DECLARATION));
        if (elements>1) {
            errors.record(ParseUtil.duplicatedEnvironmentDeclaration());
        }
    }

    private void checkSingleControllerDeclaration(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext model) {
        int elements = model.accept(new ElementBlockCounter(ElementType.CONTROLLER_DECLARATION));
        if (elements>1) {
            errors.record(ParseUtil.duplicatedControllerDeclaration());
        }
    }

    private void checkSingleVariableDefinition(JSpearSpecificationLanguageParser.JSpearSpecificationModelContext model) {
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
            return super.visitParameterDeclaration(ctx);
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
