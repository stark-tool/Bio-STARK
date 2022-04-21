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

/**
 * Represents a controller that executes a given action on the data set and then evolves to
 * another one.
 *
 */
public class ActionController implements Controller {

    private final DataStateFunction action;
    private final Controller next;

    /**
     * Creates the controller that execute the given action and then behaves like <code>next</code>.
     *
     * @param action effect on data state.
     * @param next controller enabled after the action execution.
     */
    public ActionController(DataStateFunction action, Controller next) {
        this.action = action;
        this.next = next;
    }

    @Override
    public EffectStep<Controller> next(RandomGenerator rg, DataState state) {
        return new EffectStep(action, next);
    }
}
