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

public class Isocitrate {

    /* The isocitrate dehydrogenase regulatory system (IDHKPIDH) of E. Coli controls the partioning of
    carbon flux and it is useful when the bacterium of E. coli grows on substances, like for example
    acetate, which contains only a small quantity of carbon. Without this regulation system, in fact,
    the organism would not have enough carbon available for biosynthesis of cell constituents.
    */

    /*
    public final static String[] VARIABLES =
           new String[]{
               "E", "I", "Ip", "EIp", "EIpI",
           };
    */


    // LIST OF ALL REACTIONS

    public static final int[] r1_input =  {1,0,1,0,0}; // reactants: E and Ip
    public static final int[] r1_output = {0,0,0,1,0}; // product: EIp
    public static final double r1_k = 0.02;

    public static final int[] r2_input =  {0,0,0,1,0}; // reactants: EIp
    public static final int[] r2_output = {1,0,1,0,0}; // product: E and Ip
    public static final double r2_k = 0.5;

    public static final int[] r3_input =  {0,0,0,1,0}; // reactants: EIp
    public static final int[] r3_output = {1,1,0,0,0}; // product: E and I
    public static final double r3_k = 0.5;

    public static final int[] r4_input =  {0,1,0,1,0}; // reactants: I and EIp
    public static final int[] r4_output = {0,0,0,0,1}; // product: EIpI
    public static final double r4_k = 0.02;

    public static final int[] r5_input =  {0,0,0,0,1}; // reactants: EIpI
    public static final int[] r5_output = {0,1,0,1,0}; // product: I and EIp
    public static final double r5_k = 0.5;

    public static final int[] r6_input =  {0,0,0,0,1}; // reactants: EIpI
    public static final int[] r6_output = {0,0,1,1,0}; // product: Ip and EIp
    public static final double r6_k = 0.1;


    public static final int[][] r_input = {r1_input,r2_input,r3_input,r4_input,r5_input,r6_input};
    public static final int[][] r_output = {r1_output,r2_output,r3_output,r4_output,r5_output,r6_output};

    public static final double[] r_k = {r1_k,r2_k,r3_k,r4_k,r5_k,r6_k};


    // LIST OF SPECIES
    public static final int E = 0;  // enzyme able to phosporylate and dephosporylate I
    public static final int I = 1;  // isocitrate dehydrogenase, IDH, which regulate the carbon flux
    public static final int Ip = 2; // IDH in phosporilated form,
    public static final int EIp = 3; // compound E + Ip.
    public static final int EIpI = 4; // compound EIp + I
    private static final int NUMBER_OF_VARIABLES = 5;

    public static final double THRESHOLD = 0.6;

    public static final int LEFT_BOUND = 100;

    public static final int RIGHT_BOUND = 300;




