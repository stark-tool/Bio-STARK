package reacsys;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import org.apache.commons.math3.random.RandomGenerator;
import stark.*;
import stark.controller.Controller;
import stark.controller.NilController;
import stark.ds.DataState;
import stark.ds.DataStateExpression;
import stark.ds.DataStateUpdate;

import java.util.*;

public class Main {

    // quantities in neuron #1
    public static final int Ca1 = 0; // amount of calcium
    public static final int X1 = 1; // calcium ligand
    public static final int XStar1 = 2; // compound calcium-ligand
    public static final int Ve1 = 3; // vesicles before exocytosis
    public static final int VeStar1 = 4; // vesicles after exocytosis
    public static final int T1 = 5; // neurotransmitter
    public static final int c1 = 6; // closure status of receptor from neuron #3 (1 = closed, 0 = not closed)
    public static final int o1 = 7; // opening status of receptor from neuron #3 (1 = open, 0 = not open)

    // quantities in neuron #2
    public static final int Ca2 = 8; // amount of calcium
    public static final int X2 = 9; // calcium ligand
    public static final int XStar2 = 10; // compound calcium-ligand
    public static final int Ve2 = 11; // vesicles before exocytosis
    public static final int VeStar2 = 12; // vesicles after exocytosis
    public static final int T2 = 13; // neurotransmitter
    public static final int c2 = 14; // closure status of receptor from neuron #3 (1 = closed, 0 = not closed)
    public static final int o2 = 15; // opening status of receptor from neuron #3 (1 = open, 0 = not open)

    // quantities in neuron #3
    public static final int Ca3 = 16; // amount of calcium
    public static final int X3 = 17; // calcium ligand
    public static final int XStar3 = 18; // compound calcium-ligand
    public static final int Ve3 = 19; // vesicles before exocytosis
    public static final int VeStar3 = 20; // vesicles after exocytosis
    public static final int T3 = 21; // neurotransmitter
    public static final int c31 = 22; // closure status of receptor from neuron #1 (1 = closed, 0 = not closed)
    public static final int o31 = 23; // opening status of receptor from neuron #1 (1 = open, 0 = not open)
    public static final int c32 = 24; // closure status of receptor from neuron #2 (1 = closed, 0 = not closed)
    public static final int o32 = 25; // opening status of receptor from neuron #2 (1 = open, 0 = not open)

    private static final int NUMBER_OF_VARIABLES = 26;





    public static void main(String[] args) {

        try {

            Controller controller = new NilController();

            DataState initialState = getInitialState( );

            RandomGenerator rand = new DefaultRandomGenerator();

            SystemState system = new ControlledSystem(controller, (rg, ds) -> ds.apply(applyReactions(rg, ds)),initialState);

            EvolutionSequence sequence = new EvolutionSequence(rand, rg -> system, 1);

            ArrayList<DataStateExpression> F = new ArrayList<>();
            ArrayList<String> L = new ArrayList<>();
            L.add("Ca1      ");
            L.add("Ca2      ");
            L.add("Ca3      ");



            F.add(ds->ds.get(Ca1));
            F.add(ds->ds.get(Ca2));
            F.add(ds->ds.get(Ca3));
            printAvgData(rand, L, F, system, 10000, 1, 100, 10100
            );




        }

        catch (RuntimeException e) {

            e.printStackTrace();

        }

    }



