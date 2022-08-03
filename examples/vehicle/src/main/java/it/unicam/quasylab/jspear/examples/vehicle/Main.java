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
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

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
    private static final VariableRegistry variableRegistry = new VariableRegistry(VARIABLES);

    // private static final Variable a = variableRegistry.getVariable("a");

    private static final Variable p_speed = variableRegistry.getVariable("p_speed");
    private static final Variable s_speed = variableRegistry.getVariable("s_speed");
    private static final Variable p_distance = variableRegistry.getVariable("p_distance");
    private static final Variable s_distance = variableRegistry.getVariable("s_distance");
    private static final Variable accel = variableRegistry.getVariable("accel");
    private static final Variable timer = variableRegistry.getVariable("timer");
    private static final Variable warning = variableRegistry.getVariable("warning");
    private static final Variable offset = variableRegistry.getVariable("offset");
    private static final Variable braking_distance = variableRegistry.getVariable("braking_distance");
    private static final Variable required_distance = variableRegistry.getVariable("required_distance");
    private static final Variable safety_gap = variableRegistry.getVariable("safety_gap");
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
            ControlledSystem system = new ControlledSystem(controller, (rg, ds) -> ds.set(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new ConsoleMonitor("Vehicle: "), new DefaultRandomGenerator(), rg -> system, 100);
            EvolutionSequence sequenceAttSensorSpeed = sequence.apply(getSpeedSensorPerturbation(), ATTACK_INIT, 30);


            for(int i=0; i<1000; i++) {
                System.out.println(i+
                        " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).max() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).min() +
                        " " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).max() +
  //                              sequence.get(i).evalPenaltyFunction(ds -> ds.getValue(p_speed))).max())) +
                                " " + Arrays.stream(sequence.get(i).evalPenaltyFunction(ds -> ds.getValue(p_distance))).max() +
                        //" " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_distance))).min() +
                        " " + Arrays.stream(sequenceAttSensorSpeed.get(i).evalPenaltyFunction(ds -> ds.getValue(p_distance))).max()
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


            DistanceExpression speed_expr = new AtomicDistanceExpression(ds -> ds.getValue(p_speed));
            DistanceExpression distance_expr = new AtomicDistanceExpression(ds -> - ds.getValue(p_distance));

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
                new AtomicDistanceExpression(ds -> ds.getValue(p_speed)),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }

    private static RobustnessFormula getFormulaSpeedFakeLowerBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new MinIntervalDistanceExpression(
                        new AtomicDistanceExpression(ds -> ds.getValue(p_speed)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.GREATER_OR_EQUAL_THAN,
                ETA_SpeedLB
        );
    }


    private static RobustnessFormula getFormulaInstantSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                        new AtomicDistanceExpression(ds -> ds.getValue(p_speed)),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
    }
    private static RobustnessFormula getFormulaSpeedFakeUpperBound() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
               new MaxIntervalDistanceExpression(
                       new AtomicDistanceExpression(ds -> ds.getValue(p_speed)),
                        ATTACK_INIT,
                        ATTACK_INIT+ATTACK_LENGTH-1
                ),
                RelationOperator.LESS_OR_EQUAL_THAN,
                ETA_SpeedUB
        );
     }


    private static RobustnessFormula getFormulaInstantCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new AtomicDistanceExpression(ds ->  - Math.min(0,ds.getValue(p_distance))),
                RelationOperator.GREATER_THAN,
                ETA_CRASH
        );
    }
     private static RobustnessFormula getFormulaCrash() {
        return new AtomicRobustnessFormula(getSpeedSensorPerturbation(),
                new MaxIntervalDistanceExpression(
                      new AtomicDistanceExpression(ds ->  - Math.min(0,ds.getValue(p_distance))),
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
                        variableRegistry.greaterThan("s_speed", 0),
                        Controller.ifThenElse(
                                   variableRegistry.greaterThan("safety_gap", 0 ),
                                   Controller.doAction(variableRegistry.set("accel", ACCELERATION).compose(variableRegistry.set("timer", TIMER_INIT)),registry.get("Accelerate")),
                                   Controller.doAction(variableRegistry.set("accel", - BRAKE).compose(variableRegistry.set("timer", TIMER_INIT)),registry.get("Decelerate"))),
                        Controller.doTick(registry.get("Stop"))
                )
        );

        registry.set("Stop",
                Controller.doAction(variableRegistry.set("accel" , NEUTRAL), registry.get("Stop"))
        );

        registry.set("Accelerate",
                Controller.ifThenElse(
                        variableRegistry.greaterThan("timer", 0),
                        Controller.doTick(registry.get("Accelerate")),
                        registry.get("Ctrl")
                )
        );

        registry.set("Decelerate",
                Controller.ifThenElse(
                        variableRegistry.greaterThan("timer", 0),
                        Controller.doTick(registry.get("Decelerate")),
                        registry.get("Ctrl")
                )
        );

    registry.set("IDS",
                Controller.ifThenElse(
                        variableRegistry.lessOrEqualThan("p_distance", 2*TIMER_INIT*SAFETY_DISTANCE).and(variableRegistry.equalsTo("accel", ACCELERATION)),
                        Controller.doAction(variableRegistry.set("warning", DANGER),registry.get("IDS")),
                        Controller.doAction(variableRegistry.set("warning", OK),registry.get("IDS"))
                )
        );
        return new ParallelController(registry.reference("Ctrl"), registry.reference("IDS"));
    }

    public static List<VariableUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<VariableUpdate> updates = new LinkedList<>();
        double travel = state.getValue(accel)/2 + state.getValue(p_speed);
        double new_timer = state.getValue(timer) - 1;
        double new_p_speed = Math.min(Math.max(0,state.getValue(p_speed) + state.getValue(accel)),MAX_SPEED);
        double new_p_distance = state.getValue(p_distance) - travel;
      //  updates.add(new VariableUpdate(a,state.getValue(a)+5));
        updates.add(new VariableUpdate(timer, new_timer));
        updates.add(new VariableUpdate(p_speed, new_p_speed));
        updates.add(new VariableUpdate(p_distance, new_p_distance));
        if(new_timer == 0) {
            double new_s_speed = new_p_speed;
            double new_bd = (new_s_speed * new_s_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_s_speed * TIMER_INIT)) / (2 * BRAKE);
            double new_rd = new_bd + SAFETY_DISTANCE;
            double new_sg = new_p_distance - new_rd;
            updates.add(new VariableUpdate(s_speed, new_s_speed));
            updates.add(new VariableUpdate(braking_distance, new_bd));
            updates.add(new VariableUpdate(required_distance, new_rd));
            updates.add(new VariableUpdate(safety_gap, new_sg));
        }
        return updates;
    }

    private static Perturbation getSpeedSensorPerturbation( ) {
        return new IterativePerturbation(ATTACK_LENGTH, new AtomicPerturbation(0, Main::speedSensorPerturbationFunction));
    }

    private static DataState speedSensorPerturbationFunction(RandomGenerator rg, DataState state){
        List<VariableUpdate> updates = new LinkedList<>();
        // updates.add(new VariableUpdate(a,state.getValue(a) +100));
        double new_timer = state.getValue(timer);
        if(new_timer == 0) {
            double new_p_speed = Math.max(0, state.getValue(p_speed) + state.getValue(accel));
            double offset = new_p_speed * rg.nextDouble() * MAX_SPEED_OFFSET;
            double fake_speed = new_p_speed - offset;
            double travel = state.getValue(accel) / 2 + state.getValue(p_speed);
            double new_p_distance = state.getValue(p_distance) - travel;
            double fake_bd = (fake_speed * fake_speed + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * fake_speed * TIMER_INIT)) / (2 * BRAKE);
            double fake_rd = fake_bd + SAFETY_DISTANCE;
            double fake_sg = new_p_distance - fake_rd;
            updates.add(new VariableUpdate(s_speed, fake_speed));
            updates.add(new VariableUpdate(required_distance, fake_rd));
            updates.add(new VariableUpdate(safety_gap, fake_sg));
        }
        return state.set(updates);
    }







    public static DataState getInitialState( ) {
        Map<Variable, Double> values = new HashMap<>();
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
        return new DataState(variableRegistry, values);
    }

}
