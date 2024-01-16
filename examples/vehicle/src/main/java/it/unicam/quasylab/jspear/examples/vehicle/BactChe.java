/*
 * STARK: Software Tool for the Analysis of Robustness in the unKnown environment
 *
 *                Copyright (C) 2023.
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

import it.unicam.quasylab.jspear.DefaultRandomGenerator;
import it.unicam.quasylab.jspear.EvolutionSequence;
import it.unicam.quasylab.jspear.SystemState;
import it.unicam.quasylab.jspear.TimedSystem;
import it.unicam.quasylab.jspear.controller.Controller;
import it.unicam.quasylab.jspear.controller.NilController;
import it.unicam.quasylab.jspear.distance.AtomicDistanceExpression;
import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.distance.MaxIntervalDistanceExpression;
import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.ds.RelationOperator;
import it.unicam.quasylab.jspear.perturbation.AtomicPerturbation;
import it.unicam.quasylab.jspear.perturbation.IterativePerturbation;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.robtl.AtomicRobustnessFormula;
import it.unicam.quasylab.jspear.robtl.RobustnessFormula;
import it.unicam.quasylab.jspear.robtl.ThreeValuedSemanticsVisitor;
import it.unicam.quasylab.jspear.robtl.TruthValues;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class BactChe {

    public final static String[] VARIABLES =
            new String[]{
                    "X", "Xstar", "L", "CheY", "Z", "CheYp", "XL", "Xstarm", "CheR" , "XY" , "CheB", "CheBp", "Xm" , "TIME"
            };

    public static final int[] r1_input =  {1,0,0,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r1_output = {0,1,0,0,0,0,0,0,0,0,0,0,0};
    public static final double r1_k = 1.15;

    public static final int[] r2_input =  {0,1,0,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r2_output = {1,0,0,0,0,0,0,0,0,0,0,0,0};
    public static final double r2_k = 0.25;

    public static final int[] r3_input =  {0,1,0,1,0,0,0,0,0,0,0,0,0};
    public static final int[] r3_output = {1,0,0,0,0,1,0,0,0,0,0,0,0};
    public static final double r3_k = 0.1;

    public static final int[] r4_input =  {0,0,0,0,1,1,0,0,0,0,0,0,0};
    public static final int[] r4_output = {0,0,0,1,1,0,0,0,0,0,0,0,0};
    public static final double r4_k = 10.0;

    public static final int[] r5_input =  {0,0,0,0,0,1,0,0,0,0,0,0,0};
    public static final int[] r5_output = {0,0,0,1,0,0,0,0,0,0,0,0,0};
    public static final double r5_k = 0.002;

    public static final int[] r6_input =  {0,1,1,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r6_output = {0,0,1,0,0,0,0,0,0,1,0,0,0};
    public static final double r6_k = 1.0;

    public static final int[] r7_input =  {0,0,0,0,0,0,0,0,0,0,0,1,0};
    public static final int[] r7_output = {0,0,0,0,0,0,0,0,0,0,1,0,0};
    public static final double r7_k = 1.0;

    public static final int[] r8_input =  {0,0,0,0,0,0,0,0,0,1,0,0,0};
    public static final int[] r8_output = {0,0,0,0,0,0,1,0,0,0,0,0,0};
    public static final double r8_k = 80.0;

    public static final int[] r9_input =  {0,0,0,0,0,0,1,0,1,0,0,0,0};
    public static final int[] r9_output = {0,0,0,0,0,0,0,1,1,0,0,0,0};
    public static final double r9_k = 0.01;

    public static final int[] r10_input =  {0,0,0,0,0,0,0,1,0,0,1,0,0};
    public static final int[] r10_output = {0,0,0,0,0,0,0,1,0,0,0,1,0};
    public static final double r10_k = 0.2;

    public static final int[] r11_input =  {0,0,0,0,0,0,0,1,0,0,0,1,0};
    public static final int[] r11_output = {0,2,0,0,0,0,0,0,0,0,0,1,0};
    public static final double r11_k = 1.0;

    public static final int[] r12_input =  {0,0,0,0,0,0,0,1,0,0,0,0,0};
    public static final int[] r12_output = {0,0,0,0,0,0,0,0,0,0,0,0,1};
    public static final double r12_k = 0.25;

    public static final int[] r13_input =  {0,0,0,0,0,0,0,0,0,0,0,0,1};
    public static final int[] r13_output = {0,0,0,0,0,0,0,1,0,0,0,0,0};
    public static final double r13_k = 1.15;

    public static final int[] r14_input =  {0,0,0,1,0,0,0,1,0,0,0,0,0};
    public static final int[] r14_output = {0,0,0,0,0,1,0,0,0,0,0,0,1};
    public static final double r14_k = 0.18;

    public static final int[][] r_input = {r1_input,r2_input,r3_input,r4_input,r5_input,r6_input,r7_input,r8_input,r9_input,r10_input,r11_input,r12_input,r13_input,r14_input};
    public static final int[][] r_output = {r1_output,r2_output,r3_output,r4_output,r5_output,r6_output,r7_output,r8_output,r9_output,r10_output,r11_output,r12_output,r13_output,r14_output};

    public static final double[] r_k = {r1_k,r2_k,r3_k,r4_k,r5_k,r6_k,r7_k,r8_k,r9_k,r10_k,r11_k,r12_k,r13_k,r14_k};

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
    public static final int Xstar = 1;
    public static final int L = 2;
    public static final int CheY = 3;
    public static final int Z = 4;
    public static final int CheYp = 5;
    public static final int XL = 6;
    public static final int Xstarm = 7;
    public static final int CheR = 8;
    public static final int XY = 9;
    public static final int CheB = 10;
    public static final int CheBp = 11;
    public static final int Xm = 12;


    public static final int TIME = 13;

    private static final int NUMBER_OF_VARIABLES = 14;

    //public static final ArrayList<Double> tempi = new ArrayList<>();





    public static void main(String[] args) throws IOException {
        try {

            RandomGenerator rand = new DefaultRandomGenerator();

            int size = 1;

            Controller controller = new NilController();

            DataState state = getInitialState(1.0,0.0,0.0,0.0);

            TimedSystem system = new TimedSystem(controller, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state, ds->GillespieTime(new DefaultRandomGenerator(),ds));

            EvolutionSequence sequence = new EvolutionSequence(rand, rg -> system, size);

            ArrayList<DataStateExpression> F = new ArrayList<>();

            F.add(ds->ds.get(L));

            F.add(ds->ds.get(CheR));

            F.add(ds->ds.get(CheYp));

            F.add(ds->ds.getTimeDelta());

            ArrayList<String> L = new ArrayList<>();

            L.add("L");

            L.add("CheR");

            L.add("CheYp");

            printLData(new DefaultRandomGenerator(), L, F, system, 40, size);


        System.out.println("ciao");
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

    private static void printLData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size){
        System.out.println(label);
        double[][] data = SystemState.sample(rg, F, s, steps, size);
        //System.out.println("ciao");
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1; j++) {
                System.out.printf("%f   ", data[i][j]);
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }
        //System.out.println("ciao");
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
        return new AtomicPerturbation(10, BactChe::changeXandY);
    }

    private static DataState changeXandY(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(X, state.get(X) + 2500));
        updates.add(new DataStateUpdate(X, state.get(X) + 10000));
        return state.apply(updates);
    }



    public static double GillespieTime(RandomGenerator rg, DataState state){
        double rate = 0.0;
        double[] lambda = new double[14];
        for (int j=0; j<14; j++){
            double weight = 1.0;
            for (int i=0; i<13; i++){
                weight = weight * Math.pow(state.get(i) , r_input[j][i]);
            }
            lambda[j] = r_k[j]* weight;
            rate = rate + lambda[j];
            if(rate==0.0){System.out.println("KKKKKKKKKKKKKKKKK");}
        }

        //System.out.println(rate);
        Random random = new Random();
        double rand = random.nextDouble();
        double t = (1/rate)*Math.log(1/rand);
        return t;
    }


    // ENVIRONMENT EVOLUTION

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        // double rate = 0.0;
        double[] lambda = new double[14];
        double[] lambdaParSum = new double[14];
        double lambdaSum = 0.0;
        for (int j=0; j<14; j++){
            double weight = 1.0;
            for (int i=0; i<13; i++){
                weight = weight * Math.pow(state.get(i),r_input[j][i]);
            }
            lambda[j] = r_k[j]* weight;
            lambdaSum = lambda[j]+lambdaSum;
            lambdaParSum[j] = lambdaSum;
            // rate = rate + lambda[j];
        }

        Random r = new Random();
        double token = r.nextDouble();

        int selReaction = 0;

        while(lambdaParSum[selReaction] <= token * lambdaSum){
            selReaction++;
        }

        selReaction++;


        // double e = Math.exp(-rate*state.getTimeDelta());

        //if (token < (1-e)*lambda[0]/rate){
        if (selReaction == 1){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r1_output[i] - r1_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 2){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r2_output[i] - r2_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 3){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r3_output[i] - r3_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 4){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r4_output[i] - r4_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 5){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r5_output[i] - r5_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 6){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r6_output[i] - r6_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 7){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r7_output[i] - r7_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 8){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r8_output[i] - r8_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 9){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r9_output[i] - r9_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 10){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r10_output[i] - r10_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 11){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r11_output[i] - r11_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 12){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r12_output[i] - r12_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 13){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r13_output[i] - r13_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        if (selReaction == 14){
            for(int i = 0; i<13; i++){
                double newArity = state.get(i) + r14_output[i] - r14_input[i];
                updates.add(new DataStateUpdate(i, newArity));
            }
        }

        //updates.add(new DataStateUpdate(Tinf, state.get(Tsup)));

        return updates;
    }

    // INITIALISATION OF DATA STATE

    public static DataState getInitialState(double gran, double Tstep, double Treal, double Tdelta) {
        Map<Integer, Double> values = new HashMap<>();
        values.put(X, 10.0);
        values.put(Xstar, 10.0);
        values.put(L, 1.0);
        values.put(CheY, 10.0);
        values.put(Z, 1.0);
        values.put(CheYp, 1.0);
        values.put(XL, 0.0);
        values.put(Xstarm, 1.0);
        values.put(CheR, 1000.0);
        values.put(XY, 0.0);
        values.put(CheB, 2.0);
        values.put(CheBp, 0.0);
        values.put(Xm, 0.0);

        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN), gran, Tstep, Treal, Tdelta);
    }

}
