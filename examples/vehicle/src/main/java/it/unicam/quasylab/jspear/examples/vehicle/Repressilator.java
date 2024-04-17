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
import it.unicam.quasylab.jspear.distance.*;
import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.ds.DataStateUpdate;
import it.unicam.quasylab.jspear.ds.RelationOperator;
import it.unicam.quasylab.jspear.perturbation.AtomicPerturbation;
import it.unicam.quasylab.jspear.perturbation.IterativePerturbation;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.perturbation.SequentialPerturbation;
import it.unicam.quasylab.jspear.robtl.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.*;

public class Repressilator {

    /*

    THE TWO STATE MODEL OF THE REPRESSILATOR

    The "repressilator" network consists in 3 genes forming a directed cycle of "negative interactions".
    As in "Herbach et al: ''Inferring gene regulatory networks from single-cell data: a mechanistic approach'',
    BMC Systems Biology (2017) 11:105", we adopt the "two state model" of gene expression, where the gene promoter
    can be either active or inactive, and we consider both mRNA molecules, which can be transcribed only during the
    active period, and proteins, which are produced by mRNA molecules at a constant rate.
    For i=1,2,3 we have the following variables that will allow us to model the status of such a kind of system:
    - Gi: models the inactive promoter, Gi is 1 if the promoter is inactive, otherwise Gi is 0.
    - AGi: models the active promoter, AGi is 1 if the promoter is active, otherwise AGi is 0.
      Clearly, it always holds that: Gi + AGi = 1.
    - Xi: amount of mRNA molecules.
    - Zi: amount of proteins.
    - koni: rate constant of gene i activation
    - koffi: rate constant of gene i deactivation
    - s0i: rate constant of transcription
    - s1i: rate constant of translation
    - d0i: rate constant of mRNA degradation
    - d1i: rate constant of protein degradation.
    It holds that koffi >> koni and koffi >> d0i, so that mRNA is transcribed in "burst".

    */



    /*

    SPECIFYING THE TWO STATE MODEL OF THE REPRESSILATOR WITH CHEMICAL REACTIONS

    As in "Herbach et al: ''Inferring gene regulatory networks from single-cell data: a mechanistic approach'',
    BMC Systems Biology (2017) 11:105", we use chemical reactions for specifying the repressilator in the two state
    model approach.
    In particular, reactions model the following dynamics:
    - activation of the promoter, modelled by reaction Gi -- koni --> AGi
    - deactivation of the promoter, modelled by reaction AGi -- koffi --> Gi,
    - transcritpion, modelled by reaction AGi -- s0i --> Xi + AGi,
    - translation, modelled by reaction Xi -- s1i --> Xi + Zi,
    - mRNA degradation, modelled by reaction Xi -- d0i --> empty set,
    - protein degradation, modelled by reaction Zi -- d1i --> empty set.
    Therefore, for each gene we have 6 reactions.
    Clearly, Xi and Zi are not conserved quantities, whereas Gi and AGi satisfy the conservation relation Gi + AGi = 1.
    The value of Z1, Z2, Z3 will impact on kon1, kon2, kon3, thus realising gene interaction.

    */


    /*

    ARRAYS REPRESENTING REACTANTS/PRODUCTS OF REACTIONS

    Technically, for each of the 18 reactions, we define two arrays.
    Each of these arrays has 12 positions, which are associated to active promoters, inactive promoters and molecules.
    In particular, the 12 positions are for: G1, AG1, X1, Z1, G2, AG2, X2, Z2, G3, AG3, X3, Z3.
    Then, the two arrays for each reaction ri, with i=1,..,18, are:
    - ri_input: position j is 1 if the variable corresponding to position j is a reactant of the reaction
    - ro_output: position j is 1 if the variable corresponding to j is a product of the reaction
    The arrays are defined below.

    */


