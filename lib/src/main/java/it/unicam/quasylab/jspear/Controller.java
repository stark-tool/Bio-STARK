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

import java.util.function.Predicate;

/**
 * A controller is a process that, depending on the values in a data space performs an action. The latter consists
 * in an update of the data space and a new controller that should be used at the next step.
 */
@FunctionalInterface
public interface Controller {

    static Controller ifThenElse(Predicate<DataState> pred, Controller thenController, Controller elseController) {
        return new IfThenElseController(pred, thenController, elseController);
    }

    static Controller doAction(DataStateFunction act, Controller next) {
        return new ActionController(act, next);
    }

    static Controller doTick(Controller next) {
        return new ActionController(DataStateFunction.TICK_FUNCTION, next);
    }

    static Controller doTick(int i, Controller next) {
        if (i<1) {
            return next;
        } else {
            return doTick(doTick(i-1, next));
        }
    }

    EffectStep<Controller> next(RandomGenerator rg, DataState state);

}
