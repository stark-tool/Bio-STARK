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

import java.io.IOException;
import java.util.*;
import java.util.stream.DoubleStream;

public class Main {

    public final static String[] VARIABLES =
            new String[] { "P1", "P2", "P3", "P4", "P5", "P6", "stress",  "temp", "cool", "speed", "ch_temp", "ch_wrn", "ch_speed", "ch_out", "ch_in"};
    public final static double ON = 0;
    public final static double OFF = 1;
    public final static double SLOW = 2;
    public final static double HALF = 3;
    public final static double FULL = 4;
    public final static double OK = 5;
    public final static double HOT = 6;
    public final static double LOW = 7;
    private static final double MIN_TEMP = 0;
    private static final double MAX_TEMP = 150;
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
    private static final Variable ch_temp = variableRegistry.getVariable("ch_temp");
    private static final Variable cool = variableRegistry.getVariable("cool");
    private static final Variable speed = variableRegistry.getVariable("speed");
    private static final Variable ch_wrn = variableRegistry.getVariable("ch_wrn");

    private static final Variable ch_in = variableRegistry.getVariable("ch_in");

    private static final double INITIAL_TEMP_VALUE = 95.0;
    private static final double TEMP_OFFSET = -1.5;
    private static final int N = 100;
    private static final int TAU = 100;
    private static final int TAU2 = 250;
    private static final int TAU3 = 300;
    private static final int K = TAU+N+10;
    private static final int H = 5000;
    private static final double ETA1 = 0.0;
    private static final double ETA2 = 0.02;
    private static final double ETA3 = 0.035;
    private static final double ETA4 = 0.3;


    public static void main(String[] args) throws IOException {
        try {
            Controller controller = getController();
            DataState state = getInitialState(INITIAL_TEMP_VALUE);
            ControlledSystem system = new ControlledSystem(controller, (rg, ds) -> ds.set(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Engine: "), new DefaultRandomGenerator(), rg -> system, 100);
            EvolutionSequence sequence2_tau = sequence.apply(getPerturbation(),TAU, 100);
            EvolutionSequence sequence2_tau2 = sequence.apply(getPerturbation(),TAU2, 100);
            EvolutionSequence sequence2_tau3 = sequence.apply(getPerturbation(),TAU3, 100);

            RobustnessFormula PHI1 = getFormula1();
            RobustnessFormula PHI2 = getFormula2();
            RobustnessFormula PHI3 = getFormula3();
            RobustnessFormula PHI4 = getFormula4();
            RobustnessFormula PHI5 = getFormula5();
            RobustnessFormula PHI = getFinalFormula();
            int test_step = 50;
            System.out.println("Evaluation of phi1 at step "+test_step+": "+PHI1.eval(100, test_step, sequence));
            System.out.println("Evaluation of phi2 at step "+test_step+": "+PHI2.eval(100, test_step, sequence));
            System.out.println("Evaluation of phi3 at step "+test_step+": "+PHI3.eval(100, test_step, sequence));
            System.out.println("Evaluation of phi4 at step "+test_step+": "+PHI4.eval(100, test_step, sequence));
            System.out.println("Evaluation of phi5 at step "+test_step+": "+PHI5.eval(100, test_step, sequence));
            System.out.println("Evaluation of phi at step 0: "+PHI.eval(100, 0, sequence));

            DistanceExpression expr = new AtomicDistanceExpression(ds -> ds.getValue(temp));
            DistanceExpression expr2 = new AtomicDistanceExpression(ds -> (ds.getValue(ch_wrn)==HOT?1.0:0.0));
            DistanceExpression expr3 = new AtomicDistanceExpression(ds -> ds.getValue(stress));

            Util.writeToCSV("./testTemperature.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 300, expr));

            Util.writeToCSV("./testWarning_tau.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 210, expr2));
            Util.writeToCSV("./testWarning_tau2.csv", Util.evalDistanceExpression(sequence, sequence2_tau2, 240,360, expr2));
            Util.writeToCSV("./testWarning_tau3.csv", Util.evalDistanceExpression(sequence, sequence2_tau3, 290, 410, expr2));

            Util.writeToCSV("./testStress.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 220, expr3));

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
   }

    private static RobustnessFormula getFormula1() {
        return new AtomicRobustnessFormula(getPerturbation(),
                new MinIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> Math.abs(ds.getValue(temp)-ds.getValue(ch_temp))/Math.abs(MAX_TEMP-MIN_TEMP)),
                        TAU,
                        TAU+N-1
                ),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA1
        );
    }

    private static RobustnessFormula getFormula2() {
        return new AtomicRobustnessFormula(getPerturbation(),
                new MaxIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> Math.abs(ds.getValue(temp)-ds.getValue(ch_temp))/Math.abs(MAX_TEMP-MIN_TEMP)),
                        TAU,
                        TAU+N-1
                ),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA2
        );
    }

    private static RobustnessFormula getFormula3() {
        return new AtomicRobustnessFormula(getPerturbation(),
                new MaxIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> (ds.getValue(ch_wrn)==HOT?1.0:0.0)),
                        TAU,
                        K
                ),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA3
        );
    }

