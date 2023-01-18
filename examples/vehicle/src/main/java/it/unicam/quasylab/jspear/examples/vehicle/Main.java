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
            new String[] { "p_speed_V1", "s_speed_V1", "p_distance_V1", "s_distance_V1", "accel_V1", "timer_V1",
                    "warning_V1", "offset_V1", "braking_distance_V1", "required_distance_V1", "safety_gap_V1",
                    "brakelight_V1",
                    "p_speed_V2", "s_speed_V2", "p_distance_V2", "s_distance_V2",
                    "p_distance_V1_V2", "s_distance_V1_V2", "accel_V2", "timer_V2",
                    "warning_V2", "offset_V2", "braking_distance_V2", "required_distance_V2", "safety_gap_V2",
                    "safety_gap_V1_V2", "brakelight_V2"};
    public final static double ACCELERATION = 0.1;
    public final static double BRAKE = 0.3;
    public final static double NEUTRAL = 0.0;
    public final static int TIMER_INIT = 5;
    public final static int DANGER = 1;
    public final static int OK = 0;
    public final static double MAX_SPEED_OFFSET = 0.122;

    public final static double INIT_SPEED_V1 = 30.0;
    public final static double INIT_SPEED_V2 = 30.0;
    public final static double MAX_SPEED = 40.0;
    public final static double INIT_DISTANCE_OBS_V1 = 14000.0;
    public final static double INIT_DISTANCE_V1_V2 = 2000.0;
    private static final double SAFETY_DISTANCE = 200.0;
    //private static final VariableAllocation variableRegistry = VariableAllocation.create(VARIABLES);

    // private static final Variable a = variableRegistry.getVariable("a");

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
    private static final int brakelight_V1 = 11;

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
    // private static final int x = 22;
    private static final int brakelight_V2 = 26;

    private static final int NUMBER_OF_VARIABLES = 27;

    private static final int ETA_SpeedLB = 0;
    private static final int ETA_SpeedUB = 50;
    private static final int ETA_CRASH = 0;
    private static final int H = 1000;
    private static final int ATTACK_INIT = 0;

    private static final int ATTACK_LENGTH = 550;



    public static void main(String[] args) throws IOException {
        try {
            Controller controller_V1 = getController_V1();
            Controller controller_V2 = getController_V2();
            DataState state = getInitialState( );
            ControlledSystem system = new ControlledSystem(new ParallelController(controller_V1,controller_V2), (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Vehicle: "), new DefaultRandomGenerator(), rg -> system, 100);
            //
            EvolutionSequence sequenceAttSensorSpeed_V1 = sequence.apply(getSpeedSensorPerturbationV1(), ATTACK_INIT, 30);
            EvolutionSequence sequenceAttSensorSpeed_V2 = sequence.apply(getSpeedSensorPerturbationV2(), ATTACK_INIT, 30);
            // EvolutionSequence casino = sequence.apply(perturbazioneV1(), ATTACK_INIT, 1);


            for(int i=0; i<3000; i++) {
                System.out.println(i+
                           //     " x originale " +    Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(x))).max() +
                           //     " x taroccata "+     Arrays.stream(casino.get(i).evalPenaltyFunction(ds -> ds.get(x))).max()
                           //    " s_speed_v1 " + Arrays.stream(sequenceAttSensorSpeed_V1.get(i).evalPenaltyFunction(ds -> ds.get(p_speed_V1))).max() +
                           //   " timer_v1 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(timer_V1))).max() +
                       // " sp_v1 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(p_speed_V1))).min() +
                      //  " light_v1" + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(brakelight_V1))).max() +
                      // " sp_v2 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(p_speed_V2))).max().toString() +
                      //          " light_v2" + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(brakelight_V2))).max()
                             " safety_gap_v1_v2 " + Arrays.stream(sequenceAttSensorSpeed_V2.get(i).evalPenaltyFunction(ds -> ds.get(safety_gap_V1_V2))).max() +
                           //     " safety_gap_v2 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(safety_gap_V2))).max() +
                                //"breaking_distance " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(braking_distance_V1))).max() +
                                //"required_distance " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(required_distance_V1))).max() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).min() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed_V1.get(i).evalPenaltyFunction(ds -> ds.get(p_speed_V1))).max() +
  //                              sequence.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).max())) +
                       " bd_v2 " + Arrays.stream(sequenceAttSensorSpeed_V2.get(i).evalPenaltyFunction(ds -> ds.get(braking_distance_V2))).average() +
                                " speed_v1 " + Arrays.stream(sequenceAttSensorSpeed_V2.get(i).evalPenaltyFunction(ds -> ds.get(p_speed_V1))).average() +
                                " ph_d_v1_v2 " + Arrays.stream(sequenceAttSensorSpeed_V2.get(i).evalPenaltyFunction(ds -> ds.get(p_distance_V1_V2))).average()
                        //   " sensed_distance_v1 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(s_distance_V1))).max() +
                             //   " sensed_distance_v2 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(s_distance_V1_V2))).max()
                                //" timer " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(timer_V1))).max()
                        //        " physical_distance_v2 " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(p_distance_V2))).max()
                       // " " + Arrays.stream(sequenceAttSensorSpeed_V1.get(i).evalPenaltyFunction(ds -> ds.get(p_distance_V1))).min()
                        //" " + Arrays.stream(sequenceAttSensorSpeed_V1.get(i).evalPenaltyFunction(ds -> ds.get(p_distance_V1))).max()
                );
            }


            RobustnessFormula PHI_SpeedFakeLowerBound = getFormulaSpeedFakeLowerBound();
            RobustnessFormula PHI_InstantSpeedFakeLowerBound = getFormulaInstantSpeedFakeLowerBound();

            RobustnessFormula PHI_SpeedFakeUpperBound = getFormulaSpeedFakeUpperBound();
            RobustnessFormula PHI_InstantSpeedFakeUpperBound = getFormulaInstantSpeedFakeUpperBound();

            RobustnessFormula PHI_Crash = getFormulaCrash();
            RobustnessFormula PHI_InstantCrash = getFormulaInstantCrash();

            RobustnessFormula PHI_SpeedFakeInBoundsImpliesCrash = getFormulaSpeedFakeInBoundImpliesCrash();
            RobustnessFormula PHI_AttackHasSuccess = getFormulaAttackHasSuccess();

         //    for(int test_step =0; test_step < 300; test_step++){System.out.print("Step " + test_step + ":  ");
         //       System.out.print("PHI_InstantSpeedFakeLB " + PHI_InstantSpeedFakeLowerBound.eval(10, test_step, ATTACK_INIT, sequence));
         //       System.out.print("  PHI_InstantSpeedFakeUB " + PHI_InstantSpeedFakeUpperBound.eval(10, test_step, ATTACK_INIT, sequence));
         //       System.out.print("   PHI_InstantCrash "  + PHI_InstantCrash.eval(10, test_step, ATTACK_INIT, sequence));
         //       System.out.print("   PHI_SpeedFakeLB "  + PHI_SpeedFakeLowerBound.eval(10, test_step, ATTACK_INIT, sequence));
         //       System.out.print("   PHI_SpeedFakeUB "  + PHI_SpeedFakeUpperBound.eval(10, test_step, ATTACK_INIT, sequence));
         //       System.out.println("   PHI_Crash "  + PHI_Crash.eval(10, test_step, ATTACK_INIT, sequence));
         //   }


            int test_step = 20;
                      System.out.print("PHI_InstantSpeedFakeLB " + PHI_InstantSpeedFakeLowerBound.eval(10, test_step,  sequence));
                       System.out.print("  PHI_InstantSpeedFakeUB " + PHI_InstantSpeedFakeUpperBound.eval(10, test_step, sequence));
                       System.out.print("   PHI_InstantCrash "  + PHI_InstantCrash.eval(10, test_step,  sequence));
                     System.out.print("   PHI_SpeedFakeLB "  + PHI_SpeedFakeLowerBound.eval(10, test_step,  sequence));
                      System.out.print("   PHI_SpeedFakeUB "  + PHI_SpeedFakeUpperBound.eval(10, test_step,  sequence));
                      System.out.print("   PHI_Crash "  + PHI_Crash.eval(10, test_step,  sequence));
            System.out.println("   PHI_Crash "  + PHI_SpeedFakeInBoundsImpliesCrash.eval(10, test_step,  sequence));



            System.out.println("PHI_AttackHasSuccess: " + PHI_AttackHasSuccess.eval(100, ATTACK_INIT, sequence));


            DistanceExpression speed_expr = new AtomicDistanceExpression(ds -> ds.get(p_speed_V1));
            DistanceExpression distance_expr = new AtomicDistanceExpression(ds -> - ds.get(p_distance_V1));

            int n = 300;
              double[][] speed_difference = new double[n][1];
              double[][] distance_difference = new double[n][1];
              for(int i=0; i<n; i++){
                speed_difference[i][0] = speed_expr.compute(i,sequence,sequenceAttSensorSpeed_V1);
                distance_difference[i][0] = distance_expr.compute(i,sequence,sequenceAttSensorSpeed_V1);
             }

            Util.writeToCSV("./testSpeedDifferenceH.csv",speed_difference);
            Util.writeToCSV("./testDistanceDifferenceH.csv",distance_difference);


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
   }




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



    public static Controller getController_V1() {


        ControllerRegistry registry = new ControllerRegistry();

        /*
        registry.set("aaa",
                Controller.doAction(
                        (rg, ds) -> List.of(new DataStateUpdate(x, ds.get(x) + 1000)),
                        registry.reference("bbb")
                )
                );
        registry.set("bbb",
                Controller.doTick(0,registry.reference("aaa"))
        );
        */


        registry.set("Ctrl_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed_V1, 0),
                        Controller.ifThenElse(
                                   DataState.greaterThan(safety_gap_V1, 0 ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of(new DataStateUpdate(accel_V1, ACCELERATION), new DataStateUpdate(timer_V1, TIMER_INIT),
                                                   new DataStateUpdate(brakelight_V1, 0)),
                                           registry.reference("Accelerate_V1")
                                   ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of( new DataStateUpdate(accel_V1, - BRAKE), new DataStateUpdate(timer_V1, TIMER_INIT),
                                                         new DataStateUpdate(brakelight_V1, 1)),
                                           registry.reference("Decelerate_V1"))),
                        Controller.doTick(registry.reference("Stop_V1"))
                )
        );

        registry.set("Stop_V1",
                Controller.doAction((rg, ds) -> List.of(new DataStateUpdate(accel_V1, NEUTRAL)), registry.reference("Stop_V1"))
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

        registry.set("IDS_V1",
                Controller.ifThenElse(
                        DataState.lessOrEqualThan(p_distance_V1, 2*TIMER_INIT*SAFETY_DISTANCE).and(DataState.equalsTo(accel_V1, ACCELERATION)),
                        Controller.doAction(DataStateUpdate.set(warning_V1, DANGER),registry.reference("IDS_V1")),
                        Controller.doAction(DataStateUpdate.set(warning_V1, OK),registry.reference("IDS_V1"))
                )
        );
        return new ParallelController(registry.reference("Ctrl_V1"), registry.reference("IDS_V1"));

        // return new ExecController(registry.reference("aaa"));
    }

    public static Controller getController_V2() {

        ControllerRegistry registry = new ControllerRegistry();

        registry.set("Ctrl_V2",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed_V2, 0),
                        Controller.ifThenElse(
                                DataState.greaterThan(safety_gap_V1_V2, 0 ).and(DataState.equalsTo(brakelight_V1, 0 ).and(DataState.greaterThan(safety_gap_V2, 0 ))),
                                Controller.doAction(
                                        (rg, ds) -> List.of(new DataStateUpdate(accel_V2, ACCELERATION), new DataStateUpdate(timer_V2, TIMER_INIT), new DataStateUpdate(brakelight_V2, 0)),
                                        registry.reference("Accelerate_V2")),
                                Controller.doAction(
                                        (rg, ds) -> List.of( new DataStateUpdate(accel_V2, - BRAKE), new DataStateUpdate(timer_V2, TIMER_INIT), new DataStateUpdate(brakelight_V2, 1)),
                                        registry.reference("Decelerate_V2"))),
                        Controller.doTick(registry.reference("Stop_V2"))
                )
        );

        registry.set("Stop_V2",
                Controller.doAction((rg, ds) -> List.of(new DataStateUpdate(accel_V2, NEUTRAL)), registry.reference("Stop_V2"))
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

        registry.set("IDS_V2",
                Controller.ifThenElse(
                        DataState.lessOrEqualThan(p_distance_V2, 2*TIMER_INIT*SAFETY_DISTANCE).and(DataState.equalsTo(accel_V2, ACCELERATION)),
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
        double new_p_speed_V1 = Math.min(Math.max(0,state.get(p_speed_V1) + state.get(accel_V1)),MAX_SPEED);
        double new_p_distance_V1 = state.get(p_distance_V1) - travel_V1;
        //  updates.add(new VariableUpdate(a,state.getValue(a)+5));
        double travel_V2 = state.get(accel_V2)/2 + state.get(p_speed_V2);
        double new_timer_V2 = state.get(timer_V2) - 1;
        double new_p_speed_V2 = Math.min(Math.max(0,state.get(p_speed_V2) + state.get(accel_V2)),MAX_SPEED);
        double new_p_distance_V1_V2 = state.get(p_distance_V1_V2) - travel_V2 + travel_V1;
        double new_p_distance_V2 = state.get(p_distance_V2) - travel_V2;
        //  updates.add(new VariableUpdate(a,state.getValue(a)+5));
        updates.add(new DataStateUpdate(timer_V1, new_timer_V1));
        updates.add(new DataStateUpdate(p_speed_V1, new_p_speed_V1));
        updates.add(new DataStateUpdate(p_distance_V1, new_p_distance_V1));
        updates.add(new DataStateUpdate(timer_V2, new_timer_V2));
        updates.add(new DataStateUpdate(p_speed_V2, new_p_speed_V2));
        updates.add(new DataStateUpdate(p_distance_V2, new_p_distance_V2));
        updates.add(new DataStateUpdate(p_distance_V1_V2, new_p_distance_V1_V2));
        if(new_timer_V1 == 0) {
            double new_s_speed_V1 = new_p_speed_V1;
            double new_s_distance_V1 = new_p_distance_V1;
            double new_bd_V1 = (new_s_speed_V1 * new_s_speed_V1 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_s_speed_V1 * TIMER_INIT)) / (2 * BRAKE);
            double new_rd_V1 = new_bd_V1 + SAFETY_DISTANCE;
            double new_sg_V1 = new_s_distance_V1 - new_rd_V1;
            updates.add(new DataStateUpdate(s_speed_V1, new_s_speed_V1));
            updates.add(new DataStateUpdate(braking_distance_V1, new_bd_V1));
            updates.add(new DataStateUpdate(required_distance_V1, new_rd_V1));
            updates.add(new DataStateUpdate(safety_gap_V1, new_sg_V1));
        }
        if(new_timer_V2 == 0) {
            double new_s_speed_V2 = new_p_speed_V2;
            double new_s_distance_V2 = new_p_distance_V2;
            double new_s_distance_V1_V2 = new_p_distance_V1_V2;
            double new_bd_V2 = (new_s_speed_V2 * new_s_speed_V2 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_s_speed_V2 * TIMER_INIT)) / (2 * BRAKE);
            double new_rd_V2 = new_bd_V2 + SAFETY_DISTANCE;
            double new_sg_V1_V2 = new_s_distance_V1_V2 - new_rd_V2;
            double new_sg_V2 = new_s_distance_V2 - new_rd_V2;
            updates.add(new DataStateUpdate(s_speed_V2, new_s_speed_V2));
            updates.add(new DataStateUpdate(braking_distance_V2, new_bd_V2));
            updates.add(new DataStateUpdate(required_distance_V2, new_rd_V2));
            updates.add(new DataStateUpdate(safety_gap_V1_V2, new_sg_V1_V2));
            updates.add(new DataStateUpdate(safety_gap_V2, new_sg_V2));
        }
        return updates;
    }


    /*
    private static Perturbation perturbazioneV1( ) {
        return new AtomicPerturbation(0, Main::funzioneV1);
    }

    private static DataState funzioneV1(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        double fake_x = state.get(x) - 50;
        updates.add(new DataStateUpdate(x, fake_x));
        // }
        return state.apply(updates);
    }
    */
    private static Perturbation getSpeedSensorPerturbationV1( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV1));
        //   return new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV1);
    }

    private static DataState speedSensorPerturbationFunctionV1(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        // updates.add(new VariableUpdate(a,state.getValue(a) +100));
        // double new_timer = state.get(timer_V1);
        // if(new_timer == 0) {
            // double new_p_speed = Math.max(0, state.get(p_speed_V1) + state.get(accel_V1));
            // double offset = new_p_speed * rg.nextDouble() * MAX_SPEED_OFFSET;
            double offset = state.get(p_speed_V1) * rg.nextDouble() * MAX_SPEED_OFFSET;
            // double fake_speed = new_p_speed - offset;
            double fake_speed = state.get(p_speed_V1) - offset;
            // double travel = state.get(accel_V1) / 2 + state.get(p_speed_V1);
            // double new_p_distance = state.get(p_distance_V1) - travel;
            double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
            double fake_rd = fake_bd + SAFETY_DISTANCE;
            // double fake_sg = new_p_distance - fake_rd;
            double fake_sg = state.get(p_distance_V1) - fake_rd;
            updates.add(new DataStateUpdate(s_speed_V1, fake_speed));
            updates.add(new DataStateUpdate(required_distance_V1, fake_rd));
            updates.add(new DataStateUpdate(safety_gap_V1, fake_sg));
        // }
        return state.apply(updates);
    }


    private static Perturbation getSpeedSensorPerturbationV2( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV2));
        //   return new AtomicPerturbation(0, Main::speedSensorPerturbationFunctionV1);
    }

    private static DataState speedSensorPerturbationFunctionV2(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        double offset_V1_V2 = state.get(p_distance_V1_V2) * 15.30;
        double fake_distance_V1_V2 = state.get(p_distance_V1_V2) + offset_V1_V2;
        double new_sg_V1_V2 = fake_distance_V1_V2 - state.get(required_distance_V2);
        updates.add(new DataStateUpdate(s_distance_V1_V2, fake_distance_V1_V2));
        double fake_brake = 0;
        updates.add(new DataStateUpdate(brakelight_V1, fake_brake));
        updates.add(new DataStateUpdate(safety_gap_V1_V2, new_sg_V1_V2));
        return state.apply(updates);
    }






    public static DataState getInitialState( ) {
        Map<Integer, Double> values = new HashMap<>();
        // INITIAL DATA FOR V1
        // values.put(x, (double) 100);
        values.put(brakelight_V1, (double) 0);
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
        values.put(brakelight_V2, (double) 0);
        //   values.put(a,100.0);
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
