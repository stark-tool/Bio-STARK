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

import java.util.stream.IntStream;

public final class UntilRobustnessFormula implements RobustnessFormula {

    private final RobustnessFormula leftFormula;
    private final int from;
    private final int to;
    private final RobustnessFormula rightFormula;

    public UntilRobustnessFormula(RobustnessFormula leftFormula, int from, int to, RobustnessFormula rightFormula) {
        if ((from<0)||(to<0)||(from>=to)) {
            throw new IllegalArgumentException();
        }
        this.leftFormula = leftFormula;
        this.from = from;
        this.to = to;
        this.rightFormula = rightFormula;
    }

    @Override
    public boolean eval(int sampleSize, int step, EvolutionSequence sequence) {
        return IntStream.range(from+step, to+step).parallel().anyMatch(
                i -> rightFormula.eval(sampleSize, i, sequence) &&
                        IntStream.range(step, i).allMatch(j -> leftFormula.eval(sampleSize, j, sequence))
        );
    }
}