    /*- The method <code>applyReactions</code> maps a random generator <code>rg</code> and a data state <code>ds</code>
    to a list of "updates", which are instances of class <code>DataStateUpdate</code>. Essentially, these updates
    will be used to update the data state <ds> so that the new data state is the one available at the next instant
    after the reactions are applied on <code>ds</code> at present instant.

     */
    private static List<DataStateUpdate> applyReactions(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();


        // new value for calcium in first neuron, Ca1 - reactions r11, r19 can produce it
        if(state.get(o1)==1){ // postsynaptic activity, neural receptor open - reaction r91
            updates.add(new DataStateUpdate(Ca1,1.0)); //
        }
        else {
            if (state.get(Ca1) > 0 & state.get(Ca1) < 10) { // presynaptic activity: Ca doubles until it reaches threshold 10 - reaction r11
                updates.add(new DataStateUpdate(Ca1, state.get(Ca1) * 2));
            } else {
                updates.add(new DataStateUpdate(Ca1, 0.0));
            }
        }

        // new value for calcium in second neuron, Ca2 - reactions r21, r29 can produce it
        if(state.get(o2)==1){ // postsynaptic activity, neural receptor open - reaction r29
            updates.add(new DataStateUpdate(Ca2,1.0));
        }
        else {
            if (state.get(Ca2) > 0 & state.get(Ca2) < 10) { //presynaptic activity: Ca doubles until it reaches threshold 10 - r21
                updates.add(new DataStateUpdate(Ca2, state.get(Ca2) * 2));
            } else {
                updates.add(new DataStateUpdate(Ca2, 0.0));
            }
        }

        // new value for calcium in third neuron, Ca3 - reactions r31, r39a, r39b, r39c can produce it
        if(state.get(o31)==1 & state.get(o32)==1){ // postisynaptic activity, both neural receptors open - reaction r39c
            updates.add(new DataStateUpdate(Ca3,4.0));
        }
        else{
            if(state.get(o31)==1 || state.get(o32)==1){ // postisynaptic activity, one neural receptor open - reaction r39a or r39b
                updates.add(new DataStateUpdate(Ca3,1.0));
            }
            else{
                if (state.get(Ca3) > 0 & state.get(Ca3) < 10) { //presynaptic activity: Ca doubles until it reaches threshold 10 - r31
                    updates.add(new DataStateUpdate(Ca3, state.get(Ca3) * 2)); //presynaptic activity: Ca doubles until it reaches threshold 10 - r31
                } else {
                    updates.add(new DataStateUpdate(Ca3, 0.0));
                }
            }
        }


        // new value for calcium ligand in first neuron, X1 - reactions r12, r15 can produce it
        if(state.get(XStar1)==10 & state.get(Ve1)>0){ // formation of vesicles - reaction r15
            updates.add(new DataStateUpdate(X1,10.0));
        }
        else {
            if (state.get(X1) > 0 & state.get(XStar1) == 0) { // permanency of calcium ligand - reaction r12
                updates.add(new DataStateUpdate(X1, state.get(X1)));
            } else {
                updates.add(new DataStateUpdate(X1, 0.0));
            }
        }

        // new value for calcium ligand in second neuron, X2 - reactions r22, r25 can produce it
        if(state.get(XStar2)==10 & state.get(Ve2)>0){ // formation of vesicles - reaction r25
            updates.add(new DataStateUpdate(X2,10.0));
        }
        else {
            if (state.get(X2) > 0 & state.get(XStar2) == 0) {
                updates.add(new DataStateUpdate(X2, state.get(X2))); // permanency of calcium ligand - reaction r22
            } else {
                updates.add(new DataStateUpdate(X2, 0.0));
            }
        }

        // new value for calcium ligand in third neuron, X3 - reactions r32, r35 can produce it
        if(state.get(XStar3)==10 & state.get(Ve3)>0){ // formation of vesicles - reaction r35
            updates.add(new DataStateUpdate(X3,10.0));
        }
        else {
            if (state.get(X3) > 0 & state.get(XStar3) == 0) {
                updates.add(new DataStateUpdate(X3, state.get(X3))); // permanency of calcium ligand - reaction r32
            } else {
                updates.add(new DataStateUpdate(X3, 0.0));
            }
        }


        // new values for vesicles in neuron 1, Ve1 - reactions r13 and r16 can produce it
        if(state.get(Ve1)>0 & state.get(VeStar1)==0){ // permanency of vesicles
            updates.add(new DataStateUpdate(Ve1,state.get(Ve1)));}
        else {
            if(state.get(VeStar1)>0){ // neurotransmitter released
                updates.add(new DataStateUpdate(Ve1,state.get(VeStar1)));
            }
            else {
                updates.add(new DataStateUpdate(Ve1, 0.0));
            }
        }

        // new values for vesicles in neuron 2, Ve2 - reactions r23 and r26 can produce it
        if(state.get(Ve2)>0 & state.get(VeStar2)==0){ // permanency of vesicles
            updates.add(new DataStateUpdate(Ve2,state.get(Ve2)));}
        else {
            if(state.get(VeStar2)>0){ // neurotransmitter released
                updates.add(new DataStateUpdate(Ve2,state.get(VeStar2)));
            }
            else {
                updates.add(new DataStateUpdate(Ve2, 0.0));
            }
        }

        // new values for vesicles in neuron 3, Ve3 - reactions r33 and r36 can produce it
        if(state.get(Ve3)>0 & state.get(VeStar3)==0){ // permanency of vesicles
            updates.add(new DataStateUpdate(Ve3,state.get(Ve3)));}
        else {
            if(state.get(VeStar3)>0){ // neurotransmitter released
                updates.add(new DataStateUpdate(Ve3,state.get(VeStar3)));
            }
            else {
                updates.add(new DataStateUpdate(Ve3, 0.0));
            }
        }


        // new values for complex calcium-ligand in neuron 1, XStar1 - reaction r14 can produce it
        if(state.get(Ca1)>=10 & state.get(X1)>=10){ // enough calcium to form the complex
            updates.add(new DataStateUpdate(XStar1,state.get(X1)));
        }
        else{
            updates.add(new DataStateUpdate(XStar1,0.0));
        }

        // new values for complex calcium-ligand in neuron 2, XStar2 - reaction r24 can produce it
        if(state.get(Ca2)>=10 & state.get(X2)>=10){ // enough calcium to form the complex
            updates.add(new DataStateUpdate(XStar2,state.get(X2)));
        }
        else{
            updates.add(new DataStateUpdate(XStar2,0.0));
        }

        // new values for complex calcium-ligand in neuron 3, XStar3 - reaction r34 can produce it
        if(state.get(Ca3)>=10 & state.get(X3)>=10){ // enough calcium to form the complex
            updates.add(new DataStateUpdate(XStar3,state.get(X3)));
        }
        else{
            updates.add(new DataStateUpdate(XStar3,0.0));
        }


        // new values for vesicles with neurotransmitter in first neuron, VeStar1 - reactons r15 can produce it
        if(state.get(XStar1)==10 & state.get(Ve1)>0){ // enough calcium ligand to release the neurotransmitter
            updates.add(new DataStateUpdate(VeStar1,state.get(Ve1)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar1,0.0));
        }

        // new values for vesicles with neurotransmitter in second neuron, VeStar2 - reactons r25 can produce it
        if(state.get(XStar2)==10 & state.get(Ve2)>0){// enough calcium ligand to release the neurotransmitter
            updates.add(new DataStateUpdate(VeStar2,state.get(Ve2)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar2,0.0));
        }

        // new values for vesicles with neurotransmitter in third neuron, VeStar3 - reactions r35 can produce it
        if(state.get(XStar3)==10 & state.get(Ve3)>0){// enough calcium ligand to release the neurotransmitter
            updates.add(new DataStateUpdate(VeStar3,state.get(Ve3)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar3,0.0));
        }


        // new values for neurotransmitter from first neuron, T1 - reaction r16 can produce it
        if(state.get(VeStar1)>0){ // neurotransmitter released
            updates.add(new DataStateUpdate(T1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T1,0.0));
        }

        // new values for neurotransmitter from second neuron, T2 - reaction r36 can produce it
        if(state.get(VeStar2)>0){ // neurotransmitter released
            updates.add(new DataStateUpdate(T2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T2,0.0));
        }

        // new values for neurotransmitter from third neuron, T3 - reaction r36 can produce it
        if(state.get(VeStar3)>0){ // neurotransmitter released
            updates.add(new DataStateUpdate(T3,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T3,0.0));
        }


        // new values for closing state of neuroreceptor of neuron 1, c1 - reactions r17 and r19 can modify it
        if((state.get(c1)>0 & state.get(T3)==0) || state.get(o1)>0){
            // neuroreceptor remains closed if there is no neurotransmitter or becomes closed if it is open
            updates.add(new DataStateUpdate(c1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c1,0.0));
        }

        // new values for closing state of neuroreceptor of neuron 2, c2 - reactions r27 and r29 can modify it
        if((state.get(c2)>0 & state.get(T3)==0) || state.get(o2)>0){
            // neuroreceptor remains closed if there is no neurotransmitter or becomes closed if it is open
            updates.add(new DataStateUpdate(c2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c2,0.0));
        }

        // new values for opening state of neuroreceptor of neuron 1, o1 -  reactions r18  can modify it
        if(state.get(T3)>0){
            // neuroreceptor becomes open if there is the neurotransmitter
            updates.add(new DataStateUpdate(o1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o1,0.0));
        }


        // new values for opening state of neuroreceptor of neuron 2, o2 -  reactions r18  can modify it
        if(state.get(T3)>0){
            // neuroreceptor becomes open if there is the neurotransmitter
            updates.add(new DataStateUpdate(o2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o2,0.0));
        }



        // new values for closure state of first neuroreceptor of neuron 3, o31 -  reactions r37a,r39a,r39c  can modify it
        if(state.get(c31)>0 & state.get(T1)==0){
            // neuroreceptor from neuron #1 remains closed in absence of the neurotransmitter - r37a
            updates.add(new DataStateUpdate(c31,state.get(c31)));
        }
        else{
            if(state.get(o31)==1){
                // neuroreceptor from neuron #1 closes if it is open - r39a or r39a
                updates.add(new DataStateUpdate(c31,1.0));
            }
            else{
                updates.add(new DataStateUpdate(c31, 0.0));
            }
        }

        if(state.get(c32)>0 & state.get(T2)==0){
            // neuroreceptor from neuron #2 remains closed in absence of the neurotransmitter - r37b
            updates.add(new DataStateUpdate(c32,state.get(c32)));
        }
        else{
            if(state.get(o32)==1){
                // neuroreceptor from neuron #2 closes if it is open - r39b or r39c
                updates.add(new DataStateUpdate(c32,1.0));
            }
            else{
                updates.add(new DataStateUpdate(c32, 0.0));
            }
        }

        // new values for opening state of first neuroreceptor of neuron 3, o31 -  reactions r38a  can modify it
        if(state.get(T1)>0){ // neuroreceptor opens if there is the neurotransmitter
            updates.add(new DataStateUpdate(o31,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o31,0.0));
        }

        // new values for opening state of second neuroreceptor of neuron 3, o32 -  reactions r38b  can modify it
        if(state.get(T2)>0){// neuroreceptor opens if there is the neurotransmitter
            updates.add(new DataStateUpdate(o32,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o32,0.0));
        }

        return(updates);

    }




