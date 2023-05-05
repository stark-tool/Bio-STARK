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
import it.unicam.quasylab.jspear.perturbation.*;
import it.unicam.quasylab.jspear.distl.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class Casello {

    public final static String[] VARIABLES =
            new String[]{"p_speed_V1", "s_speed_V1", "p_distance_V1", "s_distance_V1", "accel_V1", "timer_V1",
                    "warning_V1", "braking_distance_V1", "required_distance_V1", "safety_gap_V1",
                    "crashed_V1"
            };

    public final static double ACCELERATION = 0.25;
    public final static double BRAKE = 2.0;
    public final static double NEUTRAL = 0.0;
    public final static int TIMER_INIT = 1;
    public final static int DANGER = 1;
    public final static int OK = 0;
    public final static double INIT_SPEED_V1 = 25.0;
    public final static double INIT_SPEED_V2 = 25.0;
    public final static double MAX_SPEED = 40.0;
    public final static double INIT_DISTANCE_OBS_V1 = 10000.0;
    public final static double INIT_DISTANCE_V1_V2 = 5000.0;
    private static final double SAFETY_DISTANCE = 200.0;
    private static final int H = 450;

    private static final int p_speed_V1 = 0;//variableRegistry.getVariable("p_speed");
    private static final int s_speed_V1 = 1;//variableRegistry.getVariable("s_speed");
    private static final int p_distance_V1 = 2;// variableRegistry.getVariable("p_distance");
    private static final int s_distance_V1 = 3;// variableRegistry.getVariable("s_distance");
    private static final int accel_V1 = 4;//variableRegistry.getVariable("accel");
    private static final int timer_V1 = 5;//variableRegistry.getVariable("timer");
    private static final int warning_V1 = 6;//variableRegistry.getVariable("warning");
    private static final int braking_distance_V1 = 7;//variableRegistry.getVariable("braking_distance");
    private static final int required_distance_V1 = 8; //variableRegistry.getVariable("required_distance");
    private static final int safety_gap_V1 = 9;//variableRegistry.getVariable("safety_gap");
    private static final int crashed_V1 = 10;

    private static final int NUMBER_OF_VARIABLES = 11;



    public static void main(String[] args) throws IOException {
        try {
            Controller controller_V1 = getController_V1();
            DataState state = getInitialState();
            ControlledSystem system = new ControlledSystem(controller_V1, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state);
            EvolutionSequence sequence = new EvolutionSequence(new DefaultRandomGenerator(), rg -> system, 100);

            DataStateFunction mu = (rg, ds) -> ds.apply(getTargetDistribution(rg, ds));

            DisTLFormula phi = new TargetDisTLFormula(mu, ds -> ds.get(p_distance_V1)/50, 0.5);

            double value = new DoubleSemanticsVisitor().eval(phi).eval(10, 295, sequence);

            System.out.println(value);

            //double[][] distance = new double[100][1];
            //for(int i = 0; i<100;i++) {
            //    distance[i][0] = sequence.get(295).evalPenaltyFunction(ds -> Math.abs(ds.get(p_distance_V1)-200)/50)[i];
            //}

            //Util.writeToCSV("./stopping_distance.csv",distance);

            ArrayList<String> L = new ArrayList<>();

            L.add("speed");

            //L.add("sensed_speed");

            L.add("Distance");

            L.add("gap");

            L.add("accel");


            ArrayList<DataStateExpression> F = new ArrayList<>();

            F.add(ds->ds.get(p_speed_V1));

            //F.add(ds->ds.get(s_speed_V1));

            F.add(ds->ds.get(p_distance_V1));

            F.add(ds->ds.get(safety_gap_V1));

            F.add(ds->ds.get(accel_V1));



            //printLData(new DefaultRandomGenerator(), L, F, system, 300, 100);

            //printLData_min(new DefaultRandomGenerator(), L, F, system, 300, 100);

            //printLData_max(new DefaultRandomGenerator(), L, F, system, 300, 100);

        } catch (RuntimeException e) {
            e.printStackTrace();
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

    private static void printLData_min(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample_min(rg, F, new NonePerturbation(), s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }

    private static void printLData_max(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size) {
        System.out.println(label);
        double[][] data = SystemState.sample_max(rg, F, new NonePerturbation(), s, steps, size);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
    }

    // CONTROLLER OF VEHICLE 1

    public static Controller getController_V1() {

        ControllerRegistry registry = new ControllerRegistry();

        registry.set("Ctrl_V1",
                Controller.ifThenElse(
                        DataState.greaterThan(s_speed_V1, 0),
                        Controller.ifThenElse(
                                   DataState.greaterThan(safety_gap_V1, 0 ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of(new DataStateUpdate(accel_V1, ACCELERATION),
                                                   new DataStateUpdate(timer_V1, TIMER_INIT)),
                                           registry.reference("Accelerate_V1")
                                   ),
                                   Controller.doAction(
                                           (rg, ds) -> List.of( new DataStateUpdate(accel_V1, - BRAKE),
                                                   new DataStateUpdate(timer_V1, TIMER_INIT)),
                                           registry.reference("Decelerate_V1"))
                        ),
                        Controller.ifThenElse(
                                DataState.greaterThan(safety_gap_V1,0),
                                Controller.doAction(
                                        (rg, ds) -> List.of(new DataStateUpdate(accel_V1, ACCELERATION),
                                                new DataStateUpdate(timer_V1, TIMER_INIT)),
                                        registry.reference("Accelerate_V1")
                                ),
                                Controller.doAction(
                                        (rg,ds)-> List.of(new DataStateUpdate(accel_V1,NEUTRAL),
                                                new DataStateUpdate(timer_V1,TIMER_INIT)),
                                        registry.reference("Stop_V1")
                                )
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
                        Controller.doAction((rg,ds)-> List.of(new DataStateUpdate(timer_V1,TIMER_INIT)),
                                registry.reference("Stop_V1")
                        )
                )
        );



        return registry.reference("Ctrl_V1");

    }

    public static List<DataStateUpdate> getTargetDistribution(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        Random r = new Random();
        updates.add(new DataStateUpdate(p_distance_V1, r.nextGaussian()));
        return updates;
    }


    // ENVIRONMENT EVOLUTION

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double travel_V1 = Math.max(state.get(accel_V1)/2 + state.get(p_speed_V1),0);
        double new_timer_V1 = state.get(timer_V1) - 1;
        double new_p_speed_V1 = Math.min(MAX_SPEED,Math.max(0,state.get(p_speed_V1) + state.get(accel_V1)));
        double token = rg.nextDouble();
        double new_s_speed_V1;
        if (token < 0.5){
            new_s_speed_V1 = new_p_speed_V1 + rg.nextDouble()*0.3;
        } else {
            new_s_speed_V1 = new_p_speed_V1 - rg.nextDouble()*0.3;
        }
        double new_p_distance_V1 = state.get(p_distance_V1) - travel_V1;
        updates.add(new DataStateUpdate(timer_V1, new_timer_V1));
        updates.add(new DataStateUpdate(p_speed_V1, new_p_speed_V1));
        updates.add(new DataStateUpdate(p_distance_V1, new_p_distance_V1));
        if(new_timer_V1 == 0) {
            double new_bd_V1 = (new_s_speed_V1 * new_s_speed_V1 +
                    (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                    2 * new_s_speed_V1 * TIMER_INIT)) / (2 * BRAKE);
            //double new_rd_V1 = new_bd_V1 + SAFETY_DISTANCE;
            double new_sg_V1 = new_p_distance_V1 - new_bd_V1;
            updates.add(new DataStateUpdate(s_speed_V1, new_s_speed_V1));
            updates.add(new DataStateUpdate(braking_distance_V1, new_bd_V1));
            //updates.add(new DataStateUpdate(required_distance_V1, new_rd_V1));
            updates.add(new DataStateUpdate(safety_gap_V1, new_sg_V1));
            updates.add(new DataStateUpdate(s_distance_V1,new_p_distance_V1));
        }
        //if(state.get(p_distance_V1) <=0){
        //    updates.add(new DataStateUpdate(crashed_V1, 1));
        //}
        return updates;
    }


   // INITIALISATION OF DATA STATE

    public static DataState getInitialState( ) {
        Map<Integer, Double> values = new HashMap<>();

        values.put(crashed_V1, (double) 0);
        values.put(timer_V1, (double) 0);
        values.put(p_speed_V1, INIT_SPEED_V1);
        values.put(s_speed_V1, INIT_SPEED_V1);
        values.put(p_distance_V1, INIT_DISTANCE_OBS_V1);
        values.put(s_distance_V1, INIT_DISTANCE_OBS_V1);
        values.put(accel_V1, NEUTRAL);
        values.put(warning_V1, (double) OK);
        double init_bd_V1 = (INIT_SPEED_V1 * INIT_SPEED_V1 + (ACCELERATION + BRAKE) * (ACCELERATION * TIMER_INIT * TIMER_INIT +
                2 * INIT_SPEED_V1 * TIMER_INIT))/(2 * BRAKE);
        double init_rd_V1 = init_bd_V1 + SAFETY_DISTANCE;
        double init_sg_V1 = INIT_DISTANCE_OBS_V1 - init_rd_V1;
        values.put(braking_distance_V1, init_bd_V1);
        values.put(required_distance_V1, init_rd_V1);
        values.put(safety_gap_V1, init_sg_V1);

        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN));
    }

}
