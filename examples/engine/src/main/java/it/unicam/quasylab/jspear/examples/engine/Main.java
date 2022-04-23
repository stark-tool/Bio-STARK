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

package it.unicam.quasylab.jspear.examples.engine;

import it.unicam.quasylab.jspear.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

public class Main {

    public final static String[] VARIABLES =
            new String[] { "P1", "P2", "P3", "P4", "P5", "P5", "stress",  "temp", "cool", "speed"};
    public final static double ON = 0;
    public final static double OFF = 1;
    public final static double SLOW = 2;
    public final static double HALF = 3;
    public final static double FULL = 4;
    public final static double OK = 5;
    public final static double HOT = 6;
    public final static double LOW = 7;
    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 0;
    private static final double STRESS_INCR = 0.1;
    private static final VariableRegistry variableRegistry = new VariableRegistry(VARIABLES);
    private static final Variable p1 = variableRegistry.getVariable("P1");
    private static final Variable p2 = variableRegistry.getVariable("P2");
    private static final Variable p3 = variableRegistry.getVariable("P3");
    private static final Variable p4 = variableRegistry.getVariable("P4");
    private static final Variable p5 = variableRegistry.getVariable("P5");
    private static final Variable p6 = variableRegistry.getVariable("P6");
    private static final Variable stress = variableRegistry.getVariable("stress");
    private static final Variable temp = variableRegistry.getVariable("temp");
    private static final Variable cool = variableRegistry.getVariable("cool");
    private static final Variable speed = variableRegistry.getVariable("speed");



    public static void main(String[] args) {
        Controller controller = getController();
        DataState state = getInitialState();
        ControlledSystem system = new ControlledSystem(controller, (rg, ds) -> ds.set(getEnvironmentUpdates(rg, ds)), state);
        EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Engine: "), new DefaultRandomGenerator(), rg -> system, 100);
        RobustnessFormula formula = getRubustnessFormula();
        System.out.println(formula.eval(100, 0, sequence));
    }

    private static RobustnessFormula getRubustnessFormula() {
        return null;
    }


    public static Controller getController() {
        ControllerRegistry registry = new ControllerRegistry();
        registry.set("Ctrl",
                Controller.ifThenElse(
                        variableRegistry.greaterOrEqualThan("ch_temp", 99.8),
                        Controller.doAction(variableRegistry.set("cool", ON), registry.get("Cooling")),
                        Controller.doTick(registry.get("Check"))));
        registry.set("Cooling",Controller.doTick(4, registry.get("Check")));
        registry.set("Check",
                Controller.ifThenElse(
                        variableRegistry.equalsTo("ch_speed", SLOW),
                        Controller.doAction(variableRegistry.set("speed", SLOW).compose(variableRegistry.set("cool", OFF)),registry.get("Ctrl")),
                        Controller.doAction(variableRegistry.set("ch_in", SLOW).compose(variableRegistry.set("cool", OFF)),registry.get("Ctrl"))
                )
        );
        registry.set("IDS",
                Controller.ifThenElse(
                        variableRegistry.greaterThan("temp", 100.0).and(variableRegistry.equalsTo("cool", OFF)),
                        Controller.doAction(variableRegistry.set("ch_wrn", HOT).compose(variableRegistry.set("ch_speed", LOW)).compose(variableRegistry.set("ch_out", OFF)),registry.get("IDS")),
                        Controller.doAction(variableRegistry.set("ch_wrn", OK).compose(variableRegistry.set("ch_speed", HALF)).compose(variableRegistry.set("ch_out", HALF)),registry.get("IDS"))
                )
        );
        return new ParallelController(registry.reference("Ctrl"), registry.reference("IDS"));
    }

    public static List<VariableUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        double vP1 = state.getValue(p1);
        double vP2 = state.getValue(p2);
        double vP3 = state.getValue(p3);
        double vP4 = state.getValue(p4);
        double vP5 = state.getValue(p5);
        double vP6 = state.getValue(p6);
        double vTemp = state.getValue(temp);
        double vStress = state.getValue(stress);
        double vCool = state.getValue(cool);
        double vSpeed = state.getValue(speed);
        List<VariableUpdate> updates = new LinkedList<>();
        updates.add(new VariableUpdate(p1, vTemp));
        updates.add(new VariableUpdate(p2, vP1));
        updates.add(new VariableUpdate(p3, vP2));
        updates.add(new VariableUpdate(p4, vP3));
        updates.add(new VariableUpdate(p5, vP4));
        updates.add(new VariableUpdate(p6, vP5));
        if (isStressing(vP1, vP2, vP3, vP4, vP5, vP6)) {
            updates.add(new VariableUpdate(stress,Math.max(0,Math.min(1,vStress+STRESS_INCR))));
        }
        updates.add(new VariableUpdate(temp, nextTempValue(vTemp, getTemperatureVariation(rg, vCool, vSpeed))));
        return updates;
    }

    private static double nextTempValue(double vTemp, double v) {
        return Math.max(MIN_TEMP, Math.min(MAX_TEMP, vTemp+v));
    }

    private static double getTemperatureVariation(RandomGenerator rg, double vCool, double vSpeed) {
        if (vCool == ON) {
            return -1.2 + rg.nextDouble() * 0.4;
        }
        if (vSpeed == SLOW) {
            return 0.1 + rg.nextDouble() * 0.2;
        }
        if (vSpeed == HALF) {
            return 0.3 + rg.nextDouble() * 0.4;
        }
        return 0.7 + rg.nextDouble() * 0.5;
    }

    public static boolean isStressing(double vP1, double vP2, double vP3,double vP4, double vP5, double vP6) {
        return DoubleStream.of(vP1, vP2, vP3, vP4, vP5, vP6).filter(d -> d>=100).count()>3;
    }

    public static DataState getInitialState() {
        Map<Variable, Double> values = new HashMap<>();
        values.put(cool, OFF);
        values.put(speed, SLOW);
        return new DataState(variableRegistry, values);
    }
}
