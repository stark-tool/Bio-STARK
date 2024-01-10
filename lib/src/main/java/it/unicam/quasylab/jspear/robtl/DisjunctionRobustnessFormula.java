/*
 * STARK: Software Tool for the Analysis of Robustness in the unKnown environment
 *
 *                Copyright (C) 2023.
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

public final class DisjunctionRobustnessFormula implements RobustnessFormula {

    private final RobustnessFormula leftFormula;
    private final RobustnessFormula rightFormula;

    public DisjunctionRobustnessFormula(RobustnessFormula leftFormula, RobustnessFormula rightFormula) {
        this.leftFormula = leftFormula;
        this.rightFormula = rightFormula;
    }


    @Override
    public <T> RobustnessFunction<T> eval(RobustnessFormulaVisitor<T> evaluator) {
        return evaluator.evalDisjunction(this);
    }

    public RobustnessFormula getLeftFormula() {
        return leftFormula;
    }

    public RobustnessFormula getRightFormula() {
        return rightFormula;
    }

}
