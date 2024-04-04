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

import it.unicam.quasylab.jspear.*;
import it.unicam.quasylab.jspear.controller.Controller;
import it.unicam.quasylab.jspear.controller.NilController;
import it.unicam.quasylab.jspear.distance.AtomicDistanceExpression;
import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.distance.MaxIntervalDistanceExpression;
import it.unicam.quasylab.jspear.distance.MinIntervalDistanceExpression;
import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.ds.RelationOperator;
import it.unicam.quasylab.jspear.perturbation.AtomicPerturbation;
import it.unicam.quasylab.jspear.perturbation.IterativePerturbation;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.robtl.AlwaysRobustnessFormula;
import it.unicam.quasylab.jspear.robtl.AtomicRobustnessFormula;
import it.unicam.quasylab.jspear.robtl.RobustnessFormula;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class Repressilator {

    /*
    The "repressilator" network consist in 3 genes forming a directed cycle of "negative interactions".
    We consider the "two state model" of gene expression: the gene promoter can be either active or inactive.
    Then, we consider mRNA molecules, which can be transcribed only during the active period, and proteins,
    which are produced by mRNA molecules at a constant rate. For i=1,2,3 we have the following variables:
    - Gi: models the inactive promoter, Gi is 1 if the promoter is inactive, otherwise Gi is 0.
    - AGi: models the active promoter, AGi is 1 if the promoter is active, otherwise AGi is 0.
      It always holds that: Gi + AGi = 1.
    - Xi: amount of mRNA molecules.
    - Zi: amount of proteins.
    - koni: rate constant of gene i activation
    - koffi: rate constant of gene i deactivation.
    - s0i: rate constant of transcription
    - s1i: rate constant of translation
    - d0i: rate constant of mRNA degradation
    - d1i: rate constant of protein degradation.
    It holds koffi >> koni and koffi >> d0i, so that mRNA is transcribed in "burst", and d0i >> d1i, namely
    mRNA degrades faster than proteins.
    */



    // LIST OF ALL REACTIONS

    /*
    For each reaction ri, we have two arrays:
    - ri_input: position j is 1 if the variable #j is a reactant of the reaction
    - ro_output: position j is 1 if the variable #j is a product of the reaction
    */


    /*
    reaction r1 models the activation of gene 1: G1 is the reactant, AG1 is the product.
    */
    public static final int[] r1_input = {1,0,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r1_output = {0,1,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r2 models the deactivation of gene 1: AG1 is the reactant, G1 is the product.
    */
    public static final int[] r2_input =  {0,1,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r2_output = {1,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r3 models the transcription of gene 1: AG1 is the reactant, AG1 and X1 are the products.
    */
    public static final int[] r3_input =  {0,1,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r3_output = {0,1,1,0,0,0,0,0,0,0,0,0};

    /*
    reaction r4 models the translation of gene 1: X1 is the reactant, X1 and Z1 are the products.
    */
    public static final int[] r4_input =  {0,0,1,0,0,0,0,0,0,0,0,0};
    public static final int[] r4_output = {0,0,1,1,0,0,0,0,0,0,0,0};


    /*
    reaction r5 models mRNA degradation for gene1: X1 is the reactant, there is no product.
    */
    public static final int[] r5_input =  {0,0,1,0,0,0,0,0,0,0,0,0};
    public static final int[] r5_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r6 models protein degradation for gene1: Z1 is the reactant, there is no product.
    */
    public static final int[] r6_input =  {0,0,0,1,0,0,0,0,0,0,0,0};
    public static final int[] r6_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r7 models the activation of gene 2: G2 is the reactant, AG2 is the product.
    */
    public static final int[] r7_input =  {0,0,0,0,1,0,0,0,0,0,0,0};
    public static final int[] r7_output = {0,0,0,0,0,1,0,0,0,0,0,0};

    /*
    reaction r8 models the deactivation of gene 2: AG2 is the reactant, G2 is the product.
    */
    public static final int[] r8_input =  {0,0,0,0,0,1,0,0,0,0,0,0};
    public static final int[] r8_output = {0,0,0,0,1,0,0,0,0,0,0,0};

    /*
    reaction r9 models the transcription of gene 2: AG2 is the reactant, AG2 and X2 are the products.
    */
    public static final int[] r9_input =  {0,0,0,0,0,1,0,0,0,0,0,0};
    public static final int[] r9_output = {0,0,0,0,0,1,1,0,0,0,0,0};

    /*
    reaction r10 models the translation of gene 2: X2 is the reactant, X2 and Z2 are the products.
    */
    public static final int[] r10_input =  {0,0,0,0,0,0,1,0,0,0,0,0};
    public static final int[] r10_output = {0,0,0,0,0,0,1,1,0,0,0,0};

    /*
    reaction r11 models mRNA degradation for gene2: X2 is the reactant, there is no product.
    */
    public static final int[] r11_input =  {0,0,0,0,0,0,1,0,0,0,0,0};
    public static final int[] r11_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r12 models protein degradation for gene2: Z2 is the reactant, there is no product.
    */
    public static final int[] r12_input =  {0,0,0,0,0,0,0,1,0,0,0,0};
    public static final int[] r12_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r13 models the activation of gene 3: G3 is the reactant, AG3 is the product.
    */
    public static final int[] r13_input =  {0,0,0,0,0,0,0,0,1,0,0,0};
    public static final int[] r13_output = {0,0,0,0,0,0,0,0,0,1,0,0};

    /*
    reaction r14 models the deactivation of gene 3: AG3 is the reactant, G3 is the product.
    */
    public static final int[] r14_input =  {0,0,0,0,0,0,0,0,0,1,0,0};
    public static final int[] r14_output = {0,0,0,0,0,0,0,0,1,0,0,0};

    /*
    reaction r15 models the transcription of gene 3: AG3 is the reactant, AG3 and X3 are the products.
    */
    public static final int[] r15_input =  {0,0,0,0,0,0,0,0,0,1,0,0};
    public static final int[] r15_output = {0,0,0,0,0,0,0,0,0,1,1,0};

    /*
    reaction r16 models the translation of gene 3: X3 is the reactant, X3 and Z3 are the products.
    */
    public static final int[] r16_input =  {0,0,0,0,0,0,0,0,0,0,1,0};
    public static final int[] r16_output = {0,0,0,0,0,0,0,0,0,0,1,1};

    /*
    reaction r17 models mRNA degradation for gene3: X3 is the reactant, there is no product.
    */
    public static final int[] r17_input =  {0,0,0,0,0,0,0,0,0,0,1,0};
    public static final int[] r17_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    reaction r18 models protein degradation for gene3: Z3 is the reactant, there is no product.
    */
    public static final int[] r18_input =  {0,0,0,0,0,0,0,0,0,0,0,1};
    public static final int[] r18_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    public static final int[][] r_input = {r1_input,r2_input,r3_input,r4_input,r5_input,r6_input,r7_input,r8_input,r9_input,r10_input,r11_input,r12_input,r13_input,r14_input,r15_input,r16_input,r17_input,r18_input};



    // LIST OF SPECIES
    public static final int G1 = 0; // G1 is 1 if the promoter of gene 1 is inactive, o.w. G1 is 0
    public static final int AG1 = 1; // AG1 is 1 if the promoter of gene 1 is active, o.w. G1 is 1. Then, G1+AG1 is always 1.
    public static final int X1 = 2; // amount of mRNA molecules for gene 1.
    public static final int Z1 = 3; // amount of proteins produced by gene 1.

    public static final int G2 = 4; // G2 is 1 if the promoter of gene 2 is inactive, o.w. G2 is 0
    public static final int AG2 = 5; // AG2 is 1 if the promoter of gene 2 is active, o.w. G2 is 1. Then, G2+AG2 is always 1.
    public static final int X2 = 6; // amount of mRNA molecules for gene 2.
    public static final int Z2 = 7; // amount of proteins produced by gene 2.

    public static final int G3 = 8; // G3 is 1 if the promoter of gene 3 is inactive, o.w. G3 is 0
    public static final int AG3 = 9; // AG3 is 1 if the promoter of gene 3 is active, o.w. G3 is 1. Then, G3+AG3 is always 1.
    public static final int X3 = 10; // amount of mRNA molecules for gene 3.
    public static final int Z3 = 11; // amount of proteins produced by gene 3.

    public static final int kon1 = 12; // rate constant of gene 1 activation
    public static final int koff1 = 13; // rate constant of gene 1 deactivation
    public static final int s01 = 14; // rate constant of gene 1 transcription
    public static final int s11 = 15; // rate constant of gene 1 translation
    public static final int d01 = 16; // rate constant of mRNA produced by gene 1 degradation
    public static final int d11 = 17; // rate constant of protein produced by gene 1 degradation

    public static final int kon2 = 18; // rate constant of gene 2 activation
    public static final int koff2 = 19; // rate constant of gene 2 deactivation
    public static final int s02 = 20; // rate constant of gene 2 transcription
    public static final int s12 = 21; // rate constant of gene 2 translation
    public static final int d02 = 22; // rate constant of mRNA produced by gene 2 degradation
    public static final int d12 = 23; // rate constant of protein produced by gene 2 degradation

    public static final int kon3 = 24; // rate constant of gene 3 activation
    public static final int koff3 = 25; // rate constant of gene 3 deactivation
    public static final int s03 = 26; // rate constant of gene 3 transcription
    public static final int s13 = 27; // rate constant of gene 3 translation
    public static final int d03 = 28; // rate constant of mRNA produced by gene 3 degradation
    public static final int d13 = 29; // rate constant of protein produced by gene 3 degradation


    private static final int NUMBER_OF_VARIABLES = 30;

    public static final double THRESHOLD = 0.19;

    //public static final double K01 = 0.34;
    //public static double K11 = 2.15;
    //public static double BETA1 = 5;
    //public static double THETA1 = - 10;
    //public static final double K02 = 0.34;
    //public static double K12 = 2.15;
    //public static double BETA2 = 5;
    //public static double THETA2 = - 10;
    //public static final double K03 = 0.34;
    //public static double K13 = 2.15;
    //public static double BETA3 = 5;
    //public static double THETA3 = - 10;

    public static final double K01 = 0.0;
    public static double K11 = 2.0;
    public static double BETA1 = 5;
    public static double THETA1 = - 10;
    public static final double K02 = 0.0;
    public static double K12 = 2.0;
    public static double BETA2 = 5;
    public static double THETA2 = - 10;
    public static final double K03 = 0.0;
    public static double K13 = 2.0;
    public static double BETA3 = 5;
    public static double THETA3 = - 10;




    // MAIN PROGRAM
    public static void main(String[] args) throws IOException {
        try {

            RandomGenerator rand = new DefaultRandomGenerator();

            /*
            Variable <code>size</code> gives the number of runs that are used to obtain an evolution sequence.
            More in detail, an evolution sequence, modelled by class <code>EvolutionSequence</code>, is a sequence of
            sample sets of system configurations, where configurations are modelled by class <code>TimedSystem</code> and
            sample sets by class <code>SampleSet</code>.
            In this context, <code>size</code> is the cardinality of those sample sets.
            */
            int size = 100;

            /*
            One of the elements of a system configuration is the "controller", i.e. an instance of <code>Controller</code>.
            In this example we do not need controllers, therefore we use a controller that does nothing, i.e. an instance
            of <code>NilController</code>.
             */
            Controller controller = new NilController();

            /*
            Another element of a system configuration is the "data state", i.e. an instance of <code>DataState</code>,
            which models the state of the data.
            The initial state <code>state</code> is constructed by method getInitialState, which assigns the initial
            value to all 30 variables. Moreover, time granularity is set to 1.0,
             */
            DataState state = getInitialState(rand,1.0,0.0,0.0,0.0);

            /*
            <code>system</code> will be the starting configuration from which the evolution sequence will be constructed.
            This configuration consists of:
            - the controller <code>controller</code> defined above,
            - the data state <code>state</state> defined above,
            - a random function over data states, which implements interface <code>DataStateFunction</code> and maps a
            random generator <code>rg</code> and a data state <code>ds>/code> to the data state obtained by updating
            <code>ds</code> with the list of changes given by method <code><selectAndApplyReaction>/code>. Essentially,
            this method selects the reaction according to Gillespie algorithm.
            - an expression over data states, which implements interface <code>DataStateExpression</code> and maps a
            data state <code>ds>/code> to the time of next reaction.
             */
            TimedSystem system = new TimedSystem(controller, (rg, ds) -> ds.apply(selectAndApplyReaction(rg, ds)), state, ds->selectReactionTime(rand,ds));

            /*
            The evolution sequence <code>sequence></code> will consists in a sequence of sample sets of configurations
            of cardinality <code>size</size>, where the first sample of the list consists in <code>size</size> copies of
            configuration <code>system</code>.
             */
            EvolutionSequence sequence = new EvolutionSequence(rand, rg -> system, size);

            ArrayList<DataStateExpression> F = new ArrayList<>();
            F.add(ds->ds.get(G1));
            F.add(ds->ds.get(AG1));
            F.add(ds->ds.get(X1));
            F.add(ds->ds.get(Z1));
            F.add(ds->ds.get(G2));
            F.add(ds->ds.get(AG2));
            F.add(ds->ds.get(X2));
            F.add(ds->ds.get(Z2));
            F.add(ds->ds.get(G3));
            F.add(ds->ds.get(AG3));
            F.add(ds->ds.get(X3));
            F.add(ds->ds.get(Z3));
            F.add(ds->ds.getTimeDelta());
            F.add(ds->ds.getTimeReal());
            ArrayList<String> L = new ArrayList<>();

            double[][] minMax = printLMinMaxData(rand, L, F, system, 2000, size, 0, 2000);
            double[][] minMax_p = printPerturbed(rand, L, F, system, 2000, size, 0, 2000,p_rate());

            double[][] plot_z1 = new double[1000][1];
            double[][] plot_z2 = new double[1000][1];
            double[][] plot_z3 = new double[1000][1];
            double[][] data = SystemState.sample(rand, F, system, 1000, size);
            for (int i = 0; i<1000; i++){
                plot_z1[i][0] = data[i][3];
                plot_z2[i][0] = data[i][7];
                plot_z3[i][0] = data[i][11];
            }

            Util.writeToCSV("./new_plotZ1.csv",plot_z1);
            Util.writeToCSV("./new_plotZ2.csv",plot_z2);
            Util.writeToCSV("./new_plotZ3.csv",plot_z3);

            double normalisation = Math.max(minMax[1][Z1],minMax_p[1][Z1])*1.1;

            /*
            The evolution sequence <code>sequence_p</code> is obtained from the evolution sequence <code>sequence</code>
            by applying a perturbation, where:
            - the perturbation is returned by method <code>p_rate()</code> defined below
            - the perturbation is applied at step 0
            - the sample sets of configurations in <code>sequence_p</code> have a cardinality which corresponds to that
            of <code>sequence</code> multiplied by 10
            */
            EvolutionSequence sequence_p = sequence.apply(p_rate(),0,10);

            DistanceExpression phases = new MaxIntervalDistanceExpression(
                    new MinIntervalDistanceExpression(
                            new AtomicDistanceExpression(ds->ds.get(Z1)/normalisation,(v1, v2) -> Math.abs(v2-v1)),
                            0,
                            1
                    ),
                    300,
                    1000
            );

            System.out.println(phases.compute(0, sequence, sequence_p));


        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static double[][] printLMinMaxData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound){
        double[][] result = new double[2][NUMBER_OF_VARIABLES];
        System.out.println(label);
        double[][] data_av = SystemState.sample(rg, F, s, steps, size);
        double[][] data_max = SystemState.sample_max(rg, F, s, steps, size);
        double[] max = new double[NUMBER_OF_VARIABLES];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < data_av.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data_av[i].length -1 ; j++) {
                System.out.printf("%f   ", data_av[i][j]);
                if (j<NUMBER_OF_VARIABLES & leftbound <= i & i <= rightbound) {
                    if (max[j] < data_max[i][j]) {
                        max[j] = data_max[i][j];
                        result[1][j]=data_max[i][j];
                    }
                }
            }
            System.out.printf("%f\n", data_av[i][data_av[i].length -1]);
        }
        System.out.printf("%s   ", "max:");
        for(int j=0; j<NUMBER_OF_VARIABLES-1; j++){
            System.out.printf("%f   ", max[j]);
        }
        System.out.printf("%f\n", max[NUMBER_OF_VARIABLES-1]);
        return result;
    }

    private static double[][] printPerturbed(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound, Perturbation perturbation){
        double[][] result = new double[2][NUMBER_OF_VARIABLES];
        System.out.println(label);
        double[][] data_av = SystemState.sample(rg, F, perturbation, s, steps, size);
        double[][] data_max = SystemState.sample_max(rg, F, perturbation, s, steps, size);
        double[] max = new double[NUMBER_OF_VARIABLES];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < data_av.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data_av[i].length -1 ; j++) {
                System.out.printf("%f   ", data_av[i][j]);
                if (j<NUMBER_OF_VARIABLES & leftbound <= i & i <= rightbound) {
                    if (max[j] < data_max[i][j]) {
                        max[j] = data_max[i][j];
                        result[1][j]=data_max[i][j];
                    }
                }
            }
            System.out.printf("%f\n", data_av[i][data_av[i].length -1]);
        }
        System.out.printf("%s   ", "max:");
        for(int j=0; j<NUMBER_OF_VARIABLES-1; j++){
            System.out.printf("%f   ", max[j]);
        }
        System.out.printf("%f\n", max[NUMBER_OF_VARIABLES-1]);
        return result;
    }

    /*
    The following method returns a perturbation. In particular this is an atomic perturbation, i.e. an instance of
    <code>AtomicPerturbation</code>. It consists of two elements:
    - a random function over data states, i.e. an instance of <code>DataStateFunction</code>, which is returned by
    method <code>p_rate()>/code>.
    - the number of steps after which the random function will be applied, which is 0 in this case.
     */
    public static Perturbation p_rate(){
        return new AtomicPerturbation(0,Repressilator::slow);
    }

    /*
    Method <code>slow>/code> perturbs a data state by modifying the rate constant of degratation of protein constructed
    from gene 1
     */
    private static DataState slow(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)-3,0)));
        //updates.add(new DataStateUpdate(s12,Math.max(state.get(s12)-1,0)));
        //updates.add(new DataStateUpdate(s13,Math.max(state.get(s13)-1,0)));
        return state.apply(updates);
    }


    /*
    The following method selects the time of next reaction according to Gillespie algorithm.
     */
    public static double selectReactionTime(RandomGenerator rg, DataState state){
        double rate = 0.0;
        double[] lambda = new double[18];
        for (int j=0; j<18; j++){
            double weight = 1.0;
            for (int i=0; i<12; i++){
                if(r_input[j][i] > 0) {
                    weight = weight * Math.pow(state.get(i), r_input[j][i]);
                }
            }
            lambda[j] = state.get(j+12) * weight;
            rate = rate + lambda[j];
        }

        double rand = rg.nextDouble();
        return (1/rate)*Math.log(1/rand);
    }


    /*
    The following method selects the next reaction, according to Gillespie's algorithm, and returns the updates that
    allow for modifying the data state accordingly: these updates will remove the reactants used by the selected reaction
    from the data state, will add the products, and will tune the rate constant of promoters' activation according to the
    new value of proteins.
    */

    public static List<DataStateUpdate> selectAndApplyReaction(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();

        double[] lambda = new double[18];
        double[] lambdaParSum = new double[18];
        double lambdaSum = 0.0;

        for (int j=0; j<18; j++){
            double weight = 1.0;
            for (int i=0; i<12; i++){
                weight = weight * Math.pow(state.get(i),r_input[j][i]);
            }
            lambda[j] = state.get(j+12) * weight;
            lambdaSum = lambda[j]+lambdaSum;
            lambdaParSum[j] = lambdaSum;
        }

        if(lambdaSum > 0){

            double token = 1 - rg.nextDouble();

            int selReaction = 0;

            while (lambdaParSum[selReaction] < token * lambdaSum) {
                selReaction++;
            }

            selReaction++;

            switch(selReaction){
                case 1:
                    for (int i=0; i<12; i++) {
                        double newArity = state.get(i) + r1_output[i] - r1_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 2:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r2_output[i] - r2_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 3:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r3_output[i] - r3_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 4:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r4_output[i] - r4_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 5:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r5_output[i] - r5_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 6:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r6_output[i] - r6_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 7:
                    for (int i=0; i<12; i++) {
                        double newArity = state.get(i) + r7_output[i] - r7_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 8:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r8_output[i] - r8_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 9:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r9_output[i] - r9_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 10:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r10_output[i] - r10_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 11:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r11_output[i] - r11_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 12:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r12_output[i] - r12_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 13:
                    for (int i=0; i<12; i++) {
                        double newArity = state.get(i) + r13_output[i] - r13_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 14:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r14_output[i] - r14_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 15:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r15_output[i] - r15_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 16:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r16_output[i] - r16_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 17:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r17_output[i] - r17_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
                case 18:
                    for (int i = 0; i < 12; i++) {
                        double newArity = state.get(i) + r18_output[i] - r18_input[i];
                        updates.add(new DataStateUpdate(i, newArity));
                    }
                    break;
            }
        } else {
            System.out.println("Missing reagents");
        }

        double new_kon1 = (K01 + K11 * Math.exp(BETA1 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)))/(1+Math.exp(BETA1 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)));
        updates.add(new DataStateUpdate(kon1,new_kon1));
        double new_kon2 = (K02 + K12 * Math.exp(BETA2 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)))/(1+Math.exp(BETA2 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)));
        updates.add(new DataStateUpdate(kon2,new_kon2));
        double new_kon3 = (K03 + K13 * Math.exp(BETA3 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)))/(1+Math.exp(BETA3 + THETA1*state.get(Z1) + THETA2*state.get(Z2) + THETA3*state.get(Z3)));
        updates.add(new DataStateUpdate(kon3,new_kon3));

        return updates;

    }

    /*
    Method getInitialState assigns the initial value to all variables
    */
    public static DataState getInitialState(RandomGenerator rand, double gran, double Tstep, double Treal, double Tdelta) {
        Map<Integer, Double> values = new HashMap<>();

        // the system starts with all promoters inactive, no mRNA molecule and no protein

        values.put(G1, 1.0);
        values.put(AG1, 0.0);
        values.put(X1, 0.0);
        values.put(Z1, 0.0);

        values.put(G2, 1.0);
        values.put(AG2, 0.0);
        values.put(X2, 0.0);
        values.put(Z2, 0.0);

        values.put(G3, 1.0);
        values.put(AG3, 0.0);
        values.put(X3, 0.0);
        values.put(Z3, 0.0);

        double initial_kon1 = K01 + K11 * Math.exp(BETA1)/(1+Math.exp(BETA1));

        //values.put(kon1, initial_kon1);
        //values.put(koff1, 10.0);
        //values.put(s01, 1000.0);
        //values.put(s11, 10.0);
        //values.put(d01, 0.5);
        //values.put(d11, 0.1);

        values.put(kon1, initial_kon1);
        values.put(koff1, 5.0);
        values.put(s01, 250.0);
        values.put(s11, 7.0);
        values.put(d01, 1.0);
        values.put(d11, 1.0);


        double initial_kon2 = K02 + K12 * Math.exp(BETA2)/(1+Math.exp(BETA2));

        //values.put(kon2, initial_kon2);
        //values.put(koff2, 10.0);
        //values.put(s02, 1000.0);
        //values.put(s12, 10.0);
        //values.put(d02, 0.5);
        //values.put(d12, 0.1);

        values.put(kon2, initial_kon2);
        values.put(koff2, 5.0);
        values.put(s02, 250.0);
        values.put(s12, 7.0);
        values.put(d02, 1.0);
        values.put(d12, 1.0);

        double initial_kon3 = K03 + K13 * Math.exp(BETA3)/(1+Math.exp(BETA3));

        //values.put(kon3, initial_kon3);
        //values.put(koff3, 10.0);
        //values.put(s03, 1000.0);
        //values.put(s13, 10.0);
        //values.put(d03, 0.5);
        //values.put(d13, 0.1);

        values.put(kon3, initial_kon3);
        values.put(koff3, 5.0);
        values.put(s03, 250.0);
        values.put(s13, 7.0);
        values.put(d03, 1.0);
        values.put(d13, 1.0);


        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN), gran, Tstep, Treal, Tdelta);
    }

}
