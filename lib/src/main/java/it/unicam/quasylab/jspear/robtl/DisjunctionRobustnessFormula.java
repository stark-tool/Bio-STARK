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

/**
 * Defines the disjunction of two given RobTL formulae.
 */
public final class DisjunctionRobustnessFormula implements RobustnessFormula {

    private final RobustnessFormula leftFormula;
    private final RobustnessFormula rightFormula;

    /**
     * It takes two RobTL formulae as parameters.
     *
     * @param leftFormula a RobTL formula
     * @param rightFormula a RobTL formula.
     */
    public DisjunctionRobustnessFormula(RobustnessFormula leftFormula, RobustnessFormula rightFormula) {
        this.leftFormula = leftFormula;
        this.rightFormula = rightFormula;
    }

    @Override
    public boolean eval(int sampleSize, int step, EvolutionSequence sequence, boolean parallel) {
        return leftFormula.eval(sampleSize, step, sequence, parallel)||rightFormula.eval(sampleSize, step, sequence, parallel);
    }

    @Override
    public <T> RobustnessFunction<T> eval(RobustnessFormulaVisitor<T> evaluator) {
        return evaluator.evalDisjunction(this);
    }

    /**
     * Returns the RobTL formula passed as first parameter to this formula.
     *
     * @return parameter <code>leftFormula</code>.
     */
    public RobustnessFormula getLeftFormula() {
        return leftFormula;
    }

    /**
     * Returns the RobTL formula passed as second parameter to this formula.
     *
     * @return parameter <code>rightFormula</code>.
     */
    public RobustnessFormula getRightFormula() {
        return rightFormula;
    }

}
