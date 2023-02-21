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

package it.unicam.quasylab.jspear.examples.vehicle;

import it.unicam.quasylab.jspear.*;
import it.unicam.quasylab.jspear.controller.Controller;
import it.unicam.quasylab.jspear.controller.ControllerRegistry;
import it.unicam.quasylab.jspear.controller.ParallelController;
import it.unicam.quasylab.jspear.ds.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class Main {

    public final static String[] VARIABLES =
            new String[]{"p_speed_V1", "s_speed_V1", "p_distance_V1", "s_distance_V1", "accel_V1", "timer_V1",
                    "warning_V1", "offset_V1", "braking_distance_V1", "required_distance_V1", "safety_gap_V1",
                    "brake_light_V1",
                    "p_speed_V2", "s_speed_V2", "p_distance_V2", "s_distance_V2",
                    "p_distance_V1_V2", "s_distance_V1_V2", "accel_V2", "timer_V2",
                    "warning_V2", "offset_V2", "braking_distance_V2", "required_distance_V2", "safety_gap_V2",
                    "safety_gap_V1_V2", "brake_light_V2"
            };
    public final static double ACCELERATION = 1.0;
    public final static double BRAKE = 3.0;
    public final static double NEUTRAL = 0.0;
    public final static int TIMER_INIT = 3;
    public final static int DANGER = 1;
    public final static int OK = 0;
    public final static double MAX_SPEED_OFFSET = 0.3;
    public final static double INIT_SPEED_V1 = 30.0;
    public final static double INIT_SPEED_V2 = 30.0;
    public final static double MAX_SPEED = 50.0;
    public final static double INIT_DISTANCE_OBS_V1 = 10000.0;
    public final static double INIT_DISTANCE_V1_V2 = 5000.0;
    private static final double SAFETY_DISTANCE = 200.0;
    //private static final int ETA_SpeedLB = 0;
    //private static final int ETA_SpeedUB = 50;
    private static final double ETA_CRASH = 0.1;
    private static final double ETA_distance_combined = 0.8;
    private static final double ETA_distance_faster = 0.05;
    private static final double ETA_distance_slower = 0.9;
    private static final double ETA_crash_slower = 0.1;
    private static final int H = 320;
    //private static final int ATTACK_INIT = 0;
    //private static final int ATTACK_LENGTH = 550;


    private static final int p_speed_V1 = 0;//variableRegistry.getVariable("p_speed");
    private static final int s_speed_V1 = 1;//variableRegistry.getVariable("s_speed");
    private static final int p_distance_V1 = 2;// variableRegistry.getVariable("p_distance");
    private static final int s_distance_V1 = 3;// variableRegistry.getVariable("s_distance");
    private static final int accel_V1 = 4;//variableRegistry.getVariable("accel");
    private static final int timer_V1 = 5;//variableRegistry.getVariable("timer");
    private static final int warning_V1 = 6;//variableRegistry.getVariable("warning");
    private static final int offset_V1 = 7;//variableRegistry.getVariable("offset");
    private static final int braking_distance_V1 = 8;//variableRegistry.getVariable("braking_distance");
    private static final int required_distance_V1 = 9; //variableRegistry.getVariable("required_distance");
    private static final int safety_gap_V1 = 10;//variableRegistry.getVariable("safety_gap");
    private static final int brake_light_V1 = 11;

    private static final int p_speed_V2 = 12;//variableRegistry.getVariable("p_speed");
    private static final int s_speed_V2 = 13;//variableRegistry.getVariable("s_speed");
    private static final int p_distance_V2 = 14;// variableRegistry.getVariable("p_distance");
    private static final int s_distance_V2 = 15;// variableRegistry.getVariable("s_distance");
    private static final int p_distance_V1_V2 = 16;// variableRegistry.getVariable("p_distance");
    private static final int s_distance_V1_V2 = 17;
    private static final int accel_V2 = 18;//variableRegistry.getVariable("accel");
    private static final int timer_V2 = 19;//variableRegistry.getVariable("timer");
    private static final int warning_V2 = 20;//variableRegistry.getVariable("warning");
    private static final int offset_V2 = 21;//variableRegistry.getVariable("offset");
    private static final int braking_distance_V2 = 22;//variableRegistry.getVariable("braking_distance");
    private static final int required_distance_V2 = 23; //variableRegistry.getVariable("required_distance");
    private static final int safety_gap_V2 = 24;
    private static final int safety_gap_V1_V2 = 25;//variableRegistry.getVariable("safety_gap");
    private static final int brake_light_V2 = 26;

    private static final int NUMBER_OF_VARIABLES = 27;



    public static void main(String[] args) throws IOException {
        try {
            Controller controller_V1 = getController_V1();
            Controller controller_V2 = getController_V2();
            DataState state = getInitialState();
            ControlledSystem system = new ControlledSystem(new ParallelController(controller_V1, controller_V2), (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new DefaultRandomGenerator(), rg -> system, 1);

            /*
            EvolutionSequence sequenceAttSensorSpeed_V1 = sequence.apply(getSpeedSensorPerturbationV1(), ATTACK_INIT, 100);
            EvolutionSequence sequenceAttDistance_V2 = sequence.apply(getDistancePerturbationV2(), ATTACK_INIT, 100);

            RobustnessFormula PHI_SpeedFakeLowerBound = getFormulaSpeedFakeLowerBound();
            RobustnessFormula PHI_InstantSpeedFakeLowerBound = getFormulaInstantSpeedFakeLowerBound();

            RobustnessFormula PHI_SpeedFakeUpperBound = getFormulaSpeedFakeUpperBound();
            RobustnessFormula PHI_InstantSpeedFakeUpperBound = getFormulaInstantSpeedFakeUpperBound();

            RobustnessFormula PHI_Crash = getFormulaCrash();
            RobustnessFormula PHI_InstantCrash = getFormulaInstantCrash();

            RobustnessFormula PHI_SpeedFakeInBoundsImpliesCrash = getFormulaSpeedFakeInBoundImpliesCrash();
            RobustnessFormula PHI_AttackHasSuccess = getFormulaAttackHasSuccess();

            for(int test_step =0; test_step < 300; test_step++){System.out.print("Step " + test_step + ":  ");
                System.out.print("PHI_InstantSpeedFakeLB " + PHI_InstantSpeedFakeLowerBound.eval(10, test_step, ATTACK_INIT, sequence));
                System.out.print("  PHI_InstantSpeedFakeUB " + PHI_InstantSpeedFakeUpperBound.eval(10, test_step, ATTACK_INIT, sequence));
                System.out.print("   PHI_InstantCrash "  + PHI_InstantCrash.eval(10, test_step, ATTACK_INIT, sequence));
                System.out.print("   PHI_SpeedFakeLB "  + PHI_SpeedFakeLowerBound.eval(10, test_step, ATTACK_INIT, sequence));
                System.out.print("   PHI_SpeedFakeUB "  + PHI_SpeedFakeUpperBound.eval(10, test_step, ATTACK_INIT, sequence));
                System.out.println("   PHI_Crash "  + PHI_Crash.eval(10, test_step, ATTACK_INIT, sequence));
            }

            int test_step = 0;
            System.out.println("PHI_InstantSpeedFakeLB " + PHI_InstantSpeedFakeLowerBound.eval(100, test_step,  sequence));
            System.out.println("  PHI_InstantSpeedFakeUB " + PHI_InstantSpeedFakeUpperBound.eval(100, test_step, sequence));
            System.out.println("   PHI_InstantCrash "  + PHI_InstantCrash.eval(100, test_step,  sequence));
            System.out.println("   PHI_SpeedFakeLB "  + PHI_SpeedFakeLowerBound.eval(100, test_step,  sequence));
            System.out.println("   PHI_SpeedFakeUB "  + PHI_SpeedFakeUpperBound.eval(100, test_step,  sequence));
            System.out.println("   PHI_Crash "  + PHI_Crash.eval(100, test_step,  sequence));
            System.out.println("   PHI_Crash "  + PHI_SpeedFakeInBoundsImpliesCrash.eval(100, test_step,  sequence));
            System.out.println("PHI_AttackHasSuccess: " + PHI_AttackHasSuccess.eval(100, 0, sequence));

            DistanceExpression speed_expr = new AtomicDistanceExpression(ds -> ds.get(p_speed_V1));
            DistanceExpression distance_expr = new AtomicDistanceExpression(ds -> -ds.get(p_distance_V1));

             */

            //DistanceExpression relative_distance = new AtomicDistanceExpression(ds -> Math.abs(ds.get(s_distance_V1_V2) - ds.get(p_distance_V1_V2)) / ds.get(p_distance_V1_V2));
            //DistanceExpression obstacle_distance1 = new AtomicDistanceExpression(ds -> Math.abs(ds.get(s_distance_V1) - ds.get(p_distance_V1)) / ds.get(p_distance_V1));
            //DistanceExpression obstacle_distance2 = new AtomicDistanceExpression(ds -> Math.abs(ds.get(s_distance_V2) - ds.get(p_distance_V2)) / ds.get(p_distance_V2));

            DistanceExpression relative_distance_safe = new AtomicDistanceExpression(ds -> Math.min(1, 1 - Math.min(ds.get(p_distance_V1_V2),SAFETY_DISTANCE) / SAFETY_DISTANCE));
            //DistanceExpression obstacle_distance1_safe = new AtomicDistanceExpression(ds -> 1 - Math.min(ds.get(p_distance_V1), SAFETY_DISTANCE) / SAFETY_DISTANCE);
            //DistanceExpression obstacle_distance2_safe = new AtomicDistanceExpression(ds -> 1 - Math.min(ds.get(p_distance_V2), SAFETY_DISTANCE) / SAFETY_DISTANCE);
            DistanceExpression crash = new AtomicDistanceExpression(Main::rho_crash);
            DistanceExpression crash_probability = new AtomicDistanceExpression(Main::rho_crash_probability);

            RobustnessFormula Phi_1 = new AlwaysRobustnenessFormula(
                    new ConjunctionRobustnessFormula(
                        new AtomicRobustnessFormula(getIteratedCombinedPerturbation(),
                            new MaxIntervalDistanceExpression(relative_distance_safe, 250, 500),
                            RelationOperator.LESS_OR_EQUAL_THAN,
                            ETA_distance_combined,
                            0),
                        new AtomicRobustnessFormula(getIteratedCombinedPerturbation(),
                            new MaxIntervalDistanceExpression(crash_probability, 250, 500),
                            RelationOperator.LESS_OR_EQUAL_THAN,
                            ETA_CRASH,
                            0)
                    ),
                    0,
                    H);

            ThreeValuedFormula Phi_fast = new AlwaysThreeValuedFormula(
                    new AtomicThreeValuedFormula(getIteratedFasterPerturbation(),
                            new MaxIntervalDistanceExpression(relative_distance_safe, 250, 500),
                            RelationOperator.LESS_OR_EQUAL_THAN,
                            ETA_distance_faster,
                            40,
                            1.96),
                    0,
                    H);

            ThreeValuedFormula Phi_slow = new AlwaysThreeValuedFormula(
                    new AtomicThreeValuedFormula(getIteratedSlowerPerturbation(),
                            new MaxIntervalDistanceExpression(crash_probability, 250, 500),
                            RelationOperator.LESS_OR_EQUAL_THAN,
                            ETA_crash_slower,
                            40,
                            1.96),
                    0,
                    H);

            ThreeValuedFormula Phi_crash = new AlwaysThreeValuedFormula(
                    new AtomicThreeValuedFormula(getIteratedCombinedPerturbation(),
                        new MaxIntervalDistanceExpression(crash_probability, 250, 500),
                        RelationOperator.LESS_OR_EQUAL_THAN,
                        ETA_CRASH,
                        40,
                        1.96),
                    0,
                    H);

            ThreeValuedFormula Phi_comb = new ImplicationThreeValuedFormula(new ConjunctionThreeValuedFormula(Phi_fast, Phi_slow), Phi_crash);

            //System.out.println("Evaluation of PHI1: "+Phi_1.eval(100,0,sequence,false));
            //System.out.println("Evaluation of PHI_FAST: "+Phi_fast.eval(60,0,sequence));
            System.out.println("Evaluation of PHI_SLOW: "+Phi_slow.eval(60,0,sequence));
            System.out.println("Evaluation of PHI_CRASH: "+Phi_crash.eval(60,0,sequence));
            System.out.println("Evaluation of PHI_COMB: "+Phi_comb.eval(60,0,sequence));

            ArrayList<DataStateExpression> F = new ArrayList<DataStateExpression>();
            ArrayList<String> L = new ArrayList<String>();

            L.add("stp");

            //L.add("rSpeed1");
            //F.add(ds -> ds.get(p_speed_V1));

            //L.add("sSpeed1");
            //F.add(ds -> ds.get(s_speed_V1));

            //L.add("rSpeed2");
            //F.add(ds -> ds.get(p_speed_V2));

            //L.add("sSpeed2");
            //F.add(ds -> ds.get(s_speed_V2));

            //L.add("r_dist_1");
            //F.add(ds -> ds.get(p_distance_V1));

            //L.add("s_dist2");
            //F.add(ds -> ds.get(s_distance_V1));

            //L.add("r_dist2");
            //F.add(ds -> ds.get(p_distance_V2));

            //L.add("s_dist2");
            //F.add(ds -> ds.get(s_distance_V2));

            L.add("r_dist1vs2");
            F.add(ds -> ds.get(p_distance_V1_V2));

            //L.add("s_dist1vs2");
            //F.add(ds -> ds.get(s_distance_V1_V2));

            L.add("relative_safe");
            F.add(ds -> Math.min(1, 1 - Math.min(ds.get(p_distance_V1_V2),SAFETY_DISTANCE) / SAFETY_DISTANCE));

            //L.add("obs1_safe");
            //F.add(ds -> 1 - Math.min(ds.get(p_distance_V1),SAFETY_DISTANCE) / SAFETY_DISTANCE);

            //L.add("obs2_safe");
            //F.add(ds -> 1 - Math.min(ds.get(p_distance_V2),SAFETY_DISTANCE) / SAFETY_DISTANCE);

            //L.add("br_dist1");
            //F.add(ds -> ds.get(braking_distance_V1));

            //L.add("req_dist1");
            //F.add(ds -> ds.get(required_distance_V1));

            //L.add("safety_gap1");
            //F.add(ds -> ds.get(safety_gap_V1));

            L.add("safety_gap_12");
            F.add(ds -> ds.get(safety_gap_V1_V2));

            L.add("crash_12");
            F.add(Main::rho_crash);

            L.add("crash_prob");
            F.add(Main::rho_crash_probability);

            //L.add("accel2");
            //F.add(ds -> ds.get(accel_V2));


            //printLData(new DefaultRandomGenerator(), L, F, getIteratedCombinedPerturbation(), system, 500, 100);
            //printLData_min(new DefaultRandomGenerator(), L, F, getIteratedCombinedPerturbation(), system, 500, 100);
            //printLData_max(new DefaultRandomGenerator(), L, F, getIteratedCombinedPerturbation(), system, 500, 100);
            //printLData(new DefaultRandomGenerator(), L, F, system, 1000, 1);
            //printLData(new DefaultRandomGenerator(), L, F, getIteratedFasterPerturbation(), system, 500, 100);
            //printLData(new DefaultRandomGenerator(), L, F, getIteratedSlowerPerturbation(), system, 500, 100);


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static double rho_crash(DataState state) {
        double value = Math.min(SAFETY_DISTANCE,state.get(p_distance_V1_V2))/SAFETY_DISTANCE;
        if (value > 0){
            return 0.0;
        }
        else{
            return Math.abs(Math.min(SAFETY_DISTANCE,state.get(p_distance_V1_V2))/SAFETY_DISTANCE);
        }
    }
    public static double rho_crash_probability(DataState state) {
        if (state.get(p_distance_V1_V2) > 0){
            return 0.0;
        }
        else{
            return 1.0;
        }
    }
    private static void printData(RandomGenerator rg, String label, DataStateExpression f, SystemState s, int steps, int size) {
        System.out.println(label);
        double[] data = SystemState.sample(rg, f, s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d> %f\n", i, data[i]);
        }
    }

    private static void printData(RandomGenerator rg, String label, DataStateExpression f, Perturbation p, SystemState s, int steps, int size) {
        System.out.println(label);
        double[] data = SystemState.sample(rg, f, p, s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d> %f\n", i, data[i]);
        }
    }

    private static void printLData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample(rg, F, s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }
    private static void printLData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, Perturbation p, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample(rg, F, p, s, steps, size);
        for (int i = 0; i < data.length; i++) {
        System.out.printf("%d>   ", i);
        for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
        }
        System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }

    private static void printLData_min(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, Perturbation p, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample_min(rg, F, p, s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }

    private static void printLData_max(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, Perturbation p, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample_max(rg, F, p, s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }


/*
    private static RobustnessFormula getFormulaInstantSpeedFakeLowerBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
                new AtomicDistanceExpression(ds -> ds.get(p_speed_V1)),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }


    private static RobustnessFormula getFormulaSpeedFakeLowerBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
                new MinIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> ds.get(p_speed_V1)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }


    private static RobustnessFormula getFormulaInstantSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
                        new AtomicDistanceExpression(ds -> ds.get(p_speed_V1)),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
    }
    private static RobustnessFormula getFormulaSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
               new MaxIntervalDistanceExpression(
                       new AtomicDistanceExpression(ds -> ds.get(p_speed_V1)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
     }


    private static RobustnessFormula getFormulaInstantCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
                new AtomicDistanceExpression(ds ->  - Math.min(0,ds.get(p_distance_V1))),
                RelationOperator.GREATER_THAN,
                ETA_CRASH
        );
    }
     private static RobustnessFormula getFormulaCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbationV1(),
                new MaxIntervalDistanceExpression(
                      new AtomicDistanceExpression(ds ->  - Math.min(0,ds.get(p_distance_V1))),
                      ATTACK_INIT,
                       2*ATTACK_LENGTH
                 ),
                RelationOperator.GREATER_THAN,
                ETA_CRASH
        );
     }

    private static RobustnessFormula getFormulaSpeedFakeInBoundImpliesCrash() {
        return new ImplicationRobustnessFormula(
                new ConjunctionRobustnessFormula(getFormulaSpeedFakeLowerBound(), getFormulaSpeedFakeUpperBound()),
                getFormulaCrash()
        );
     }

    private static RobustnessFormula getFormulaAttackHasSuccess() {
        return new EventuallyRobustnessFormula(getFormulaSpeedFakeInBoundImpliesCrash(),
                0,
                H
        );
     }


 */


    public static Controller getController_V1() {


        ControllerRegistry registry = new ControllerRegistry();

        registry.set("Ctrl_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed_V1, 0),
                        Controller.ifThenElse(
                                   DataState.greaterThan(safety_gap_V1, 0 ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of(new DataStateUpdate(accel_V1, ACCELERATION), new DataStateUpdate(timer_V1, TIMER_INIT),
                                                   new DataStateUpdate(brake_light_V1, 0)),
                                           registry.reference("Accelerate_V1")
                                   ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of( new DataStateUpdate(accel_V1, - BRAKE), new DataStateUpdate(timer_V1, TIMER_INIT),
                                                         new DataStateUpdate(brake_light_V1, 1)),
                                           registry.reference("Decelerate_V1"))
                        ),
                        Controller.doAction(
                                (rg,ds)-> List.of(new DataStateUpdate(accel_V1,NEUTRAL), new DataStateUpdate(timer_V1,TIMER_INIT)),
                                registry.reference("Stop_V1")
                        )
                )
        );

        registry.set("Accelerate_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V1, 0),
                        Controller.doTick(registry.reference("Accelerate_V1")),
                        registry.reference("Ctrl_V1")
                )
        );

        registry.set("Decelerate_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V1, 0),
                        Controller.doTick(registry.reference("Decelerate_V1")),
                        registry.reference("Ctrl_V1")
                )
        );

        registry.set("Stop_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V1, 0),
                        Controller.doTick(registry.reference("Stop_V1")),
                        Controller.ifThenElse(
                                DataState.equalsTo(warning_V1,DANGER),
                                Controller.doAction(
                                        (rg, ds) -> List.of(new DataStateUpdate(accel_V1, -BRAKE),
                                                new DataStateUpdate(timer_V1, TIMER_INIT)),
                                        registry.reference("Decelerate_V1")
                                ),
                                Controller.doAction(
                                        DataStateUpdate.set(timer_V1,TIMER_INIT),
                                        registry.reference("Stop_V1")
                                )
                        )
                )
        );

        registry.set("IDS_V1",
                Controller.ifThenElse(
                        DataState.lessOrEqualThan(p_distance_V1, 2*TIMER_INIT*SAFETY_DISTANCE).and(DataState.equalsTo(accel_V1, ACCELERATION).or(DataState.equalsTo(accel_V1, NEUTRAL).and(DataState.greaterThan(p_speed_V1,0.0)))),
                        Controller.doAction(DataStateUpdate.set(warning_V1, DANGER),registry.reference("IDS_V1")),
                        Controller.doAction(DataStateUpdate.set(warning_V1, OK),registry.reference("IDS_V1"))
                )
        );
        return new ParallelController(registry.reference("Ctrl_V1"), registry.reference("IDS_V1"));

    }

    public static Controller getController_V2() {

        ControllerRegistry registry = new ControllerRegistry();

        registry.set("Ctrl_V2",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed_V2, 0),
                        Controller.ifThenElse(
                                DataState.greaterThan(safety_gap_V1_V2, 0 ).and(DataState.equalsTo(brake_light_V1, 0 ).or(DataState.greaterOrEqualThan(s_distance_V1_V2, 300))).and(DataState.greaterThan(safety_gap_V2, 0 )),
                                Controller.doAction(
                                        (rg, ds) -> List.of(new DataStateUpdate(accel_V2, ACCELERATION), new DataStateUpdate(timer_V2, TIMER_INIT),
                                                new DataStateUpdate(brake_light_V2, 0)),
                                        registry.reference("Accelerate_V2")),
                                Controller.doAction(
                                        (rg, ds) -> List.of( new DataStateUpdate(accel_V2, - BRAKE), new DataStateUpdate(timer_V2, TIMER_INIT),
                                                new DataStateUpdate(brake_light_V2, 1)),
                                        registry.reference("Decelerate_V2"))
                        ),
                        Controller.doAction(
                                (rg,ds)-> List.of(new DataStateUpdate(accel_V2,NEUTRAL), new DataStateUpdate(timer_V2,TIMER_INIT)),
                                registry.reference("Stop_V2")
                        )
                )
        );

        registry.set("Accelerate_V2",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V2, 0),
                        Controller.doTick(registry.reference("Accelerate_V2")),
                        registry.reference("Ctrl_V2")
                )
        );

        registry.set("Decelerate_V2",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V2, 0),
                        Controller.doTick(registry.reference("Decelerate_V2")),
                        registry.reference("Ctrl_V2")
                )
        );

        registry.set("Stop_V2",
                Controller.ifThenElse(
                        DataState.greaterThan(timer_V2, 0),
                        Controller.doTick(registry.reference("Stop_V2")),
                        Controller.ifThenElse(
                                DataState.equalsTo(warning_V2,DANGER),
                                Controller.doAction(
                                        (rg, ds) -> List.of(new DataStateUpdate(accel_V2, -BRAKE),
                                                new DataStateUpdate(timer_V2, TIMER_INIT)),
                                        registry.reference("Decelerate_V2")
                                ),
                                Controller.doAction(
                                        DataStateUpdate.set(timer_V2,TIMER_INIT),
                                        registry.reference("Stop_V2")
                                )
                        )
                )
        );

        registry.set("IDS_V2",
                Controller.ifThenElse(
                        DataState.lessOrEqualThan(p_distance_V2, 2*TIMER_INIT*SAFETY_DISTANCE).and(DataState.equalsTo(accel_V2, ACCELERATION).or(DataState.equalsTo(accel_V2, NEUTRAL).and(DataState.greaterThan(p_speed_V2,0.0)))),
                        Controller.doAction(DataStateUpdate.set(warning_V2, DANGER),registry.reference("IDS_V2")),
                        Controller.doAction(DataStateUpdate.set(warning_V2, OK),registry.reference("IDS_V2"))
                )
        );
        return new ParallelController(registry.reference("Ctrl_V2"), registry.reference("IDS_V2"));
    }


    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double travel_V1 = state.get(accel_V1)/2 + state.get(p_speed_V1);
        double new_timer_V1 = state.get(timer_V1) - 1;
        double new_p_speed_V1 = Math.min(MAX_SPEED,Math.max(0,state.get(p_speed_V1) + state.get(accel_V1)));
        double new_p_distance_V1 = state.get(p_distance_V1) - travel_V1;
        double travel_V2 = state.get(accel_V2)/2 + state.get(p_speed_V2);
        double new_timer_V2 = state.get(timer_V2) - 1;
        double new_p_speed_V2 = Math.min(MAX_SPEED,Math.max(0,state.get(p_speed_V2) + state.get(accel_V2)));
        double new_p_distance_V1_V2 = state.get(p_distance_V1_V2) - travel_V2 + travel_V1;
        double new_p_distance_V2 = state.get(p_distance_V2) - travel_V2;
        updates.add(new DataStateUpdate(timer_V1, new_timer_V1));
        updates.add(new DataStateUpdate(p_speed_V1, new_p_speed_V1));
        updates.add(new DataStateUpdate(p_distance_V1, new_p_distance_V1));
        updates.add(new DataStateUpdate(timer_V2, new_timer_V2));
        updates.add(new DataStateUpdate(p_speed_V2, new_p_speed_V2));
        updates.add(new DataStateUpdate(p_distance_V2, new_p_distance_V2));
        updates.add(new DataStateUpdate(p_distance_V1_V2, new_p_distance_V1_V2));
        if(new_timer_V1 == 0) {
            double new_bd_V1 = (new_p_speed_V1 * new_p_speed_V1 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_p_speed_V1 * TIMER_INIT)) / (2 * BRAKE);
            double new_rd_V1 = new_bd_V1 + SAFETY_DISTANCE;
            double new_sg_V1 = new_p_distance_V1 - new_rd_V1;
            updates.add(new DataStateUpdate(s_speed_V1, new_p_speed_V1));
            updates.add(new DataStateUpdate(braking_distance_V1, new_bd_V1));
            updates.add(new DataStateUpdate(required_distance_V1, new_rd_V1));
            updates.add(new DataStateUpdate(safety_gap_V1, new_sg_V1));
            updates.add(new DataStateUpdate(s_distance_V1,new_p_distance_V1));
        }
        if(new_timer_V2 == 0) {
            double new_bd_V2 = (new_p_speed_V2 * new_p_speed_V2 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_p_speed_V2 * TIMER_INIT)) / (2 * BRAKE);
            double new_rd_V2 = new_bd_V2 + SAFETY_DISTANCE;
            double new_sg_V1_V2 = new_p_distance_V1_V2 - new_rd_V2;
            double new_sg_V2 = new_p_distance_V2 - new_rd_V2;
            updates.add(new DataStateUpdate(s_speed_V2, new_p_speed_V2));
            updates.add(new DataStateUpdate(braking_distance_V2, new_bd_V2));
            updates.add(new DataStateUpdate(required_distance_V2, new_rd_V2));
            updates.add(new DataStateUpdate(safety_gap_V1_V2, new_sg_V1_V2));
            updates.add(new DataStateUpdate(safety_gap_V2, new_sg_V2));
            updates.add(new DataStateUpdate(s_distance_V2, new_p_distance_V2));
            updates.add(new DataStateUpdate(s_distance_V1_V2, new_p_distance_V1_V2));
        }
        return updates;
    }