    /*
    Reaction r1 models the activation of gene 1: G1 is the reactant, AG1 is the product.
    */
    public static final int[] r1_input = {1,0,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r1_output = {0,1,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r2 models the deactivation of gene 1: AG1 is the reactant, G1 is the product.
    */
    public static final int[] r2_input =  {0,1,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r2_output = {1,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r3 models the transcription of gene 1: AG1 is the reactant, AG1 and X1 are the products.
    */
    public static final int[] r3_input =  {0,1,0,0,0,0,0,0,0,0,0,0};
    public static final int[] r3_output = {0,1,1,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r4 models the translation of gene 1: X1 is the reactant, X1 and Z1 are the products.
    */
    public static final int[] r4_input =  {0,0,1,0,0,0,0,0,0,0,0,0};
    public static final int[] r4_output = {0,0,1,1,0,0,0,0,0,0,0,0};


    /*
    Reaction r5 models mRNA degradation for gene1: X1 is the reactant, there is no product.
    */
    public static final int[] r5_input =  {0,0,1,0,0,0,0,0,0,0,0,0};
    public static final int[] r5_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r6 models protein degradation for gene1: Z1 is the reactant, there is no product.
    */
    public static final int[] r6_input =  {0,0,0,1,0,0,0,0,0,0,0,0};
    public static final int[] r6_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r7 models the activation of gene 2: G2 is the reactant, AG2 is the product.
    */
    public static final int[] r7_input =  {0,0,0,0,1,0,0,0,0,0,0,0};
    public static final int[] r7_output = {0,0,0,0,0,1,0,0,0,0,0,0};

    /*
    Reaction r8 models the deactivation of gene 2: AG2 is the reactant, G2 is the product.
    */
    public static final int[] r8_input =  {0,0,0,0,0,1,0,0,0,0,0,0};
    public static final int[] r8_output = {0,0,0,0,1,0,0,0,0,0,0,0};

    /*
    Reaction r9 models the transcription of gene 2: AG2 is the reactant, AG2 and X2 are the products.
    */
    public static final int[] r9_input =  {0,0,0,0,0,1,0,0,0,0,0,0};
    public static final int[] r9_output = {0,0,0,0,0,1,1,0,0,0,0,0};

    /*
    Reaction r10 models the translation of gene 2: X2 is the reactant, X2 and Z2 are the products.
    */
    public static final int[] r10_input =  {0,0,0,0,0,0,1,0,0,0,0,0};
    public static final int[] r10_output = {0,0,0,0,0,0,1,1,0,0,0,0};

    /*
    Reaction r11 models mRNA degradation for gene2: X2 is the reactant, there is no product.
    */
    public static final int[] r11_input =  {0,0,0,0,0,0,1,0,0,0,0,0};
    public static final int[] r11_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r12 models protein degradation for gene2: Z2 is the reactant, there is no product.
    */
    public static final int[] r12_input =  {0,0,0,0,0,0,0,1,0,0,0,0};
    public static final int[] r12_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r13 models the activation of gene 3: G3 is the reactant, AG3 is the product.
    */
    public static final int[] r13_input =  {0,0,0,0,0,0,0,0,1,0,0,0};
    public static final int[] r13_output = {0,0,0,0,0,0,0,0,0,1,0,0};

    /*
    Reaction r14 models the deactivation of gene 3: AG3 is the reactant, G3 is the product.
    */
    public static final int[] r14_input =  {0,0,0,0,0,0,0,0,0,1,0,0};
    public static final int[] r14_output = {0,0,0,0,0,0,0,0,1,0,0,0};

    /*
    Reaction r15 models the transcription of gene 3: AG3 is the reactant, AG3 and X3 are the products.
    */
    public static final int[] r15_input =  {0,0,0,0,0,0,0,0,0,1,0,0};
    public static final int[] r15_output = {0,0,0,0,0,0,0,0,0,1,1,0};

    /*
    Reaction r16 models the translation of gene 3: X3 is the reactant, X3 and Z3 are the products.
    */
    public static final int[] r16_input =  {0,0,0,0,0,0,0,0,0,0,1,0};
    public static final int[] r16_output = {0,0,0,0,0,0,0,0,0,0,1,1};

    /*
    Reaction r17 models mRNA degradation for gene3: X3 is the reactant, there is no product.
    */
    public static final int[] r17_input =  {0,0,0,0,0,0,0,0,0,0,1,0};
    public static final int[] r17_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    Reaction r18 models protein degradation for gene3: Z3 is the reactant, there is no product.
    */
    public static final int[] r18_input =  {0,0,0,0,0,0,0,0,0,0,0,1};
    public static final int[] r18_output = {0,0,0,0,0,0,0,0,0,0,0,0};

    public static final int[][] r_input = {r1_input,r2_input,r3_input,r4_input,r5_input,r6_input,r7_input,r8_input,r9_input,r10_input,r11_input,r12_input,r13_input,r14_input,r15_input,r16_input,r17_input,r18_input};



    /*

    VARIABLES MODELING THE STATUS OF THE SYSTEM

    Below a list of 30 variables, the idea being that the value of these 30 variables gives a data state, namely an
    instance of class <code>DataState</code> representing the status of all quantities of the system.
    We have variables for active and inactive promoters, mRNA, proteins and reaction rates. Reaction rates can vary
    since promoter parameters, i.e. the rates of promoter activation and deactivation, depend on the amount of proteins.
    Each variable is associated with an index, from 0 to 29.
    */
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

    public static final int kon1 = 12; // rate constant of gene 1 activation, also called "burst frequency" for gene 1
    public static final int koff1 = 13; // rate constant of gene 1 deactivation
    public static final int s01 = 14; // rate constant of gene 1 transcription
    public static final int s11 = 15; // rate constant of gene 1 translation
    public static final int d01 = 16; // rate constant of mRNA produced by gene 1 degradation
    public static final int d11 = 17; // rate constant of protein produced by gene 1 degradation

    public static final int kon2 = 18; // rate constant of gene 2 activation, also called "burst frequency" for gene 2
    public static final int koff2 = 19; // rate constant of gene 2 deactivation
    public static final int s02 = 20; // rate constant of gene 2 transcription
    public static final int s12 = 21; // rate constant of gene 2 translation
    public static final int d02 = 22; // rate constant of mRNA produced by gene 2 degradation
    public static final int d12 = 23; // rate constant of protein produced by gene 2 degradation

    public static final int kon3 = 24; // rate constant of gene 3 activation, also called "burst frequency" for gene 3
    public static final int koff3 = 25; // rate constant of gene 3 deactivation
    public static final int s03 = 26; // rate constant of gene 3 transcription
    public static final int s13 = 27; // rate constant of gene 3 translation
    public static final int d03 = 28; // rate constant of mRNA produced by gene 3 degradation
    public static final int d13 = 29; // rate constant of protein produced by gene 3 degradation

    private static final int NUMBER_OF_VARIABLES = 30;

    // RIMUOVERE PRIMA DI INVIARE IL PAPER:

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

    /*

    CONSTANTS FOR COMPUTING THE BURST FREQUENCY

    The following constants will be used to derive the burst frequency for gene 1, gene 2 and gene 3 from
    the amount of proteins Z1, Z2, Z3.

    The values are taken from
    "Ulysse Herbach: ''Harissa: Stochastic Simulation and Inference of Gene Regulatory Networks Based on Transcriptional
    Bursting''. Proc. CMSB 2023".
     */
    public static final double K01 = 0.0; // minimal burst frequency
    public static double K11 = 2.0; // maximal burst frequency
    public static double BETA1 = 5; // basal activity gene 1

    public static final double K02 = 0.0; // minimal burst frequency
    public static double K12 = 2.0; // maximal  burst frequency
    public static double BETA2 = 5; // basal activity gene 2

    public static final double K03 = 0.0; // minimal burst frequency
    public static double K13 = 2.0; // maximal burst frequency
    public static double BETA3 = 5; // basal activity gene 3

    public static double THETA11 = 0; // interaction gene 1 --> gene 1
    public static double THETA21 = 0; // interaction gene 2 --> gene 1
    public static double THETA31 = -10; // interaction gene 3 --> gene 1
    public static double THETA12 = -10; // interaction gene 1 --> gene 2
    public static double THETA22 = 0; // interaction gene 2 --> gene 2
    public static double THETA32 = 0; // interaction gene 3 --> gene 2
    public static double THETA13 = 0; // interaction gene 1 --> gene 3
    public static double THETA23 = -10; // interaction gene 2 --> gene 3
    public static double THETA33 = 0; // interaction gene 3 --> gene 3



    // MAIN PROGRAM
    public static void main(String[] args) throws IOException {
        try {


            /*

            INITIAL CONFIGURATION

            In order to perform simulations/analysis/model checking for a particular system, we need to create its
            initial configuration, which is an instance of <code>TimedSystem>/code>

            */


            /*
            One of the elements of a system configuration is the "controller", i.e. an instance of <code>Controller</code>.
            In this example we do not need controllers, therefore we use a controller that does nothing, i.e. an instance
            of <code>NilController</code>.
            In other case studies, controllers may be used to control the activity of a system.
            For instance, a scheduling of a therapy may be modelled by a controller.
            */
            Controller controller = new NilController();

            /*
            Another element of a system configuration is the "data state", i.e. an instance of <code>DataState</code>,
            which models the state of the data.
            Instances of <code>DataState</code> contains values for variables representing the quantities of the
            system and four values allowing us to model the evolution of time: gran, Tstep, Treal, Tdelta.
            The initial state <code>state</code> is constructed by exploiting the static method
            <code>getInitialState</code>, which will be defined later and assigns the initial value to all 30
            variables defined above.
             */
            DataState state = getInitialState(1.0,0.0,0.0,0.0);

            /*
            We define the <code>TimedSystem</code> <code>system</code>, which will be the starting configuration from
            which the evolution sequence will be constructed.
            This configuration consists of 4 elements:
            - the controller <code>controller</code> defined above,
            - the data state <code>state</state> defined above,
            - a random function over data states, which implements interface <code>DataStateFunction</code> and maps a
            random generator <code>rg</code> and a data state <code>ds</code> to the data state obtained by updating
            <code>ds</code> with the list of changes given by method <code>selectAndApplyReaction/code>. Essentially,
            this static method, defined later, selects the next reaction among the 18 available according to Gillespie
            algorithm and realises the changes on variables that are consequence of the firing of the selected reaction,
            i.e. reactants are removed from <code>ds</code> and products are added to <code>ds</code>. Moreover, since
            proteins are among reactants/products and their amount impact on burst frequencies, also those will be
            updated.
            - an expression over data states, which implements interface <code>DataStateExpression</code> and maps a
            data state <code>ds</code> to the time of next reaction.
             */
            RandomGenerator rand = new DefaultRandomGenerator();
            TimedSystem system = new TimedSystem(controller, (rg, ds) -> ds.apply(selectAndApplyReaction(rg, ds)), state, ds->selectReactionTime(rand,ds));


            /*

            EVOLUTION SEQUENCES

            Having the initial configuration <code>system</code>, we can generate its behaviour, which means that
            we can generate an evolution sequence.

             */

            /*
            Variable <code>size</code> gives the number of runs that are used to obtain the evolution sequence.
            More in detail, an evolution sequence, modelled by class <code>EvolutionSequence</code>, is a sequence of
            sample sets of system configurations, where configurations are modelled by class <code>TimedSystem</code>
            and sample sets by class <code>SampleSet</code>.
            In this context, <code>size</code> is the cardinality of those sample sets.
            */
            int size = 100;

            /*
            The evolution sequence <code>sequence></code> created by the following instruction consists in a sequence of
            sample sets of configurations of cardinality <code>size</size>, where the first sample of the list consists
            in <code>size</size> copies of configuration <code>system</code> defined above.
            Notice that <code>sequence></code> contains initially only this first sample, the subsequent ones will be
            created "on demand".
             */
            EvolutionSequence sequence = new EvolutionSequence(rand, rg -> system, size);


            /*
            Each expression in the following list <code>F</code> allows us to read the value of a given variable
            from a data state
             */
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

            ArrayList<String> L = new ArrayList<>();
            L.add("G1      ");
            L.add("AG1     ");
            L.add("X1      ");
            L.add("Z1      ");
            L.add("G2      ");
            L.add("AG2     ");
            L.add("X2      ");
            L.add("Z2      ");
            L.add("G3      ");
            L.add("AG3     ");
            L.add("X3      ");
            L.add("Z3      ");



            /*

            EXPERIMENTS

            We propose three experiments showing how our tool can be used.


            */




            /*

            EXPERIMENT #1

            In this experiment we generate two evolution sequences from configuration <code>system</system>.
            Both evolution sequences are sequences of length N of sample sets of cardinality <code>size</code> of
            configurations, with the first sample set consisting in <code>size</code> copies of <code>system</code>.
            The second evolution sequence is perturbed by applying the perturbation returned by the static method
            <code>pert_transl_1(x)</code> defined later. Essentially, such a perturbation will change the rate of the
            translation of protein obtained from the first gene by adding x. Since promoter parameters depend on the
            amount of proteins, this perturbation impact on the dynamics of the whole system. This perturbation
            models protein translation deregulation.

            For both evolution sequences, we print some information allowing us to observe the dynamics of both the
            nominal and the perturbed system: for each time unit in [0,N-1] and for each variable, we print out
            the average value that the variable assumes in the <code>size<code> configurations in the sample set
            obtained at that time unit.
            After that, we print out also the average value that the variables assume in all configurations in all
            sample sets obtained in all time units.
            Clearly, if the perturbation diminishes the degradation rate for Z1, then in the perturbed sequence we
            observe higher values for Z1, lower values for Z2 and higher values for Z3.
            On the contrary, if the perturbation augments the degradation rate for Z1, in the perturbed sequence we
            observe lower values for Z1, higher values for Z2 and lower values for Z3.

            */

            System.out.println("");
            System.out.println("EXPERIMENT 1");
            System.out.println("");


            int N = 1000;    // length ot the evolution sequence

            double x = -3.0; // x positive: higher values for Z1, lower for Z2, higher for Z3
                             // x negative: lower values for Z1, higher for Z2, lower for Z3


            int w1=50;
            int w2=50;
            int replica= 6;

            System.out.println("");
            System.out.println("Simulation of nominal system - data average values:");
            System.out.println("");
            printAvgData(rand, L, F, system, N, size, 0, N);
            System.out.println("");
            System.out.println("Simulation of perturbed system - s11 incremented by " + x + " - data average values:");
            System.out.println("");
            printAvgDataPerturbed(rand, L, F, system, N, size, 0, N, itZ1TranslRate(x, w1, w2, replica));

            /*
            While in the previous three lines of code the average values of variables obtained step-by-step are
            printed out, the following portion of code stores them in .csv files. This is useful if we need to plot them.
            */

            double[][] plot_z1 = new double[N][1];
            double[][] plot_z2 = new double[N][1];
            double[][] plot_z3 = new double[N][1];

            double[][] plot_x1 = new double[N][1];
            double[][] plot_x2 = new double[N][1];
            double[][] plot_x3 = new double[N][1];

            double[][] data = SystemState.sample(rand, F, system, N, size);
            for (int i = 0; i<N; i++){
                plot_z1[i][0] = data[i][3];
                plot_z2[i][0] = data[i][7];
                plot_z3[i][0] = data[i][11];

                plot_x1[i][0] = data[i][2];
                plot_x2[i][0] = data[i][6];
                plot_x3[i][0] = data[i][10];
            }
            Util.writeToCSV("./new_plotZ1.csv",plot_z1);
            Util.writeToCSV("./new_plotZ2.csv",plot_z2);
            Util.writeToCSV("./new_plotZ3.csv",plot_z3);

            Util.writeToCSV("./new_plotX1.csv",plot_x1);
            Util.writeToCSV("./new_plotX2.csv",plot_x2);
            Util.writeToCSV("./new_plotX3.csv",plot_x3);

            double[][] plot_pz1 = new double[N][1];
            double[][] plot_pz2 = new double[N][1];
            double[][] plot_pz3 = new double[N][1];

            double[][] plot_px1 = new double[N][1];
            double[][] plot_px2 = new double[N][1];
            double[][] plot_px3 = new double[N][1];

            double[][] pdata = SystemState.sample(rand, F, itZ1TranslRate(x,w1,w2,replica), system, N, size);
            for (int i = 0; i<N; i++){
                plot_pz1[i][0] = pdata[i][3];
                plot_pz2[i][0] = pdata[i][7];
                plot_pz3[i][0] = pdata[i][11];

                plot_px1[i][0] = pdata[i][2];
                plot_px2[i][0] = pdata[i][6];
                plot_px3[i][0] = pdata[i][10];
            }
            Util.writeToCSV("./new_pplotZ1.csv",plot_pz1);
            Util.writeToCSV("./new_pplotZ2.csv",plot_pz2);
            Util.writeToCSV("./new_pplotZ3.csv",plot_pz3);

            Util.writeToCSV("./new_pplotX1.csv",plot_px1);
            Util.writeToCSV("./new_pplotX2.csv",plot_px2);
            Util.writeToCSV("./new_pplotX3.csv",plot_px3);







            /*

            EXPERIMENT #2


            ESTIMATING AND CHECKING THE BEHAVIOURAL DIFFERENCES BETWEEN NOMINAL AND PERTURBED EVOLUTION SEQUENCES


            In this experiment we generate again a nominal and a perturbed evolution sequence, as in EXPERIMENT 1.
            Then, we quantify the differences between those evolutions sequences, which corresponds to quantifying the
            behavioural distance between the nominal and the perturbed sequence. The differences are quantified with
            respect to the amount of protein Z1, Z2 or Z3.
            Then, we write down a robustness formula that simply expresses whether the maximal of these distances is
            below a given threshold.

             */


            /*
            In order to quantify the difference between two evolution sequences w.r.t. Zi, we need to define the
            difference between two configurations w.r.t. Zi: given two configurations with Zi=m and Zi=n, the
            difference between those configurations w.r.t. Zi is the value |m-n|, normalised wrt the maximal value that
            can be assumed by Zi, so that this difference is always in [0,1].
            Since we cannot know a priori which is the maximal value that can be assumed by Zi, we estimate it.

            Therefore, we start with estimating the maximal values that can be assumed by all variables.
            To this purpose, we generate a nominal and a perturbed evolution sequence of length 2N and collect the
            maximal values that are assumed by the variables in all configurations in all sample sets.
            */










            System.out.println("");
            System.out.println("EXPERIMENT 2");
            System.out.println("");





            System.out.println("");
            System.out.println("Simulation of nominal system - Data maximal values:");
            double[] dataMax = printMaxData(rand, L, F, system, N, size, w1+w2, 2*N);
            System.out.println("");
            System.out.println("Simulation of perturbed system - Data maximal values:");
            System.out.println("");
            double[] dataMax_p = printMaxDataPerturbed(rand, L, F, system, N, size, 20, 2*N, itZ1TranslRate(x,w1,w2,replica));

            double normalisationZ1 = Math.max(dataMax[Z1],dataMax_p[Z1])*1.1;
            double normalisationZ2 = Math.max(dataMax[Z2],dataMax_p[Z1])*1.1;
            double normalisationZ3 = Math.max(dataMax[Z3],dataMax_p[Z2])*1.1;





            /*
            The following instruction allows us to create the evolution sequence <code>sequence_p</code>, which is
            obtained from the evolution sequence <code>sequence</code> by applying a perturbation, where:
            - as in EXPERIMENT #1 the perturbation is returned by the static method <code>itZ1PertRate()</code> defined later
            - the perturbation is applied at step 0
            - the sample sets of configurations in <code>sequence_p</code> have a cardinality which corresponds to that
            of <code>sequence</code> multiplied by <code>scale>/code>
            */




            int scale=5;
            EvolutionSequence sequence_p = sequence.apply(itZ1TranslRate(x, w1, w2, replica),0,scale);




            /*
            The following lines of code first defines three atomic distances between evolution sequences, named
            <code>atomicZi</code> for i=1,2,3. Then, these distances are evaluated, time-point by time-point, over
            evolution sequence <code>sequence</code> and its perturbed version <code>sequence_p</code> defined above.
            Finally, the time-point to time-point values of the distances are stored in .csv files.
            Technically, <code>distanceZi</code> is an atomic distance in the sense that it is an instance of
            class <code>AtomicDistanceExpression</code>, which consists in a data state expression,
            which maps a data state to a number, or rank, and a binary operator. As already discussed, in this case,
            given two configurations, the data state expression allow us to get the normalised value of protein Zi,
            which is a value in [0,1], from both configuration, and the binary operator gives us their difference, which,
            intuitively, is the difference with respect to the level of Zi between the two configurations.
            This distance will be lifted to two sample sets of configurations, those obtained from <code>sequence</code> and
            <code>sequence_p</code> at the same step.
            */

            int leftBound = 0;
            int rightBound = 1000;


            AtomicDistanceExpression atomicZ1 = new AtomicDistanceExpression(ds->ds.get(Z1)/normalisationZ1,(v1, v2) -> Math.abs(v2-v1));

            AtomicDistanceExpression atomicZ2 = new AtomicDistanceExpression(ds->ds.get(Z2)/normalisationZ2,(v1, v2) -> Math.abs(v2-v1));

            AtomicDistanceExpression atomicZ3 = new AtomicDistanceExpression(ds->ds.get(Z3)/normalisationZ3,(v1, v2) -> Math.abs(v2-v1));

            double[][] direct_evaluation_atomic_Z1 = new double[rightBound-leftBound][1];
            double[][] direct_evaluation_atomic_Z2 = new double[rightBound-leftBound][1];
            double[][] direct_evaluation_atomic_Z3 = new double[rightBound-leftBound][1];

            for (int i = 0; i<(rightBound-leftBound); i++){
                direct_evaluation_atomic_Z1[i][0] = atomicZ1.compute(i+leftBound, sequence, sequence_p);
                direct_evaluation_atomic_Z2[i][0] = atomicZ2.compute(i+leftBound, sequence, sequence_p);
                direct_evaluation_atomic_Z3[i][0] = atomicZ3.compute(i+leftBound, sequence, sequence_p);
            }

            Util.writeToCSV("./atomic_Z1.csv",direct_evaluation_atomic_Z1);
            Util.writeToCSV("./atomic_Z2.csv",direct_evaluation_atomic_Z2);
            Util.writeToCSV("./atomic_Z3.csv",direct_evaluation_atomic_Z3);


            /*Finally, from the atomic distance an instance of <code>MaxIntervalDistanceExpression</code> is created,
                    which gives the maximal value of the atomic distance computed in all instants of the given interval.
            */






            /*
            We conclude EXPERIMENT #2 by using the model checker.
            First we define the distances <code>distanceZi</code>, as instances of <code>MaxIntervalDistanceExpression</code>.
            Each <code>distanceZi</code> evaluates <code>atomicZi</code> in all time-points and returns the max value.
            Then we define the distance expression <code>distanceMaxZ1Z2Z3</code>, which returns the maximal value
            among those returned by three distances defined above.
            Then, we define a robustness formula, in particular an atomic formula, namely an instance of
            <code>AtomicRobustnessFormula</code>.
            This formula will be evaluated on the evolution sequence <code>sequence</code> and expresses that the
            distance, expressed by expression distance <code>distanceMaxZ1Z2Z3</code> between that evolution
            sequence and the evolution sequence obtained from it by applying the perturbation returned by method
            <code>itZ1TranslRate(x)</code>, is below a given threshold.

             */


            DistanceExpression distanceZ1 = new MaxIntervalDistanceExpression(
                    atomicZ1,
                    leftBound,
                    rightBound
            );

            DistanceExpression distanceZ2 = new MaxIntervalDistanceExpression(
                    atomicZ2,
                    leftBound,
                    rightBound
            );

            DistanceExpression distanceZ3 = new MaxIntervalDistanceExpression(
                    atomicZ3,
                    leftBound,
                    rightBound
            );


            DistanceExpression distanceMaxZ1Z2Z3 = new MaxDistanceExpression(
                    distanceZ1,
                    new MaxDistanceExpression(distanceZ2, distanceZ3)
            );


            double THRESHOLD = 0.15;

            RobustnessFormula robF = new AtomicRobustnessFormula(itZ1TranslRate(x,w1,w2,replica),
                    distanceMaxZ1Z2Z3,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    THRESHOLD);


            TruthValues value1 = new ThreeValuedSemanticsVisitor(rand,50,1.96).eval(robF).eval(5, 0, sequence);
            System.out.println(" ");
            System.out.println("\n robF evaluation at 0: " + value1);





            /*

            EXPERIMENT #3

            In this experiment, we proceed as in EXPERIMENT 2 to define a perturbation and an expression distance,
            then we define a robustness formula that expresses that the distance between the nominal and the
            perturbed evolution sequences is below a given threshold.
            The main differences between EXPERIMENT #3 and EXPERIMENT #2 is that now the perturbation modifies the
            translation rates of both gene 1 and gene 2 and, moreover, the effects of the perturbation are iteratively
            activated and deactivated.


             */


           /*
           The perturbed sequence is obtained by applying the perturbation returned by the static method
           <code>itProtTranslRate</code>, which, iteratively,
            */


            /*
            System.out.println("");
            System.out.println("Experiment 3");
            System.out.println("");

            double x_1 = -3.0;
            double y_1 = 3.0;
            double x_2 = 3.0;
            double y_2 = -3.0;

            int Q=1000;
            System.out.println("");
            System.out.println("Simulation of nominal system - Data average values");
            System.out.println("");
            printAvgData(rand, L, F, system, Q, size, 20, Q);
            System.out.println("");
            System.out.println("Simulation of perturbed system ");
            System.out.println("Translation rate of gene 1 incremented by " + x_1 + "and translation rate of gene 2 incremented by " + y_1 + "every 50 steps for 40 steps");
            System.out.println("Simulation of perturbed system - Data average values");
            System.out.println("Data average values:");
            System.out.println("");
            printAvgDataPerturbed(rand, L, F, system, Q, size, 20, Q, itProtTranslRate(x_1,y_1,-x_1,-y_1));
            System.out.println("");
            System.out.println("Simulation of nominal system - Data maximal values");
            System.out.println("");
            double[] dataMaxIt = printMaxData(rand, L, F, system, 5*Q, size, 20, 2*Q);
            System.out.println("");
            System.out.println("Simulation of perturbed system - Data maximal values");
            System.out.println("");
            double[] dataMaxItp = printMaxDataPerturbed(rand, L, F, system, 5*Q, size, 20, 2*Q, itProtTranslRate(x_1,y_1,x_2,y_2));
            double normalisationZ1it = Math.max(dataMaxIt[Z1],dataMaxItp[Z1])*1.1;
            double normalisationZ2it = Math.max(dataMaxIt[Z2],dataMaxItp[Z1])*1.1;
            double normalisationZ3it = Math.max(dataMaxIt[Z3],dataMaxItp[Z2])*1.1;

            AtomicDistanceExpression atomicZ1it = new AtomicDistanceExpression(ds->ds.get(Z1)/normalisationZ1it,(v1, v2) -> Math.abs(v2-v1));

            AtomicDistanceExpression atomicZ2it = new AtomicDistanceExpression(ds->ds.get(Z2)/normalisationZ2it,(v1, v2) -> Math.abs(v2-v1));

            AtomicDistanceExpression atomicZ3it = new AtomicDistanceExpression(ds->ds.get(Z3)/normalisationZ3it,(v1, v2) -> Math.abs(v2-v1));


            DistanceExpression distanceZ1it = new MaxIntervalDistanceExpression(
                    atomicZ1it,
                    leftBound,
                    rightBound
            );

            DistanceExpression distanceZ2it = new MaxIntervalDistanceExpression(
                    atomicZ2it,
                    leftBound,
                    rightBound
            );

            DistanceExpression distanceZ3it = new MaxIntervalDistanceExpression(
                    atomicZ3it,
                    leftBound,
                    rightBound
            );


            DistanceExpression distanceMaxZ1Z2Z3it = new MaxDistanceExpression(
                    distanceZ1it,
                    new MaxDistanceExpression(distanceZ2it, distanceZ3it)
            );


            EvolutionSequence sequence_p_it = sequence.apply(pert_transl_1(x),0,scale);

            double[][] direct_evaluation_atomic_Z1_it = new double[rightBound-leftBound][1];
            double[][] direct_evaluation_atomic_Z2_it = new double[rightBound-leftBound][1];
            double[][] direct_evaluation_atomic_Z3_it = new double[rightBound-leftBound][1];

            for (int i = 0; i<980; i++){
                direct_evaluation_atomic_Z1_it[i][0] = atomicZ1it.compute(i+leftBound, sequence, sequence_p_it);
                direct_evaluation_atomic_Z2_it[i][0] = atomicZ2it.compute(i+leftBound, sequence, sequence_p_it);
                direct_evaluation_atomic_Z3_it[i][0] = atomicZ3it.compute(i+leftBound, sequence, sequence_p_it);
            }

            Util.writeToCSV("./atomic_Z1_it.csv",direct_evaluation_atomic_Z1_it);
            Util.writeToCSV("./atomic_Z2_it.csv",direct_evaluation_atomic_Z2_it);
            Util.writeToCSV("./atomic_Z3_it.csv",direct_evaluation_atomic_Z3_it);

            int scaleIt = 5;
            EvolutionSequence sequence_pIt = sequence.apply(itProtTranslRate(x_1,y_1,x_2,y_2) ,0,scaleIt);



            System.out.println(" ");
            System.out.printf("%s \n", "The behavioural distance between the nominal and the perturbed sequence is: ");
            System.out.println(distanceMaxZ1Z2Z3it.compute(0, sequence, sequence_pIt));

            double THRESHOLD_IT = 0.15;

            RobustnessFormula robFIt = new AtomicRobustnessFormula(itProtTranslRate(x_1,y_1,x_2,y_2),
                    distanceMaxZ1Z2Z3it,
                    RelationOperator.LESS_OR_EQUAL_THAN,
                    THRESHOLD_IT);

            TruthValues value = new ThreeValuedSemanticsVisitor(rand,50,1.96).eval(robFIt).eval(5, 0, sequence);
            System.out.println(" ");
            System.out.println("\n robFIt evaluation at 0: " + value);


             */


        } catch (RuntimeException e) {
            e.printStackTrace();
        }



    }





    /*
    The following method generates an evolution sequence consisting of a sequence of <code>steps</code> sample sets
    of cardinality <code>size</code>, with the first sample set consisting in <code>size</code> copies of configuration
    <code>s</code>.
    For each sample set, all expressions over data states in <code>F</code> are evaluated on all configurations and
    their average value are printed out.
    The method returns the average evaluation that each expression in <code>F</code> gets in all configurations in all
    sample sets that are in the sequence in between positions <code>leftbound</code> and <code>rightbound</code>
     */
    private static double[] printAvgData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound){
        System.out.println(label);
        /*
        The following instruction creates an evolution sequence consisting in a sequence of <code>steps</code> sample
        sets of cardinality <size>.
        The first sample set contains <code>size</code> copies of configuration <code>s</code>.
        The subsequent sample sets are derived by simulating the dynamics.
        For each step from 1 to <code>steps</code> and for each variable, the average value taken by the
        variables in the elements of the sample set at each step are printed out.
         */
        double[][] data_avg = SystemState.sample(rg, F, s, steps, size);
        double[] tot = new double[F.size()];
        Arrays.fill(tot, 0);
        for (int i = 0; i < data_avg.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data_avg[i].length -1 ; j++) {
                System.out.printf("%f   ", data_avg[i][j]);
                if (leftbound <= i & i <= rightbound) {
                    tot[j]=tot[j]+data_avg[i][j];
                }
            }
            System.out.printf("%f\n", data_avg[i][data_avg[i].length -1]);
            if (leftbound <= i & i <= rightbound) {
                tot[data_avg[i].length -1]=tot[data_avg[i].length -1]+data_avg[i][data_avg[i].length -1];
            }
        }
        System.out.println(" ");
        System.out.println("Avg over all steps of the average values taken in the single step by the variables:");
        for(int j=0; j<tot.length-1; j++){
            System.out.printf("%f   ", tot[j] / (rightbound-leftbound));
        }
        System.out.printf("%f\n", tot[tot.length-1]/ (rightbound-leftbound));
        System.out.println("");
        System.out.println("");
        return tot;
    }


    private static double[] printAvgDataPerturbed(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound, Perturbation perturbation){
        System.out.println(label);

        double[] tot = new double[F.size()];

        double[][] data_avg = SystemState.sample(rg, F, perturbation, s, steps, size);
        Arrays.fill(tot, 0);
        for (int i = 0; i < data_avg.length; i++) {
            System.out.printf("%d>   ", i);
            for (int j = 0; j < data_avg[i].length -1 ; j++) {
                System.out.printf("%f   ", data_avg[i][j]);
                if (leftbound <= i & i <= rightbound) {
                    tot[j]=tot[j]+data_avg[i][j];
                }
            }
            System.out.printf("%f\n", data_avg[i][data_avg[i].length -1]);
            if (leftbound <= i & i <= rightbound) {
                tot[data_avg[i].length -1]=tot[data_avg[i].length -1]+data_avg[i][data_avg[i].length -1];
            }
        }
        System.out.println("");
        System.out.println("Avg over all steps of the average values taken in the single step by the variables:");
        for(int j=0; j<tot.length-1; j++){
            System.out.printf("%f   ", tot[j] / (rightbound-leftbound));
        }
        System.out.printf("%f\n", tot[tot.length-1]/ (rightbound-leftbound));
        System.out.println("");
        return tot;

    }

    /*
    The following method generates an evolution sequence consisting of a sequence of <code>steps</code> sample sets
    of cardinality <code>size</code>, with the first sample set consisting in <code>size</code> copies of configuration
    <code>s</code>. For each configuration in each sample set, all expressions over data states in <code>F</code> are
    evaluated. the method returns the max evaluation that each expression in <code>F</code> gives in all sample sets
    that are in the sequence in between positions <code>leftbound</code> and <code>rightbound</code>
     */


    private static double[] printMaxData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound){

        /*
        The following instruction creates an evolution sequence consisting in a sequence of <code>steps</code> sample
        sets of cardinality <size>.
        The first sample set contains <code>size</code> copies of configuration <code>s</code>.
        The subsequent sample sets are derived by simulating the dynamics.
        Finally, for each step from 1 to <code>steps</code> and for each variable, the maximal value taken by the
        variable in the elements of the sample set is stored.
         */
        double[][] data_max = SystemState.sample_max(rg, F, s, steps, size);
        double[] max = new double[F.size()];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < data_max.length; i++) {
            //System.out.printf("%d>   ", i);
            for (int j = 0; j < data_max[i].length -1 ; j++) {
                //System.out.printf("%f   ", data_max[i][j]);
                if (leftbound <= i & i <= rightbound) {
                    if (max[j] < data_max[i][j]) {
                        max[j] = data_max[i][j];
                    }
                }
            }
            //System.out.printf("%f\n", data_max[i][data_max[i].length -1]);
            if (leftbound <= i & i <= rightbound) {
                if (max[data_max[i].length -1] < data_max[i][data_max[i].length -1]) {
                    max[data_max[i].length -1] = data_max[i][data_max[i].length -1];
                }
            }
        }
        System.out.println(" ");
        //System.out.println("Maximal values taken by variables by the non perturbed system:");
        System.out.println(label);
        for(int j=0; j<max.length-1; j++){
            System.out.printf("%f ", max[j]);
        }
        System.out.printf("%f\n", max[max.length-1]);
        System.out.println("");
        System.out.println("");
        return max;
    }


    private static double[] printMaxDataPerturbed(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size, int leftbound, int rightbound, Perturbation perturbation){

        double[] max = new double[F.size()];

        double[][] data_max = SystemState.sample_max(rg, F, perturbation, s, steps, size);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < data_max.length; i++) {
            //System.out.printf("%d>   ", i);
            for (int j = 0; j < data_max[i].length -1 ; j++) {
                //System.out.printf("%f   ", data_max[i][j]);
                if (leftbound <= i & i <= rightbound) {
                    if (max[j] < data_max[i][j]) {
                        max[j] = data_max[i][j];
                    }
                }
            }
            //System.out.printf("%f\n", data_max[i][data_max[i].length -1]);
            if (leftbound <= i & i <= rightbound) {
                if (max[data_max[i].length -1] < data_max[i][data_max[i].length -1]) {
                    max[data_max[i].length -1] = data_max[i][data_max[i].length -1];
                }
            }
        }
        //System.out.println("");
        //System.out.println("Maximal values taken by variables in steps by the perturbed system:");
        System.out.println(label);
        for(int j=0; j<max.length-1; j++){
            System.out.printf("%f ", max[j]);
        }
        System.out.printf("%f\n", max[max.length-1]);
        System.out.println("");
        return max;

    }




