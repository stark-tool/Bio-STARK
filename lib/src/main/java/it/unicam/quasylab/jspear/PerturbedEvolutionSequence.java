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

import it.unicam.quasylab.jspear.ds.DataStateFunction;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Optional;

public class PerturbedEvolutionSequence extends EvolutionSequence {

    private Perturbation p;

    /* metodo cambiato il 17 gennaio 2023
    protected PerturbedEvolutionSequence(SimulationMonitor monitor, RandomGenerator rg, List<SampleSet<SystemState>> originalSequence, SampleSet<SystemState> last, Perturbation p) {
        super(monitor, rg, originalSequence,last);
        this.p = p;
        doAdd(doApply(last));
    }
    */

    protected PerturbedEvolutionSequence(SimulationMonitor monitor, RandomGenerator rg, List<SampleSet<SystemState>> originalSequence, SampleSet<SystemState> secondLast, SampleSet<SystemState> last, Perturbation p) {
        //metodo aggiunto il 17 gennaio 2023
        super(monitor, rg, originalSequence, secondLast);
        this.p = p;
        doAdd(doApply(last));
    }

    protected PerturbedEvolutionSequence(SimulationMonitor monitor, RandomGenerator rg, List<SampleSet<SystemState>> originalSequence, SampleSet<SystemState> last, Perturbation p) {
        // metodo aggiunto il 17 gennaio 2023
        super(monitor, rg);
        this.p = p;
        doAdd(doApply(last));
    }

    //ulteriori costruttori per gestire il caso senza monitor, 17 gennaio 2023
    protected PerturbedEvolutionSequence(RandomGenerator rg, List<SampleSet<SystemState>> originalSequence, SampleSet<SystemState> secondLast, SampleSet<SystemState> last, Perturbation p) {
        super(null, rg, originalSequence, secondLast);
        this.p = p;
        doAdd(doApply(last));
    }

    protected PerturbedEvolutionSequence(RandomGenerator rg, List<SampleSet<SystemState>> originalSequence, SampleSet<SystemState> last, Perturbation p) {
        super(null, rg);
        this.p = p;
        doAdd(doApply(last));
    }

    @Override
    protected synchronized SampleSet<SystemState> generateNextStep() {
        this.p = this.p.step();
        return doApply(super.generateNextStep());
    }


    protected synchronized SampleSet<SystemState> doApply(SampleSet<SystemState> sample) {
        Optional<DataStateFunction> perturbationFunction = this.p.effect();
        if (perturbationFunction.isPresent()) {
            return sample.apply(getRandomGenerator(), (rg, s) -> s.apply(rg, perturbationFunction.get()));
        } else {
            return sample;
        }
    }
}
