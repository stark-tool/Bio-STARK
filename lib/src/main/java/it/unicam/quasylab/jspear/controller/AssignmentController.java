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

package it.unicam.quasylab.jspear.controller;

import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.function.BiFunction;

public class AssignmentController implements Controller {
    private final BiFunction<RandomGenerator, DataState, List<DataStateUpdate>> assignment;
    private final Controller nextController;

    public AssignmentController(BiFunction<RandomGenerator, DataState, List<DataStateUpdate>> assignment, Controller nextController) {
        this.assignment = assignment;
        this.nextController = nextController;
    }

    @Override
    public EffectStep<Controller> next(RandomGenerator rg, DataState state) {
        return nextController.next(rg, state).applyBefore(assignment.apply(rg, state));
    }
}
