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

package it.unicam.quasylab.jspear.perturbation;

import it.unicam.quasylab.jspear.ds.DataStateFunction;

import java.util.Optional;

/**
 * Identifies a perturbation that must be performed for a given number of times.
 *
 * @param replica number of times to repeat the perturbation.
 * @param body the perturbation to apply.
 */
public record IterativePerturbation(int replica, Perturbation body) implements Perturbation {


    @Override
    public Optional<DataStateFunction> effect() {
        if (replica>0) {
            return body.effect();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Perturbation step() {
        if (replica > 0) {
            return new SequentialPerturbation(body.step(), new IterativePerturbation(replica-1, body));
        } else {
            return Perturbation.NONE;
        }
    }

    @Override
    public boolean isDone() {
        return replica<=0;
    }
}
