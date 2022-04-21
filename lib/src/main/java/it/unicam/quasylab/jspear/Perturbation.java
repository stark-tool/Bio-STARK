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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;

/**
 * This interface model a perturbation applied to a given sample set.
 */
public sealed interface Perturbation permits NonePerturbation, AtomicPerturbation, SequentialPerturbation, IterativePerturbation {


    Perturbation NONE = new NonePerturbation();

    /**
     * Returns the effect of this perturbation at the current time.
     *
     * @return the effect of this perturbation at the current time.
     */
    Optional<DataStateFunction> effect();


    /**
     * Returns the perturbation active after one computational step.
     *
     * @return the perturbation active after one computational step.
     */
    Perturbation step();


    /**
     * Returns true if this perturbation has been terminated its effects.
     *
     * @return true if this perturbation has been terminated its effects.
     */
    boolean isDone();


    default DataState apply(RandomGenerator rg, DataState state) {
        Optional<DataStateFunction> effect = effect();
        if (effect.isPresent()) {
            return effect.get().apply(rg, state);
        } else {
            return state;
        }
    }

    default SystemState apply(RandomGenerator rg, SystemState state) {
        return new PerturbedSystem(state.setDataState(this.apply(rg, state.getDataState())), this);
    }

}
