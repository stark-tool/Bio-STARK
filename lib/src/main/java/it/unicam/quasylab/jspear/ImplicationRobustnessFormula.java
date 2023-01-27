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

package it.unicam.quasylab.jspear;

public final class ImplicationRobustnessFormula implements RobustnessFormula {

    private final RobustnessFormula leftFormula;
    private final RobustnessFormula rightFormula;

    public ImplicationRobustnessFormula(RobustnessFormula leftFormula, RobustnessFormula rightFormula) {
        this.leftFormula = leftFormula;
        this.rightFormula = rightFormula;
    }

    @Override
    public boolean eval(int sampleSize, int step, EvolutionSequence sequence, boolean parallel) {
        return (!leftFormula.eval(sampleSize, step, sequence,parallel))||rightFormula.eval(sampleSize, step, sequence,parallel);
    }


}
