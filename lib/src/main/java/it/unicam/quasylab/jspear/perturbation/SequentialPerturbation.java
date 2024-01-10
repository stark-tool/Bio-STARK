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

package it.unicam.quasylab.jspear.perturbation;

import it.unicam.quasylab.jspear.ds.DataStateFunction;

import java.util.Optional;

/**
 * Aggregate a list of perturbations that must be performed one after the other.
 *
 * @param first first perturbation in the sequence.
 * @param second second perturbation in the sequence.
 */
public record SequentialPerturbation(Perturbation first, Perturbation second) implements Perturbation {


    @Override
    public Optional<DataStateFunction> effect() {
        if (first.isDone()) {
            return second.effect();
        } else {
            return first.effect();
        }
    }

    @Override
    public Perturbation step() {
        if (first().isDone()) {
            return second.step();
        } else {
            return new SequentialPerturbation(first.step(), second);
        }
    }

    @Override
    public boolean isDone() {
        return first().isDone()&&second.isDone();
    }
}