    // MAIN PROGRAM
    public static void main(String[] args) throws IOException {
        try {

            RandomGenerator rand = new DefaultRandomGenerator();

            int size = 10;

            Controller controller = new NilController();

            DataState state = getInitialState(rand,1.0,0.0,0.0,0.0);

            TimedSystem system = new TimedSystem(controller, (rg, ds) -> ds.apply(getEnvironmentUpdates(rg, ds)), state, ds->GillespieTime(new DefaultRandomGenerator(),ds));

            EvolutionSequence sequence = new EvolutionSequence(rand, rg -> system, size);

            // THE FOLLOWING INSTRUCTIONS SIMULATE <code>size*10</code>> RUNS CONISTING IN <code>RIGHT_BOUND</code> STEPS OF system.
            // THE MINIMUM AND MAXIMUM VALUES ASSUMED BY EACH VARIABLE IN INTERVAL [LEFT_BOUND,RIGHT_BOUND] ARE STORED IN minMax[0] AND minMax[1], RESP.
            ArrayList<DataStateExpression> F = new ArrayList<>();
            F.add(ds->ds.get(E));
            F.add(ds->ds.get(I));
            F.add(ds->ds.get(Ip));
            F.add(ds->ds.get(EIp));
            F.add(ds->ds.get(EIpI));
            F.add(ds->ds.getTimeDelta());
            F.add(ds->ds.getTimeReal());
            ArrayList<String> L = new ArrayList<>();
            L.add("E");
            L.add("I");
            L.add("Ip");
            L.add("EIp");
            L.add("EIpI");
            double[][] minMax = printLMinMaxData(new DefaultRandomGenerator(), L, F, system, RIGHT_BOUND, size*10, LEFT_BOUND, RIGHT_BOUND);
            System.out.println(minMax[0][1]);
            System.out.println(minMax[1][1]);

            /* The following distance expression returns the maximum, in interval [LEFT_BOUND , RIGHT_BOUND], of the
            absolute value of the difference of the value assumed by variable I in the nominal and in the perturbed system,
            normalised by the width of the interval of values that I assumes in the nominal system in interval
            [LEFT_BOUND , RIGHT_BOUND]. More precisely, if the interval is [m,M], the normalisation consists in dividing by
            1.1M - 0.9m.
            * */
            DistanceExpression distance = new MaxIntervalDistanceExpression(
                    new AtomicDistanceExpression(ds->ds.get(I)/(minMax[1][I]*1.1-minMax[0][I]*0.9),(v1, v2) -> Math.abs(v2-v1)),
                    LEFT_BOUND,
                    RIGHT_BOUND);


            // ROBUSTNESS FORMULA
            /* The following formula tells us whether the difference expressed by <code>distance</cod> between the nominal
            system and its version perturbed by <code>pertEandIp10</code> is bound by THRESHOLD
             */
            RobustnessFormula robF = new AtomicRobustnessFormula(pertEandIp(),
                    distance,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    THRESHOLD);


            TruthValues value1 = new ThreeValuedSemanticsVisitor(rand,50,1.96).eval(robF).eval(5, 0, sequence);
            System.out.println("\n robF evaluation at 0: " + value1);


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    /* This method will simulate <code>size</code> runs of lenght <code>steps</code> of system <code>s</code>.
       For each time step, each data state expression in list <code>F</code> is evaluated on all <code>s</code>
       systems and the average value is printed.
       The method returns a double[][], where:
       - double[0,j] contains the minimum value of jth expression in F
       - double[1,j] contains the maximum value of jth expression in F
    */

    private static void printLData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size){
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

    private static double[][] printLMinMaxData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound){
        double[][] result = new double[2][NUMBER_OF_VARIABLES];
        System.out.println(label);
        double[][] data = SystemState.sample(rg, F, s, steps, size);
        double[] min = new double[NUMBER_OF_VARIABLES];
        double[] max = new double[NUMBER_OF_VARIABLES];
        for(int i = 0; i < NUMBER_OF_VARIABLES ; i++){
            min[i]=Double.POSITIVE_INFINITY;
            max[i]=Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data[i].length -1 ; j++) {
                System.out.printf("%f   ", data[i][j]);
                if (j<NUMBER_OF_VARIABLES & leftbound <= i & i <= rightbound) {
                    if (min[j] > data[i][j]) {
                        min[j] = data[i][j];
                        result[0][j] = data[i][j];
                    }
                    if (max[j] < data[i][j]) {
                        max[j] = data[i][j];
                        result[1][j]=data[i][j];
                    }
                }
            }
            System.out.printf("%f\n", data[i][data[i].length -1]);
        }

        System.out.printf("%n", "min");
        for(int j=0; j<NUMBER_OF_VARIABLES; j++){
            System.out.printf("%f    ", min[j]);
        }

        System.out.printf("%n", "max");
        for(int j=0; j<NUMBER_OF_VARIABLES; j++){
            System.out.printf("%f   ", max[j]);
        }

        return result;
    }


