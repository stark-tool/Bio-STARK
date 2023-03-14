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

import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.robtl.RobustnessFormula;

import java.util.Map;

public class SystemSpecification {

    private final ControlledSystem system;

    private final Map<String, DataStateExpression> penalties;

    private final Map<String, RobustnessFormula> formulas;


    public SystemSpecification(ControlledSystem system, Map<String, DataStateExpression> penalties, Map<String, RobustnessFormula> formulas) {
        this.system = system;
        this.penalties = penalties;
        this.formulas = formulas;
    }

    public String[] getPenalties() {
        return penalties.keySet().toArray(new String[0]);
    }

    public DataStateExpression getPenalty(String name) {
        return penalties.get(name);
    }

    public ControlledSystem getSystem() {
        return system;
    }

    public String[] getFormulas() { return formulas.keySet().toArray(new String[0]); }

    public RobustnessFormula getFormula(String name) {
        return formulas.get(name);
    }
}
