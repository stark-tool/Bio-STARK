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

package it.unicam.quasylab.jspear.robtl.old;

import it.unicam.quasylab.jspear.EvolutionSequence;
import it.unicam.quasylab.jspear.robtl.TruthValues;

public final class EventuallyThreeValuedFormula implements ThreeValuedFormula {
    private final ThreeValuedFormula formula;
    private final int from;
    private final int to;

    public EventuallyThreeValuedFormula(ThreeValuedFormula formula, int from, int to) {
        this.formula = formula;
        this.from = from;
        this.to = to;
    }

    @Override
    public TruthValues eval(int sampleSize, int step, EvolutionSequence sequence) {
        TruthValues value = TruthValues.FALSE;
        for(int i = from+step; i<to+step; i++){
            value = TruthValues.or(value, formula.eval(sampleSize, i, sequence));
            if(value==TruthValues.TRUE){i=to+step;}
        }
        return value;
        //return IntStream.of(from, to).parallel().anyMatch(i -> formula.eval(sampleSize, step+i, sequence));
    }

}
