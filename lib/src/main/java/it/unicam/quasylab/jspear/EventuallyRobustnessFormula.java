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

public final class EventuallyRobustnessFormula implements RobustnessFormula {
    private final RobustnessFormula arg;
    private final int from;
    private final int to;

    public EventuallyRobustnessFormula(RobustnessFormula arg, int from, int to) {
        this.arg = arg;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean eval(int sampleSize, int step, EvolutionSequence sequence, boolean isParallel) {
        if (isParallel) {
            return IntStream.of(from, to).parallel().anyMatch(i -> arg.eval(sampleSize, step+i, sequence));
        } else {
            return IntStream.of(from, to).anyMatch(i -> arg.eval(sampleSize, step+i, sequence));
        }
    }

}
