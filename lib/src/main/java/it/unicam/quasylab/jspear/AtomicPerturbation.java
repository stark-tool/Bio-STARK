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

import java.util.Optional;

/**
 * An atomic perturbation is used to apply a given function.
 *
 * @param perturbationFunction perturbation function to apply.
 */
public record AtomicPerturbation(int afterSteps, DataStateFunction perturbationFunction) implements Perturbation {

    @Override
    public Optional<DataStateFunction> effect() {
        if (afterSteps <= 0) {
            return Optional.of(perturbationFunction);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Perturbation step() {
        if (afterSteps <= 0) {
            return Perturbation.NONE;
        } else {
            return new AtomicPerturbation(afterSteps-1, perturbationFunction);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }


}