    private static RobustnessFormula getFormula4() {
        return new AtomicRobustnessFormula(getPerturbation(),
                new MaxIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> ds.getValue(stress)),
                        TAU,
                        K
                ),
                RelationOperator.GREATER_THAN,
                ETA4
        );
    }

    private static RobustnessFormula getFormula5() {
        return new ImplicationRobustnessFormula(
                new ConjunctionRobustnessFormula(getFormula1(), getFormula2()),
                new ConjunctionRobustnessFormula(getFormula3(), getFormula4())
        );
    }

    private static RobustnessFormula getFinalFormula() {
        return new EventuallyRobustnessFormula(getFormula5(),
                0,
                H
        );
    }


    public static Controller getController() {
        ControllerRegistry registry = new ControllerRegistry();
        registry.set("Ctrl",
                Controller.ifThenElse(
                        variableRegistry.greaterOrEqualThan("ch_temp", 99.8),
                        Controller.doAction(variableRegistry.set("cool", ON), registry.get("Cooling")),
                        registry.get("Check")
                ));
        registry.set("Cooling",Controller.doTick(4, registry.get("Check")));
        registry.set("Check",
                Controller.ifThenElse(
                        variableRegistry.equalsTo("ch_speed", SLOW),
                        Controller.doAction(variableRegistry.set("speed", SLOW).compose(variableRegistry.set("cool", OFF)),registry.get("Ctrl")),
                        Controller.doAction(variableRegistry.set("speed", variableRegistry.get("ch_in")).compose(variableRegistry.set("cool", OFF)),registry.get("Ctrl"))
                )
        );
        registry.set("IDS",
                Controller.ifThenElse(
                        variableRegistry.greaterThan("temp", 101.0).and(variableRegistry.equalsTo("cool", OFF)),
                        Controller.doAction(variableRegistry.set("ch_wrn", HOT).compose(variableRegistry.set("ch_speed", LOW)).compose(variableRegistry.set("ch_out", FULL)),registry.get("IDS")),
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
        double newTemp = nextTempValue(vTemp, getTemperatureVariation(rg, vCool, vSpeed));
        updates.add(new VariableUpdate(temp, newTemp));
        updates.add(new VariableUpdate(ch_temp, newTemp));
        return updates;
    }

    private static Perturbation getPerturbation() {
        return new IterativePerturbation(N, new AtomicPerturbation(0, Main::perturbationFunction));
    }

    private static DataState perturbationFunction(RandomGenerator rg, DataState state) {
        double vTemp = state.getValue(temp);
        return state.set(ch_temp, vTemp+ rg.nextDouble()*TEMP_OFFSET);
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

    public static DataState getInitialState(double vTemp) {
        Map<Variable, Double> values = new HashMap<>();
        values.put(temp, vTemp);
        values.put(cool, OFF);
        values.put(speed, HALF);
        values.put(ch_in, HALF);
        values.put(p1, vTemp);
        values.put(p2, vTemp);
        values.put(p3, vTemp);
        values.put(p4, vTemp);
        values.put(p5, vTemp);
        values.put(p6, vTemp);
        return new DataState(variableRegistry, values);
    }
}
