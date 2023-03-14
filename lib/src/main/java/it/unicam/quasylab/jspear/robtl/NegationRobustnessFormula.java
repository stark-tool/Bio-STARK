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

package it.unicam.quasylab.jspear.robtl;

import it.unicam.quasylab.jspear.EvolutionSequence;

public final class NegationRobustnessFormula implements RobustnessFormula {

    private final RobustnessFormula argument;

    public NegationRobustnessFormula(RobustnessFormula argument) {
        this.argument = argument;
    }



    @Override
    public boolean eval(int sampleSize, int step, EvolutionSequence sequence, boolean parallel) {
        return !argument.eval(sampleSize, step, sequence, parallel);
    }

    @Override
    public <T> RobustnessFunction<T> eval(RobustnessFormulaVisitor<T> evaluator) {
        return evaluator.evalNegation(this);
    }

    public RobustnessFormula getArgument() {
        return argument;
    }
}