/*

    private static Perturbation getSpeedSensorPerturbationV1( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV1));
        //   return new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV1);
    }

    private static DataState speedSensorPerturbationFunctionV1(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        double offset = state.get(p_speed_V1) * rg.nextDouble() * MAX_SPEED_OFFSET;
        double fake_speed = state.get(p_speed_V1) - offset;
        double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
        double fake_rd = fake_bd + SAFETY_DISTANCE;
        double fake_sg = state.get(p_distance_V1) - fake_rd;
        updates.add(new DataStateUpdate(s_speed_V1, fake_speed));
        updates.add(new DataStateUpdate(required_distance_V1, fake_rd));
        updates.add(new DataStateUpdate(safety_gap_V1, fake_sg));
        return state.apply(updates);
    }


    private static Perturbation getDistancePerturbationV2( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::distancePerturbationFunctionV2));
    }

    private static DataState distancePerturbationFunctionV2(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        double offset_V1_V2 = state.get(p_distance_V1_V2) * 15.30;
        double fake_distance_V1_V2 = state.get(p_distance_V1_V2) + offset_V1_V2;
        double new_sg_V1_V2 = fake_distance_V1_V2 - state.get(required_distance_V2);
        updates.add(new DataStateUpdate(s_distance_V1_V2, fake_distance_V1_V2));
        double fake_brake = 0;
        updates.add(new DataStateUpdate(brake_light_V1, fake_brake));
        updates.add(new DataStateUpdate(safety_gap_V1_V2, new_sg_V1_V2));
        return state.apply(updates);
    }


 */
    private static  Perturbation getFasterPerturbation() {
        return new IterativePerturbation(3, new AtomicPerturbation(TIMER_INIT-1, Main::fasterPerturbation));
    }

    private static  Perturbation getSlowerPerturbation() {
        return new IterativePerturbation(3, new AtomicPerturbation(TIMER_INIT-1, Main::slowerPerturbation));
    }


    private static  Perturbation getIteratedFasterPerturbation() {
        return new AfterPerturbation(1, new IterativePerturbation(150, new AtomicPerturbation(TIMER_INIT-1, Main::fasterPerturbation)));
    }

    private static  Perturbation getIteratedSlowerPerturbation() {
        return new AfterPerturbation(1, new IterativePerturbation(150, new AtomicPerturbation(TIMER_INIT-1, Main::slowerPerturbation)));
    }

    private static  Perturbation getIteratedCombinedPerturbation() {
        return new AfterPerturbation(1, new IterativePerturbation(50, new SequentialPerturbation(getFasterPerturbation(),getSlowerPerturbation())));
    }

    private static DataState fasterPerturbation(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double offset = state.get(p_speed_V1) * rg.nextDouble() * MAX_SPEED_OFFSET;
        //double offset = rg.nextDouble() * MAX_SPEED_OFFSET;
        double fake_speed = Math.min(MAX_SPEED,state.get(p_speed_V1) + offset);
        double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
        double fake_rd = fake_bd + SAFETY_DISTANCE;
        double fake_sg = state.get(p_distance_V1) - fake_rd;
        //double fake_distance_V1 = state.get(p_distance_V1) + state.get(p_speed_V1) - fake_speed;
        //double fake_distance_V1_V2 = state.get(p_distance_V1_V2) + state.get(p_speed_V1) - fake_speed;
        updates.add(new DataStateUpdate(s_speed_V1, fake_speed));
        updates.add(new DataStateUpdate(required_distance_V1, fake_rd));
        updates.add(new DataStateUpdate(safety_gap_V1, fake_sg));
        //updates.add(new DataStateUpdate(s_distance_V1,fake_distance_V1));
        //updates.add(new DataStateUpdate(s_distance_V1_V2,fake_distance_V1_V2));
        return state.apply(updates);
    }

    private static DataState slowerPerturbation(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double offset = state.get(p_speed_V2) * rg.nextDouble() * MAX_SPEED_OFFSET;
        //double offset = rg.nextDouble() * MAX_SPEED_OFFSET;
        double fake_speed = Math.max(0, state.get(p_speed_V2) - offset);
        double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
        double fake_rd = fake_bd + SAFETY_DISTANCE;
        double fake_sg = state.get(p_distance_V1_V2) - fake_rd;
        //double fake_dist_V2 = state.get(p_distance_V2) + state.get(p_speed_V2) - fake_speed;
        //double fake_dist_V1_V2 = state.get(p_distance_V1_V2) + state.get(p_speed_V2) - fake_speed;
        updates.add(new DataStateUpdate(s_speed_V2, fake_speed));
        updates.add(new DataStateUpdate(required_distance_V2, fake_rd));
        updates.add(new DataStateUpdate(safety_gap_V1_V2, fake_sg));
        //updates.add(new DataStateUpdate(s_distance_V2,fake_dist_V2));
        //updates.add(new DataStateUpdate(s_distance_V1_V2,fake_dist_V1_V2));
        return state.apply(updates);
    }







    public static DataState getInitialState( ) {
        Map<Integer, Double> values = new HashMap<>();
        // INITIAL DATA FOR V1
        values.put(brake_light_V1, (double) 0);
        values.put(timer_V1, (double) 0);
        values.put(p_speed_V1, INIT_SPEED_V1);
        values.put(s_speed_V1, INIT_SPEED_V1);
        values.put(p_distance_V1, INIT_DISTANCE_OBS_V1);
        values.put(s_distance_V1, INIT_DISTANCE_OBS_V1);
        values.put(accel_V1, NEUTRAL);
        values.put(warning_V1, (double) OK);
        values.put(offset_V1, 0.0);
        double init_bd_V1 = (INIT_SPEED_V1 * INIT_SPEED_V1 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * INIT_SPEED_V1 * TIMER_INIT))/(2 * BRAKE);
        double init_rd_V1 = init_bd_V1 + SAFETY_DISTANCE;
        double init_sg_V1 = INIT_DISTANCE_OBS_V1 - init_rd_V1;
        values.put(braking_distance_V1, init_bd_V1);
        values.put(required_distance_V1, init_rd_V1);
        values.put(safety_gap_V1, init_sg_V1);
        // INITIAL DATA FOR V2
        values.put(timer_V2, (double) 0);
        values.put(brake_light_V2, (double) 0);
        values.put(p_speed_V2, INIT_SPEED_V2);
        values.put(s_speed_V2, INIT_SPEED_V2);
        values.put(p_distance_V2, INIT_DISTANCE_V1_V2 + INIT_DISTANCE_OBS_V1);
        values.put(s_distance_V2, INIT_DISTANCE_V1_V2 + INIT_DISTANCE_OBS_V1);
        values.put(p_distance_V1_V2, INIT_DISTANCE_V1_V2 );
        values.put(s_distance_V1_V2, INIT_DISTANCE_V1_V2 );
        values.put(accel_V2, NEUTRAL);
        values.put(warning_V2, (double) OK);
        values.put(offset_V2, 0.0);
        double init_bd_V2 = (INIT_SPEED_V2 * INIT_SPEED_V2 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * INIT_SPEED_V2 * TIMER_INIT))/(2 * BRAKE);
        double init_rd_V2 = init_bd_V2 + SAFETY_DISTANCE;
        double init_sg_V1_V2 = INIT_DISTANCE_V1_V2 - init_rd_V2;
        double init_sg_V2 = INIT_DISTANCE_V1_V2 + INIT_DISTANCE_OBS_V1- init_rd_V2;
        values.put(braking_distance_V2, init_bd_V2);
        values.put(required_distance_V2, init_rd_V2);
        values.put(safety_gap_V1_V2, init_sg_V1_V2);
        values.put(safety_gap_V2, init_sg_V2);

        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN));
    }

}