    private static DataState getInitialState( ){
        Map<Integer, Double> initialValues = new HashMap<>();

        initialValues.put(Ca1,1.0);
        initialValues.put(X1,10.0);
        initialValues.put(XStar1,0.0);
        initialValues.put(Ve1,5.0);
        initialValues.put(VeStar1,0.0);
        initialValues.put(T1,0.0);
        initialValues.put(c1,1.0);
        initialValues.put(o1,0.0);

        initialValues.put(Ca2,1.0);
        initialValues.put(X2,10.0);
        initialValues.put(XStar2,0.0);
        initialValues.put(Ve2,5.0);
        initialValues.put(VeStar2,0.0);
        initialValues.put(T2,0.0);
        initialValues.put(c2,1.0);
        initialValues.put(o2,0.0);

        initialValues.put(Ca3,0.0);
        initialValues.put(X3,10.0);
        initialValues.put(XStar3,0.0);
        initialValues.put(Ve3,5.0);
        initialValues.put(VeStar3,0.0);
        initialValues.put(T3,0.0);
        initialValues.put(c31,1.0);
        initialValues.put(o31,0.0);
        initialValues.put(c32,1.0);
        initialValues.put(o32,0.0);

        return new DataState(NUMBER_OF_VARIABLES,i -> initialValues.getOrDefault(i, Double.NaN));
    }


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

}