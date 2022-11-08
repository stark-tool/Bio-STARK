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
import it.unicam.quasylab.jspear.controller.Controller;
import it.unicam.quasylab.jspear.controller.ControllerRegistry;
import it.unicam.quasylab.jspear.controller.ParallelController;
import it.unicam.quasylab.jspear.ds.*;
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
    //private static final VariableAllocation variableRegistry = VariableAllocation.create(VARIABLES);
    private static final int p1 = 0;//variableRegistry.getVariable("P1");
    private static final int p2 = 1;//variableRegistry.getVariable("P2");
    private static final int p3 = 2;//variableRegistry.getVariable("P3");
    private static final int p4 = 3;//variableRegistry.getVariable("P4");
    private static final int p5 = 4;//variableRegistry.getVariable("P5");
    private static final int p6 = 5;//variableRegistry.getVariable("P6");
    private static final int stress = 6;//variableRegistry.getVariable("stress");
    public static final int temp = 7;//variableRegistry.getVariable("temp");
    private static final int ch_temp = 8;//variableRegistry.getVariable("ch_temp");
    public static final int cool = 9;//variableRegistry.getVariable("cool");
    private static final int ch_speed = 10;//variableRegistry.getVariable("speed");
    private static final int ch_wrn = 11;//variableRegistry.getVariable("ch_wrn");

    private static final int ch_in = 12;//variableRegistry.getVariable("ch_in");

    private static final int ch_out = 13;//

    private static final int NUMBER_OF_VARIABLES = 14;//
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
            ControlledSystem system = new ControlledSystem(controller, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Engine: "), new DefaultRandomGenerator(), rg -> system, 1);

            for(int i=0; i<350; i++) {

                System.out.println(i + " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(cool))).max() +
                        " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(temp))).max() +
                        " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(ch_temp))).max() +
                        " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(stress))).max()
                );
            }

            /*
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

            DistanceExpression expr = new AtomicDistanceExpression(ds -> (ds.get(temp)/Math.abs(MAX_TEMP-MIN_TEMP)));
            DistanceExpression expr2 = new AtomicDistanceExpression(ds -> (ds.get(ch_wrn)==HOT?1.0:0.0));
            DistanceExpression expr3 = new AtomicDistanceExpression(ds -> ds.get(stress));

            Util.writeToCSV("./testTemperature.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 300, expr));

            Util.writeToCSV("./testWarning_tau.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 210, expr2));
            Util.writeToCSV("./testWarning_tau2.csv", Util.evalDistanceExpression(sequence, sequence2_tau2, 240,360, expr2));
            Util.writeToCSV("./testWarning_tau3.csv", Util.evalDistanceExpression(sequence, sequence2_tau3, 290, 410, expr2));

            Util.writeToCSV("./testStress.csv", Util.evalDistanceExpression(sequence, sequence2_tau, 90, 220, expr3));

            DistanceExpression MaxExpr2 = new MaxIntervalDistanceExpression(
                    new AtomicDistanceExpression(ds -> (ds.get(ch_wrn)==HOT?1.0:0.0)),
                    TAU,
                    K
            );
            DistanceExpression MaxExpr3 = new MaxIntervalDistanceExpression(
                    new AtomicDistanceExpression(ds -> ds.get(stress)),
                    TAU,
                    K
            );
            */
            /*
            int m = 50;
            double[][] warn = new double[m][1];
            double[][] st = new double[m][1];
            for(int i=0; i<m; i++){
                EvolutionSequence sequence3 = sequence.apply(getPerturbation(),TAU+i, 100);
                warn[i][0] = MaxExpr2.compute(i,sequence,sequence3);
                st[i][0] = MaxExpr3.compute(i,sequence,sequence3);
            }
            Util.writeToCSV("./testIntervalWarn.csv",warn);
            Util.writeToCSV("./testIntervalSt.csv",st);
            */

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
   }

    private static RobustnessFormula getFormula1() {
        return new AtomicRobustnessFormula(getPerturbation(),
                new MinIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> Math.abs(ds.get(temp)-ds.get(ch_temp))/Math.abs(MAX_TEMP-MIN_TEMP)),
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
                        new AtomicDistanceExpression(ds -> Math.abs(ds.get(temp)-ds.get(ch_temp))/Math.abs(MAX_TEMP-MIN_TEMP)),
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
                        new AtomicDistanceExpression(ds -> (ds.get(ch_wrn)==HOT?1.0:0.0)),
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
                        new AtomicDistanceExpression(ds -> ds.get(stress)),
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


    public static ControllerRegistry getControllerRegistry() {
        ControllerRegistry registry = new ControllerRegistry();
        registry.set("Ctrl",
                Controller.ifThenElse(
                        DataState.greaterOrEqualThan(ch_temp, 99.8),
                        Controller.doAction(DataStateUpdate.set(cool, ON), registry.reference("Cooling")),
                        registry.reference("Check")
                ));
        registry.set("Cooling",Controller.doTick(4, registry.reference("Check")));
        registry.set("Check",
                Controller.ifThenElse(
                        DataState.equalsTo(ch_speed, SLOW),
                        Controller.doAction(
                                (rg, ds) -> List.of (new DataStateUpdate(ch_speed, SLOW), new DataStateUpdate(cool, OFF)),registry.reference("Ctrl")),
                        Controller.doAction(
                                (rg, ds) -> List.of( new DataStateUpdate(ch_speed, ds.get(ch_in)), new DataStateUpdate(cool, OFF)),registry.reference("Ctrl"))
                )
        );
        registry.set("IDS",
                Controller.ifThenElse(
                        DataState.greaterThan(temp, 101.0).and(DataState.equalsTo(cool, OFF)),
                        Controller.doAction(
                                (rd, ds) -> List.of(new DataStateUpdate(ch_wrn, HOT), new DataStateUpdate(ch_speed, LOW), new DataStateUpdate(ch_out, FULL)),registry.reference("IDS")),
                        Controller.doAction(
                                (rg, ds) -> List.of(new DataStateUpdate( ch_wrn, OK), new DataStateUpdate(ch_speed, HALF), new DataStateUpdate(ch_out, HALF)),registry.reference("IDS"))
                )
        );
        return registry;
    }

    public static Controller getController() {
        ControllerRegistry registry = getControllerRegistry();
        return new ParallelController(registry.reference("Ctrl"), registry.reference("IDS"));
    }

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        double vP1 = state.get(p1);
        double vP2 = state.get(p2);
        double vP3 = state.get(p3);
        double vP4 = state.get(p4);
        double vP5 = state.get(p5);
        double vP6 = state.get(p6);
        double vTemp = state.get(temp);
        double vStress = state.get(stress);
        double vCool = state.get(cool);
        double vSpeed = state.get(ch_speed);
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(p1, vTemp));
        updates.add(new DataStateUpdate(p2, vP1));
        updates.add(new DataStateUpdate(p3, vP2));
        updates.add(new DataStateUpdate(p4, vP3));
        updates.add(new DataStateUpdate(p5, vP4));
        updates.add(new DataStateUpdate(p6, vP5));
        if (isStressing(vP1, vP2, vP3, vP4, vP5, vP6)) {
            updates.add(new DataStateUpdate(stress,Math.max(0,Math.min(1,vStress+STRESS_INCR))));
        }
        double newTemp = nextTempValue(vTemp, getTemperatureVariation(rg, vCool, vSpeed));
        updates.add(new DataStateUpdate(temp, newTemp));
        updates.add(new DataStateUpdate(ch_temp, newTemp));
        return updates;
    }

    private static Perturbation getPerturbation() {
        return new IterativePerturbation(N, new AtomicPerturbation(0, Main::perturbationFunction));
    }

    private static DataState perturbationFunction(RandomGenerator rg, DataState state) {
        double vTemp = state.get(temp);
        return state.apply(List.of(new DataStateUpdate(ch_temp, vTemp+ rg.nextDouble()*TEMP_OFFSET)));
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
        Map<Integer, Double> values = new HashMap<>();
        values.put(temp, vTemp);
        values.put(cool, OFF);
        values.put(ch_speed, HALF);
        values.put(ch_in, HALF);
        values.put(ch_temp, vTemp);
        values.put(p1, vTemp);
        values.put(p2, vTemp);
        values.put(p3, vTemp);
        values.put(p4, vTemp);
        values.put(p5, vTemp);
        values.put(p6, vTemp);
        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, 0.0));
    }
}