    /* PERTURBATIONS:
    Perturbation <code>pertEandIp</code> perturbs the system state by applying function <code>changeEandIp</code> at the
    first computation step.
    Perturbation <code>pertEandIp10</code> perturbs the system state by applying funciton <code>changeEandIp</code> at the
    10th computation step.
    Perturbation <code>pertEandIp10Iter</code> applies <code>pertEandIp10</code> for 5 times.
    */
    public static Perturbation pertEandIp(){
        return new AtomicPerturbation(0,Isocitrate::changeEandIp );
    }

    public static Perturbation pertEandIpIter(){
        return new IterativePerturbation(5,pertEandIp10());
    }

    public static Perturbation pertEandIp10(){
        return new AtomicPerturbation(10, Isocitrate::changeEandIp);
    }

    // FUNCTIONS SUPPORTING PERTURBATION
    /*
    The following function changes the values of E and Ip.
    Each value v is mapped to a value in [v/10 , v*10].
     */
    private static DataState changeEandIp(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(E, state.get(E)/10.0  + rg.nextDouble()*(state.get(E)*10.0 - state.get(E)/10.0 +1) ));
        updates.add(new DataStateUpdate(Ip,state.get(Ip)/10.0 +  rg.nextDouble()*(state.get(Ip)*10.0 - state.get(Ip)/10.0 +1) ));
        return state.apply(updates);
    }



    // GILLESPIE TIME COMPUTATION
    public static double GillespieTime(RandomGenerator rg, DataState state){
        double rate = 0.0;
        double[] lambda = new double[6];
        for (int j=0; j<6; j++){
            double weight = 1.0;
            for (int i=0; i<5; i++){
                if(r_input[j][i] > 0) {
                    weight = weight * Math.pow(state.get(i), r_input[j][i]);
                }
            }
            lambda[j] = r_k[j] * weight;
            rate = rate + lambda[j];
        }

        double rand = rg.nextDouble();
        double t = (1/rate)*Math.log(1/rand);
        return t;
    }


    // ENVIRONMENT EVOLUTION

    public static List<DataStateUpdate> getEnvironmentUpdates(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();

        double[] lambda = new double[6];
        double[] lambdaParSum = new double[6];
        double lambdaSum = 0.0;

        for (int j=0; j<6; j++){
            double weight = 1.0;
            for (int i=0; i<5; i++){
                weight = weight * Math.pow(state.get(i),r_input[j][i]);
            }
            lambda[j] = r_k[j]* weight;
            lambdaSum = lambda[j]+lambdaSum;
            lambdaParSum[j] = lambdaSum;
        }

        if(lambdaSum > 0){

            double token = 1 - rg.nextDouble();

            int selReaction = 0;

            while (lambdaParSum[selReaction] <= token * lambdaSum) {
                selReaction++;
            }

            selReaction++;

            if (selReaction == 1) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r1_output[i] - r1_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }

            if (selReaction == 2) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r2_output[i] - r2_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }

            if (selReaction == 3) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r3_output[i] - r3_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }

            if (selReaction == 4) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r4_output[i] - r4_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }

            if (selReaction == 5) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r5_output[i] - r5_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }

            if (selReaction == 6) {
                for (int i = 0; i < 5; i++) {
                    double newArity = state.get(i) + r6_output[i] - r6_input[i];
                    updates.add(new DataStateUpdate(i, newArity));
                }
            }
        }

        return updates;

    }

    // INITIALISATION OF DATA STATE. The initial value for all variables are randomly chosen between 1 and 100.

    public static DataState getInitialState(RandomGenerator rand, double gran, double Tstep, double Treal, double Tdelta) {
        Map<Integer, Double> values = new HashMap<>();

        double initE = Math.ceil(100 * rand.nextDouble());
        double initI = Math.ceil(100 * rand.nextDouble());
        double initIp = Math.ceil(100 * rand.nextDouble());
        double initEIp = Math.ceil(100 * rand.nextDouble());
        double initEIpI = Math.ceil(100 * rand.nextDouble());

        values.put(E, initE);
        values.put(I, initI);
        values.put(Ip, initIp);
        values.put(EIp, initEIp);
        values.put(EIpI, initEIpI);

        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN), gran, Tstep, Treal, Tdelta);
    }

}
