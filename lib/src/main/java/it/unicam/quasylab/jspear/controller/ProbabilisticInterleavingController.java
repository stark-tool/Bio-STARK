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

/**
 * Represents a controller consisting of two controllers running in probabilistic interleaving. At each step the
 * effects and the transitions of the controller chosen probabilistically are applied.
 */
public class ProbabilisticInterleavingController implements Controller {

    private final int p;
    private final Controller leftController;
    private final Controller rightController;

    /**
     * Creates a new controller consisting by the probabilistic interleaving of the two given controllers.
     *
     * @param p a probability weight.
     * @param leftController a controller.
     * @param rightController a controller.
     */
    public ProbabilisticInterleavingController(int p, Controller leftController, Controller rightController) {
        this.p = p;
        this.leftController = leftController;
        this.rightController = rightController;
    }

    @Override
    public EffectStep<Controller> next(RandomGenerator rg, DataState state) {
        double p = rg.nextDouble();
        if (p <= this.p) {
            EffectStep<Controller> effectStep = this.leftController.next(rg, state);
            List<DataStateUpdate> updates = effectStep.effect();
            ProbabilisticInterleavingController c = new ProbabilisticInterleavingController(this.p, effectStep.next(), this.rightController);
            return new EffectStep<>(updates, c);
        }
        else{
            EffectStep<Controller> effectStep = this.rightController.next(rg, state);
            List<DataStateUpdate> updates = effectStep.effect();
            ProbabilisticInterleavingController c = new ProbabilisticInterleavingController(this.p, this.leftController,effectStep.next());
            return new EffectStep<>(updates, c);
        }
    }
}