    /*
    The following method returns a perturbation. In particular this is an atomic perturbation, i.e. an instance of
    <code>AtomicPerturbation</code>. It consists of two elements:
    - a random function over data states, i.e. an instance of <code>DataStateFunction</code>, which is returned by
    method <code>changeDZ1()>/code>,
    - the number of steps after which the random function will be applied, which is 0 in this case.
    In particular, the function returned by <code>changeDZ1()>/code> modifies the data state by changing the value of
    the degradation rate of protein Z1.
    Overall, this perturbation changes immediately the degradation rate of Z1, thus impacting on the whole evolution
    of the system.
     */

    /* public static Perturbation pert_transl_1(){
        return new AtomicPerturbation(0,Repressilator::pert_s11);
    }

    private static DataState pert_s11(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)+6,0)));
        return state.apply(updates);
    }
    */

    public static Perturbation pert_transl_1(double x){
        return new AtomicPerturbation(0,(rg,ds)->ds.apply(upd_s11(rg,ds,x)));
    }

    private static List<DataStateUpdate> upd_s11(RandomGenerator rg, DataState state, double x){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)+x,0)));
        return updates;
    }


    /*
    The following method returns a perturbation. In particular this is an iterative perturbation, i.e. an instance of
    <code>IterativePerturbation</code>. It consists of two elements, an integer and a body perturbation, the idea being
    that the integer expresses how many times the body perturbation is applied.
    In this case, the body perturbation is a sequential perturbation, namely an instance of
    <code>SequentialPerturbation</code>, which, essentially, consists in two perturbations where the second is applied
    after the first has terminated its effect. The first perturbation is applied after <code>w1<code> steps
    and, increments the translation rate of Z1 by the parameter <code>x</code>. The second perturbation is applied after
    further <code>w2<code> stepps and reverts the effects of the first one.

     */


    public static Perturbation itZ1TranslRate(double x, int w1, int w2, int replica){
        return new IterativePerturbation(replica,
                new SequentialPerturbation(
                        new AtomicPerturbation(w1,(rg,ds)->ds.apply(upd_s11(rg,ds,x))),
                        new AtomicPerturbation(w2,(rg,ds)->ds.apply(upd_s11(rg,ds,-x)))
                )
        );
    }









    /*
    The following method returns a perturbation. In particular this is an iterative perturbation, i.e. an instance of
    <code>IterativePerturbation</code>. It consists of two elements, an integer and a body perturbation, the idea being
    that the integer expresses how many times the body perturbation is applied.
    In this case, the body perturbation is a sequential perturbation, namely an instance of
    <code>SequentialPerturbation</code>, which, essentially, consists in two perturbations where the second is applied
    after the first has terminated its effect. The first perturbation is applied after 10 steps
    and, increments the translation rate of Z2 and decrements that of Z1. The second perturbation is applied after 40
    steps and reverts the effects of the first one. Overall, the perturbed system evolves with perturbed values of the
    degradation rates of Z1 and Z1 in between time intervals [10,50], [60,100], [110.150].

     */


    public static Perturbation itProtTranslRate(double x1 , double y1, double x2, double y2){
        return new IterativePerturbation(18,
                new SequentialPerturbation(
                        new AtomicPerturbation(10,(rg,ds)->ds.apply(upd_s11_s12(rg,ds,x1,y1))),
                        new AtomicPerturbation(40,(rg,ds)->ds.apply(upd_s11_s12(rg,ds,x2,y2)))
                )
        );
    }


    private static List<DataStateUpdate> upd_s11_s12(RandomGenerator rg, DataState state, double x, double y){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)+x,0)));
        updates.add(new DataStateUpdate(s12,Math.max(state.get(s12)+y,0)));
        return updates;
    }





    /*
    private static DataState fastZ1slowZ2(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)+3,0)));
        updates.add(new DataStateUpdate(s12,Math.max(state.get(s12)-3,0)));
        return state.apply(updates);
    }



    public static Perturbation itProtTranslRate(){
        return new IterativePerturbation(3,
                new SequentialPerturbation(
                        new AtomicPerturbation(10,Repressilator::slowZ1fastZ2),
                        new AtomicPerturbation(40,Repressilator::fastZ1slowZ2)
                )
        );
    }


    private static DataState slowZ1fastZ2(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)-3,0)));
        updates.add(new DataStateUpdate(s12,Math.max(state.get(s12)+3,0)));
        return state.apply(updates);
    }

    private static DataState fastZ1slowZ2(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();
        updates.add(new DataStateUpdate(s11,Math.max(state.get(s11)+3,0)));
        updates.add(new DataStateUpdate(s12,Math.max(state.get(s12)-3,0)));
        return state.apply(updates);
    }

    */


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


        double new_kon1 = (K01 + K11 * Math.exp(BETA1 + THETA11*state.get(Z1) + THETA21*state.get(Z2) + THETA31*state.get(Z3)))/(1+Math.exp(BETA1 + THETA11*state.get(Z1) + THETA21*state.get(Z2) + THETA31*state.get(Z3)));
        updates.add(new DataStateUpdate(kon1,new_kon1));

        double new_kon2 = (K02 + K12 * Math.exp(BETA2 + THETA12*state.get(Z1) + THETA22*state.get(Z2) + THETA32*state.get(Z3)))/(1+Math.exp(BETA2 + THETA12*state.get(Z1) + THETA22*state.get(Z2) + THETA32*state.get(Z3)));
        updates.add(new DataStateUpdate(kon2,new_kon2));

        double new_kon3 = (K03 + K13 * Math.exp(BETA3 + THETA13*state.get(Z1) + THETA23*state.get(Z2) + THETA33*state.get(Z3)))/(1+Math.exp(BETA3 + THETA13*state.get(Z1) + THETA23*state.get(Z2) + THETA33*state.get(Z3)));
        updates.add(new DataStateUpdate(kon3,new_kon3));

        return updates;

    }







    /*
    Method getInitialState assigns the initial value to all variables
    */
    public static DataState getInitialState(double gran, double Tstep, double Treal, double Tdelta) {
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
        values.put(d11, 0.1);


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
        values.put(d12, 0.1);

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
        values.put(d13, 0.1);


        return new DataState(NUMBER_OF_VARIABLES, i -> values.getOrDefault(i, Double.NaN), gran, Tstep, Treal, Tdelta);
    }

}
