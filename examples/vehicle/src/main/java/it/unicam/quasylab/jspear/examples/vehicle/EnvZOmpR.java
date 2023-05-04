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
import it.unicam.quasylab.jspear.controller.NilController;
import it.unicam.quasylab.jspear.distance.AtomicDistanceExpression;
import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.distance.MaxIntervalDistanceExpression;
import it.unicam.quasylab.jspear.ds.*;
import it.unicam.quasylab.jspear.perturbation.*;
import it.unicam.quasylab.jspear.robtl.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class EnvZOmpR {

    public final static String[] VARIABLES =
            new String[]{
                    "X", "Y", "XT", "XP", "XPY", "YP", "XDYP", "XD", "TIME"
            };

    public static final int[] r1_input = {0,0,0,0,0,0,0,1};
    public static final int[] r1_output = {1,0,0,0,0,0,0,0};
    public static final double r1_k = 0.5;

    public static final int[] r2_input = {1,0,0,0,0,0,0,0};
    public static final int[] r2_output = {0,0,0,0,0,0,0,1};
    public static final double r2_k = 0.5;

    public static final int[] r3_input = {0,0,1,0,0,0,0,0};
    public static final int[] r3_output = {1,0,0,0,0,0,0,0};
    public static final double r3_k = 0.5;

    public static final int[] r4_input = {1,0,0,0,0,0,0,0};
    public static final int[] r4_output = {0,0,1,0,0,0,0,0};
    public static final double r4_k = 0.5;

    public static final int[] r5_input = {0,0,1,0,0,0,0,0};
    public static final int[] r5_output = {0,0,0,1,0,0,0,0};
    public static final double r5_k = 0.1;

    public static final int[] r6_input = {0,1,0,1,0,0,0,0};
    public static final int[] r6_output = {0,0,0,0,1,0,0,0};
    public static final double r6_k = 0.02;

    public static final int[] r7_input = {0,0,0,0,1,0,0,0};
    public static final int[] r7_output = {0,1,0,1,0,0,0,0};
    public static final double r7_k = 0.5;

    public static final int[] r8_input = {0,0,0,0,1,0,0,0};
    public static final int[] r8_output = {1,0,0,0,0,1,0,0};
    public static final double r8_k = 0.5;

    public static final int[] r9_input = {0,0,0,0,0,1,0,1};
    public static final int[] r9_output = {0,0,0,0,0,0,1,0};
    public static final double r9_k = 0.02;

    public static final int[] r10_input = {0,0,0,0,0,0,1,0};
    public static final int[] r10_output = {0,0,0,0,0,1,0,1};
    public static final double r10_k = 0.5;

    public static final int[] r11_input = {0,0,0,0,0,0,1,0};
    public static final int[] r11_output = {0,1,0,0,0,0,0,1};
    public static final double r11_k = 0.1;

    public static final int[][] r_input = {r1_input,r2_input,r3_input,r4_input,r5_input,r6_input,r7_input,r8_input,r9_input,r10_input,r11_input};
    public static final int[][] r_output = {r1_output,r2_output,r3_output,r4_output,r5_output,r6_output,r7_output,r8_output,r9_output,r10_output,r11_output};

    public static final double[] r_k = {r1_k,r2_k,r3_k,r4_k,r5_k,r6_k,r7_k,r8_k,r9_k,r10_k,r11_k};

    public static final double H = 3000;
    public static final double ETA_007 = 0.07;
    public static final double ETA_005 = 0.05;
    public static final double ETA_003 = 0.03;

    public static final double ETA_012 = 0.12;
    public static final double ETA_010 = 0.10;
    public static final double ETA_001 = 0.01;

    public static final double ETA_008 = 0.08;
    public static final double ETA_002 = 0.02;


    public static final int X = 0;
    public static final int Y = 1;
    public static final int XT = 2;
    public static final int XP = 3;
    public static final int XPY = 4;
    public static final int YP = 5;
    public static final int XDYP = 6;
    public static final int XD = 7;
    public static final int TIME = 8;

    private static final int NUMBER_OF_VARIABLES = 9;

    //public static final ArrayList<Double> tempi = new ArrayList<>();





    public static void main(String[] args) throws IOException {
        try {

            int size = 50;

            Controller controller = new NilController();

            DataState state = getInitialState(1.0,0.0,0.0,0.0);

            TimedSystem system = new TimedSystem(controller, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)),state, ds->GillespieTime(new DefaultRandomGenerator(),ds));

            EvolutionSequence sequence = new EvolutionSequence(new DefaultRandomGenerator(), rg -> system, size);
            EvolutionSequence sequence2 = sequence.apply(addXY(),10,10);

            DistanceExpression distance = new MaxIntervalDistanceExpression(
                    new AtomicDistanceExpression(ds->ds.get(YP)/40),
                    200,
                    300);

            RobustnessFormula Phi_007 = new AtomicRobustnessFormula(addXY(),
                            distance,
                            RelationOperator.LESS_OR_EQUAL_THAN,
                            ETA_007);

            RobustnessFormula Phi_005 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_005);

            RobustnessFormula Phi_003 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_003);

            RobustnessFormula Phi_012 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_012);

            RobustnessFormula Phi_010 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_010);

            RobustnessFormula Phi_001 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_001);

            RobustnessFormula Phi_008 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_008);

            RobustnessFormula Phi_002 = new AtomicRobustnessFormula(addXY(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    ETA_002);



            //double[][] val_200_007 = new double[10][1];
            //double[][] val_200_005 = new double[10][1];
            //double[][] val_200_003 = new double[10][1];

            TruthValues value1 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_012).eval(10, 0, sequence);
            System.out.println("Phi_012 evaluation at 0: " + value1);
            TruthValues value2 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_010).eval(10, 0, sequence);
            System.out.println("Phi_010 evaluation at 0: " + value2);
            //TruthValues value3 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_001).eval(10, 0, sequence);
            //System.out.println("Phi_001 evaluation at 0: " + value3);

            /*
            for(int i = 0; i<10; i++) {
                int step = i*10;
                TruthValues value1 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_200_007).eval(10, step, sequence);
                System.out.println("Phi_200_007 evaluation at step "+step+": " + value1);
                if (value1 == TruthValues.TRUE) {
                    val_200_007[i][0] = 1;
                } else {
                    if (value1 == TruthValues.UNKNOWN) {
                        val_200_007[i][0] = 0;
                    } else {
                        val_200_007[i][0] = -1;
                    }
                }
                TruthValues value2 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_200_005).eval(10, step, sequence);
                System.out.println("Phi_200_005 evaluation at step "+step+": " + value2);
                if (value2 == TruthValues.TRUE) {
                    val_200_005[i][0] = 1;
                } else {
                    if (value2 == TruthValues.UNKNOWN) {
                        val_200_005[i][0] = 0;
                    } else {
                        val_200_005[i][0] = -1;
                    }
                }
                TruthValues value3 = new ThreeValuedSemanticsVisitor(50,1.96).eval(Phi_200_003).eval(10, step, sequence);
                System.out.println("Phi_200_003 evaluation at step "+step+": " + value3);
                if (value3 == TruthValues.TRUE) {
                    val_200_003[i][0] = 1;
                } else {
                    if (value3 == TruthValues.UNKNOWN) {
                        val_200_003[i][0] = 0;
                    } else {
                        val_200_003[i][0] = -1;
                    }
                }
            }



            Util.writeToCSV("./eta_007.csv",val_200_007);
            Util.writeToCSV("./eta_005.csv",val_200_005);
            Util.writeToCSV("./eta_003.csv",val_200_003);

             */


            /*

            ArrayList<String> L = new ArrayList<>();

            L.add("YP");

            L.add("Y");

            L.add("X");

            //L.add("Tempo step");

            //L.add("Granularity");

            ArrayList<DataStateExpression> F = new ArrayList<>();

            F.add(ds->ds.get(YP));

            F.add(ds->ds.get(Y));

            F.add(ds->ds.get(X));

            //F.add(ds->ds.getTimeStep());

            //F.add(ds->ds.getGranularity());

            //printLData(new DefaultRandomGenerator(), L, F, addXY(), system, 500, size*10);


            //double[] test = sequence2.evalPenaltyFunction(F.get(2),19);
            //double[] test2 = sequence2.evalPenaltyFunction(F.get(2),20);
            //double[] test3 = sequence2.evalPenaltyFunction(F.get(2),21);

            //System.out.println(test[0]);
            //System.out.println(test2[0]);
            //System.out.println(test3[0]);


            double sommaX = 0.0;
            double sommatau = 0.0;

            double[] test = sequence2.evalPenaltyFunction(F.get(2),21);
            double[] test2 = sequence2.evalPenaltyFunction(ds-> ds.getTimeDelta(),21);
            for (int j = 0; j<size*10; j++) {
                sommaX = sommaX + test[j];
                sommatau = sommatau + test2[j];
            }

            System.out.println("Media X: "+sommaX/(size*10));
            System.out.println("Media tau: "+sommatau/(size*10));



            double sommaY = 0.0;

            for(int i=100; i<500;i++){
                double[] test = sequence2.evalPenaltyFunction(F.get(1),i);
                for(int j=0; j<size*10; j++) {
                    sommaY = sommaY + test[j];
                }
            }

            System.out.println("Media Y: "+sommaY/(400*size*10));

            double sommaX = 0.0;

            for(int i=100; i<500;i++){
                double[] test = sequence2.evalPenaltyFunction(F.get(2),i);
                for(int j=0; j<size*10; j++) {
                    sommaX = sommaX + test[j];
                }
            }

            System.out.println("Media X: "+sommaX/(400*size*10));

             */


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

    // PERTURBATIONS

    public static Perturbation ItAddXY(){
        return new IterativePerturbation(5,addXY());
    }

    public static Perturbation addXY(){
        return new AtomicPerturbation(10, EnvZOmpR::changeXandY);
    }

    private static DataState changeXandY(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        //double randX = rg.nextDouble()*250;
        //double randY = rg.nextDouble()*1000;
        updates.add(new DataStateUpdate(X, state.get(X) + 2500));
        updates.add(new DataStateUpdate(Y, state.get(Y) + 10000));
        return state.apply(updates);
    }




    public static double GillespieTime(RandomGenerator rg, DataState state){
        double rate = 0.0;
        double[] lambda = new double[11];
        for (int j=0; j<11; j++){
            double weight = 1.0;
            for (int i=0; i<8; i++){
                weight = weight* Math.pow(state.get(i),r_input[j][i]);
            }
            lambda[j] = r_k[j]* weight;
            rate = rate + lambda[j];
        }

        Random random = new Random();
        double rand = random.nextDouble();
        double t = (1/rate)*Math.log(1/rand);
        return t;
    }


    // ENVIRONMENT EVOLUTION

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        double rate = 0.0;
        double[] lambda = new double[11];
        for (int j=0; j<11; j++){
            double weight = 1.0;
            for (int i=0; i<8; i++){
                weight = weight* Math.pow(state.get(i),r_input[j][i]);
            }
            lambda[j] = r_k[j]* weight;
            rate = rate + lambda[j];
        }

        //Random random = new Random();
        //double rand = random.nextDouble();
        //double t = (1/rate)*Math.log(1/rand);

        //double newTime = state.get(TIME) + t;

        //updates.add(new DataStateUpdate(TIME, newTime));

        double e = Math.exp(-rate*state.getTimeDelta());

        Random r = new Random();
        double token = r.nextDouble();
        if (token < (1-e)*lambda[0]/rate){
            for(int i = 0; i<8; i++){
                double newArity = state.get(i) + r1_output[i] - r1_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        } else {
            if(token < (1-e)*(lambda[0] + lambda[1])/rate){
                for(int i = 0; i<8; i++){
                    double newArity = state.get(i) + r2_output[i] - r2_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            } else {
                if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2])/rate){
                    for(int i = 0; i<8; i++){
                        double newArity = state.get(i) + r3_output[i] - r3_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                } else {
                    if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3])/rate){
                        for(int i = 0; i<8; i++){
                            double newArity = state.get(i) + r4_output[i] - r4_input[i];
                            updates.add(new DataStateUpdate(i, newArity));
                        }
                    } else {
                        if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4])/rate){
                            for(int i = 0; i<8; i++){
                                double newArity = state.get(i) + r5_output[i] - r5_input[i];
                                updates.add(new DataStateUpdate(i, newArity));
                            }
                        } else {
                            if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5])/rate){
                                for(int i = 0; i<8; i++){
                                    double newArity = state.get(i) + r6_output[i] - r6_input[i];
                                    updates.add(new DataStateUpdate(i, newArity));
                                }
                            } else {
                                if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5] + lambda[6])/rate){
                                    for(int i = 0; i<8; i++){
                                        double newArity = state.get(i) + r7_output[i] - r7_input[i];
                                        updates.add(new DataStateUpdate(i, newArity));
                                    }
                                } else {
                                    if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5] + lambda[6] + lambda[7])/rate){
                                        for(int i = 0; i<8; i++){
                                            double newArity = state.get(i) + r8_output[i] - r8_input[i];
                                            updates.add(new DataStateUpdate(i, newArity));
                                        }
                                    } else {
                                        if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5] + lambda[6] + lambda[7] + lambda[8])/rate){
                                            for(int i = 0; i<8; i++){
                                                double newArity = state.get(i) + r9_output[i] - r9_input[i];
                                                updates.add(new DataStateUpdate(i, newArity));
                                            }
                                        } else {
                                            if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5] + lambda[6] + lambda[7] + lambda[8] + lambda[9])/rate){
                                                for(int i = 0; i<8; i++){
                                                    double newArity = state.get(i) + r10_output[i] - r10_input[i];
                                                    updates.add(new DataStateUpdate(i, newArity));
                                                }
                                            } else {
                                                if(token < (1-e)*(lambda[0] + lambda[1] + lambda[2] + lambda[3] + lambda[4] + lambda[5] + lambda[6] + lambda[7] + lambda[8] + lambda[9] + lambda[10])/rate){
                                                    for(int i = 0; i<8; i++){
                                                        double newArity = state.get(i) + r11_output[i] - r11_input[i];
                                                        updates.add(new DataStateUpdate(i, newArity));
                                                    }
                                                } else {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //updates.add(new DataStateUpdate(Tinf, state.get(Tsup)));

        return updates;
    }

    // INITIALISATION OF DATA STATE

    public static DataState getInitialState(double gran, double Tstep, double Treal, double Tdelta) {
        Map<Integer, Double> values = new HashMap<>();
        values.put(X, 25.0);
        values.put(Y, 150.0);
        values.put(XT, 0.0);
        values.put(XP, 0.0);
        values.put(XPY, 0.0);
        values.put(YP, 10.0);
        values.put(XDYP, 0.0);
        values.put(XD, 50.0);
        values.put(TIME, 0.0);

        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN), gran, Tstep, Treal, Tdelta);
    }

}
