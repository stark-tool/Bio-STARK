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
            new String[] { "p_speed", "s_speed", "p_distance", "s_distance", "accel", "timer",
                    "warning", "offset", "braking_distance", "required_distance", "safety_gap"};
    public final static double ACCELERATION = 0.1;
    public final static double BRAKE = 0.3;
    public final static double NEUTRAL = 0.0;
    public final static int TIMER_INIT = 4;
    public final static int DANGER = 1;
    public final static int OK = 0;
    public final static double MAX_SPEED_OFFSET = 0.065

            ;
    public final static double INIT_SPEED = 30.0;
    public final static double MAX_SPEED = 40.0;
    public final static double INIT_DISTANCE = 3000.0;
    private static final double SAFETY_DISTANCE = 200.0;
    //private static final VariableAllocation variableRegistry = VariableAllocation.create(VARIABLES);

    // private static final Variable a = variableRegistry.getVariable("a");

    private static final int p_speed = 0;//variableRegistry.getVariable("p_speed");
    private static final int s_speed = 1;//variableRegistry.getVariable("s_speed");
    private static final int p_distance = 2;// variableRegistry.getVariable("p_distance");
    private static final int s_distance = 3;// variableRegistry.getVariable("s_distance");
    private static final int accel = 4;//variableRegistry.getVariable("accel");
    private static final int timer = 5;//variableRegistry.getVariable("timer");
    private static final int warning = 6;//variableRegistry.getVariable("warning");
    private static final int offset = 7;//variableRegistry.getVariable("offset");
    private static final int braking_distance = 8;//variableRegistry.getVariable("braking_distance");
    private static final int required_distance = 9; //variableRegistry.getVariable("required_distance");
    private static final int safety_gap = 10;//variableRegistry.getVariable("safety_gap");

    private static final int NUMBER_OF_VARIABLES = 11;

    private static final int ETA_SpeedLB = 0;
    private static final int ETA_SpeedUB = 50;
    private static final int ETA_CRASH = 0;
    private static final int H = 1000;
    private static final int ATTACK_INIT = 30;

    private static final int ATTACK_LENGTH = 100;



    public static void main(String[] args) throws IOException {
        try {
            Controller controller = getController();
            DataState state = getInitialState( );
            ControlledSystem system = new ControlledSystem(controller, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Vehicle: "), new DefaultRandomGenerator(), rg -> system, 100);
            EvolutionSequence sequenceAttSensorSpeed = sequence.apply(getSpeedSensorPerturbation(), ATTACK_INIT, 30);


            for(int i=0; i<1000; i++) {
                System.out.println(i+
                        " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(p_speed))).max() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).min() +
                        " " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.get(p_speed))).max() +
  //                              sequence.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).max())) +
                                " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.get(p_distance))).max() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_distance))).min() +
                        " " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.get(p_distance))).max()
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

            System.out.println("PHI_AttackHasSuccess: " + PHI_AttackHasSuccess.eval(100, ATTACK_INIT, sequence));


            DistanceExpression speed_expr = new AtomicDistanceExpression(ds -> ds.get(p_speed));
            DistanceExpression distance_expr = new AtomicDistanceExpression(ds -> - ds.get(p_distance));

            int n = 300;
              double[][] speed_difference = new double[n][1];
              double[][] distance_difference = new double[n][1];
              for(int i=0; i<n; i++){
                speed_difference[i][0] = speed_expr.compute(i,sequence,sequenceAttSensorSpeed);
                distance_difference[i][0] = distance_expr.compute(i,sequence,sequenceAttSensorSpeed);
             }

            Util.writeToCSV("./testSpeedDifferenceH.csv",speed_difference);
            Util.writeToCSV("./testDistanceDifferenceH.csv",distance_difference);


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
   }


   private static RobustnessFormula getFormulaInstantSpeedFakeLowerBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new AtomicDistanceExpression(ds -> ds.get(p_speed)),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }

    private static RobustnessFormula getFormulaSpeedFakeLowerBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new MinIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> ds.get(p_speed)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }


    private static RobustnessFormula getFormulaInstantSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                        new AtomicDistanceExpression(ds -> ds.get(p_speed)),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
    }
    private static RobustnessFormula getFormulaSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
               new MaxIntervalDistanceExpression(
                       new AtomicDistanceExpression(ds -> ds.get(p_speed)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
     }


    private static RobustnessFormula getFormulaInstantCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new AtomicDistanceExpression(ds ->  - Math.min(0,ds.get(p_distance))),
                RelationOperator.GREATER_THAN,
                ETA_CRASH
        );
    }
     private static RobustnessFormula getFormulaCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new MaxIntervalDistanceExpression(
                      new AtomicDistanceExpression(ds ->  - Math.min(0,ds.get(p_distance))),
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



    public static Controller getController() {

        ControllerRegistry registry = new ControllerRegistry();

        registry.set("Ctrl",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed, 0),
                        Controller.ifThenElse(
                                   DataState.greaterThan(safety_gap, 0 ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of(new DataStateUpdate(accel, ACCELERATION), new DataStateUpdate(timer, TIMER_INIT)),
                                           registry.get("Accelerate")
                                   ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of( new DataStateUpdate(accel, - BRAKE), new DataStateUpdate(timer, TIMER_INIT)),
                                           registry.get("Decelerate"))),
                        Controller.doTick(registry.get("Stop"))
                )
        );

        registry.set("Stop",
                Controller.doAction((rg, ds) -> List.of(new DataStateUpdate(accel , NEUTRAL)), registry.get("Stop"))
        );

        registry.set("Accelerate",
                Controller.ifThenElse(
                        DataState.greaterThan(timer, 0),
                        Controller.doTick(registry.get("Accelerate")),
                        registry.get("Ctrl")
                )
        );

        registry.set("Decelerate",
                Controller.ifThenElse(
                        DataState.greaterThan(timer, 0),
                        Controller.doTick(registry.get("Decelerate")),
                        registry.get("Ctrl")
                )
        );

    registry.set("IDS",
                Controller.ifThenElse(
                        DataState.lessOrEqualThan(p_distance, 2*TIMER_INIT*SAFETY_DISTANCE).and(DataState.equalsTo(accel, ACCELERATION)),
                        Controller.doAction(DataStateUpdate.set(warning, DANGER),registry.get("IDS")),
                        Controller.doAction(DataStateUpdate.set(warning, OK),registry.get("IDS"))
                )
        );
        return new ParallelController(registry.reference("Ctrl"), registry.reference("IDS"));
    }

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double travel = state.get(accel)/2 + state.get(p_speed);
        double new_timer = state.get(timer) - 1;
        double new_p_speed = Math.min(Math.max(0,state.get(p_speed) + state.get(accel)),MAX_SPEED);
        double new_p_distance = state.get(p_distance) - travel;
      //  updates.add(new VariableUpdate(a,state.getValue(a)+5));
        updates.add(new DataStateUpdate(timer, new_timer));
        updates.add(new DataStateUpdate(p_speed, new_p_speed));
        updates.add(new DataStateUpdate(p_distance, new_p_distance));
        if(new_timer == 0) {
            double new_s_speed = new_p_speed;
            double new_bd = (new_s_speed * new_s_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_s_speed * TIMER_INIT)) / (2 * BRAKE);
            double new_rd = new_bd + SAFETY_DISTANCE;
            double new_sg = new_p_distance - new_rd;
            updates.add(new DataStateUpdate(s_speed, new_s_speed));
            updates.add(new DataStateUpdate(braking_distance, new_bd));
            updates.add(new DataStateUpdate(required_distance, new_rd));
            updates.add(new DataStateUpdate(safety_gap, new_sg));
        }
        return updates;
    }

    private static Perturbation getSpeedSensorPerturbation( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::speedSensorPerturbationFunction));
    }

    private static DataState speedSensorPerturbationFunction(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        // updates.add(new VariableUpdate(a,state.getValue(a) +100));
        double new_timer = state.get(timer);
        if(new_timer == 0) {
            double new_p_speed = Math.max(0, state.get(p_speed) + state.get(accel));
            double offset = new_p_speed * rg.nextDouble() * MAX_SPEED_OFFSET;
            double fake_speed = new_p_speed - offset;
            double travel = state.get(accel) / 2 + state.get(p_speed);
            double new_p_distance = state.get(p_distance) - travel;
            double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
            double fake_rd = fake_bd + SAFETY_DISTANCE;
            double fake_sg = new_p_distance - fake_rd;
            updates.add(new DataStateUpdate(s_speed, fake_speed));
            updates.add(new DataStateUpdate(required_distance, fake_rd));
            updates.add(new DataStateUpdate(safety_gap, fake_sg));
        }
        return state.apply(updates);
    }







    public static DataState getInitialState( ) {
        Map<Integer, Double> values = new HashMap<>();
        values.put(timer, (double) 0);
     //   values.put(a,100.0);
        values.put(p_speed, INIT_SPEED);
        values.put(s_speed, INIT_SPEED);
        values.put(p_distance, INIT_DISTANCE);
        values.put(s_distance, INIT_DISTANCE);
        values.put(accel, NEUTRAL);
        values.put(warning, (double) OK);
        values.put(offset, 0.0);
        double init_bd = (INIT_SPEED * INIT_SPEED + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * INIT_SPEED * TIMER_INIT))/(2 * BRAKE);
        double init_rd = init_bd + SAFETY_DISTANCE;
        double init_sg = INIT_DISTANCE - init_rd;
        values.put(braking_distance, init_bd);
        values.put(required_distance, init_rd);
        values.put(safety_gap, init_sg);
        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN));
    }

}
