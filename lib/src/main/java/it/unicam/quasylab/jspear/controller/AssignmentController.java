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

package it.unicam.quasylab.jspear.controller;

import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class AssignmentController implements Controller {
    private final BiPredicate<RandomGenerator, DataState> guard;
    private final BiFunction<RandomGenerator, DataState, List<DataStateUpdate>> assignment;
    private final Controller nextController;

    public AssignmentController(BiPredicate<RandomGenerator, DataState> guard, BiFunction<RandomGenerator, DataState, List<DataStateUpdate>> assignment, Controller nextController) {
        this.guard = guard;
        this.assignment = assignment;
        this.nextController = nextController;
    }

    @Override
    public EffectStep<Controller> next(RandomGenerator rg, DataState state) {
        EffectStep<Controller> stepEffect = nextController.next(rg, state);
        if (guard.test(rg, state)) {
            return stepEffect.applyBefore(assignment.apply(rg, state));
        } else {
            return stepEffect;
        }
    }
}
