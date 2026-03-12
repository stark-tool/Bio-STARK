package reacsys;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import org.apache.commons.math3.random.RandomGenerator;
import stark.*;
import stark.controller.Controller;
import stark.controller.NilController;
import stark.ds.DataState;
import stark.ds.DataStateUpdate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {

    public static final int Ca1 = 0;
    public static final int X1 = 1;
    public static final int XStar1 = 2;
    public static final int Ve1 = 3;
    public static final int VeStar1 = 4;
    public static final int T1 = 5;
    public static final int c1 = 6;
    public static final int o1 = 7;

    public static final int Ca2 = 8;
    public static final int X2 = 9;
    public static final int XStar2 = 10;
    public static final int Ve2 = 11;
    public static final int VeStar2 = 12;
    public static final int T2 = 13;
    public static final int c2 = 14;
    public static final int o2 = 15;

    public static final int Ca3 = 16;
    public static final int X3 = 17;
    public static final int XStar3 = 18;
    public static final int Ve3 = 19;
    public static final int VeStar3 = 20;
    public static final int T3 = 21;
    public static final int c31 = 22;
    public static final int o31 = 23;
    public static final int c32 = 24;
    public static final int o32 = 25;

    private static final int NUMBER_OF_VARIABLES = 26;





    public static void main(String[] args) {

        try {

            Controller controller = new NilController();

            DataState initialState = getInitialState( );

            RandomGenerator rand = new DefaultRandomGenerator();

            SystemState system = new ControlledSystem(controller, (rg, ds) -> ds.apply(applyReactions(rg, ds)),initialState);

        }

        catch (RuntimeException e) {

            e.printStackTrace();

        }

    }

    private static List<DataStateUpdate> applyReactions(RandomGenerator rg, DataState state){
        List<DataStateUpdate> updates = new LinkedList<>();

        // new value for calcium in first neuron, Ca1 - reactions r11, r19 can produce it
        if(state.get(o1)==1){ // neural receptor open - r91
            updates.add(new DataStateUpdate(Ca1,1.0)); //
        }
        else {
            if (state.get(Ca1) > 0 & state.get(Ca1) < 10) { // presynaptic activity: Ca doubles until it reaches threshold 10 - r11
                updates.add(new DataStateUpdate(Ca1, state.get(Ca1) * 2));
            } else {
                updates.add(new DataStateUpdate(Ca1, 0.0));
            }
        }

        // new value for calcium in second neuron, Ca2 - reactions r21, r29 can produce it
        if(state.get(o2)==1){ // neural receptor open - r29
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
        if(state.get(o31)==1 & state.get(o32)==1){ // both neural receptors open - r39c
            updates.add(new DataStateUpdate(Ca3,4.0));
        }
        else{
            if(state.get(o31)==1 || state.get(o32)==1){ // one neural receptor open - r39a or r39b
                updates.add(new DataStateUpdate(Ca3,1.0));
            }
            else{
                if (state.get(Ca3) > 0 & state.get(Ca3) < 10) {
                    updates.add(new DataStateUpdate(Ca3, state.get(Ca3) * 2));//presynaptic activity: Ca doubles until it reaches threshold 10 - r31
                } else {
                    updates.add(new DataStateUpdate(Ca3, 0.0));
                }
            }
        }




        // new values for calcium ligand in first neuron, X1 - reactions r12, r15 can produce it
        if(state.get(XStar1)==10 & state.get(Ve1)>0){ // formation of vesicles - r15
            updates.add(new DataStateUpdate(X1,10.0));
        }
        else {
            if (state.get(X1) > 0 & state.get(XStar1) == 0) { // permanency of calcium ligand - r12
                updates.add(new DataStateUpdate(X1, state.get(X1)));
            } else {
                updates.add(new DataStateUpdate(X1, 0.0));
            }
        }

        // new values for calcium ligand in second neuron, X2 - reactions r22, r25 can produce it
        if(state.get(XStar2)==10 & state.get(Ve2)>0){ // formation of vesicles - r25
            updates.add(new DataStateUpdate(X2,10.0));
        }
        else {
            if (state.get(X2) > 0 & state.get(XStar2) == 0) {
                updates.add(new DataStateUpdate(X2, state.get(X2))); // permanency of calcium ligand - r22
            } else {
                updates.add(new DataStateUpdate(X2, 0.0));
            }
        }

        // new values for calcium ligand in third neuron, X3 - reactions r32, r35 can produce it
        if(state.get(XStar3)==10 & state.get(Ve3)>0){ // formation of vesicles - r35
            updates.add(new DataStateUpdate(X3,10.0));
        }
        else {
            if (state.get(X3) > 0 & state.get(XStar3) == 0) {
                updates.add(new DataStateUpdate(X3, state.get(X3))); // permanency of calcium ligand - r32
            } else {
                updates.add(new DataStateUpdate(X3, 0.0));
            }
        }

        // new values for vesicles in neuron 1, Ve1 - reactons r13 and r16 can produce it
        if(state.get(Ve1)>0 & state.get(VeStar1)==0){
            updates.add(new DataStateUpdate(Ve1,state.get(Ve1)));}
        else {
            if(state.get(VeStar1)>0){
                updates.add(new DataStateUpdate(Ve1,state.get(VeStar1)));
            }
            else {
                updates.add(new DataStateUpdate(Ve1, 0.0));
            }
        }

        // new values for vesicles in neuron 2, Ve2 - reactons r23 and r26 can produce it
        if(state.get(Ve2)>0 & state.get(VeStar2)==0){
            updates.add(new DataStateUpdate(Ve2,state.get(Ve2)));}
        else {
            if(state.get(VeStar2)>0){
                updates.add(new DataStateUpdate(Ve2,state.get(VeStar2)));
            }
            else {
                updates.add(new DataStateUpdate(Ve2, 0.0));
            }
        }

        // new values for vesicles in neuron 3, Ve3 - reactons r33 and r36 can produce it
        if(state.get(Ve3)>0 & state.get(VeStar3)==0){
            updates.add(new DataStateUpdate(Ve3,state.get(Ve3)));}
        else {
            if(state.get(VeStar3)>0){
                updates.add(new DataStateUpdate(Ve3,state.get(VeStar3)));
            }
            else {
                updates.add(new DataStateUpdate(Ve3, 0.0));
            }
        }



        // new values for complex calcium-ligand in neuron 1, XStar1 - reaction r14 can produce it
        if(state.get(Ca1)>=10 & state.get(X1)>=10){
            updates.add(new DataStateUpdate(XStar1,state.get(X1)));
        }
        else{
            updates.add(new DataStateUpdate(XStar1,0.0));
        }

        // new values for complex calcium-ligand in neuron 2, XStar2 - reaction r24 can produce it
        if(state.get(Ca2)>=10 & state.get(X2)>=10){
            updates.add(new DataStateUpdate(XStar2,state.get(X2)));
        }
        else{
            updates.add(new DataStateUpdate(XStar2,0.0));
        }

        // new values for complex calcium-ligand in neuron 3, XStar3 - reaction r34 can produce it
        if(state.get(Ca3)>=10 & state.get(X3)>=10){
            updates.add(new DataStateUpdate(XStar3,state.get(X3)));
        }
        else{
            updates.add(new DataStateUpdate(XStar3,0.0));
        }

        // new values for vesicles with neurotransmitter in first neuron, VeStar1 - reactons r15 can produce it
        if(state.get(XStar1)==10 & state.get(Ve1)>0){
            updates.add(new DataStateUpdate(VeStar1,state.get(Ve1)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar1,0.0));
        }

        // new values for vesicles with neurotransmitter in second neuron, VeStar2 - reactons r25 can produce it
        if(state.get(XStar2)==10 & state.get(Ve2)>0){
            updates.add(new DataStateUpdate(VeStar2,state.get(Ve2)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar2,0.0));
        }

        // new values for vesicles with neurotransmitter in third neuron, VeStar3 - reactions r35 can produce it
        if(state.get(XStar3)==10 & state.get(Ve3)>0){
            updates.add(new DataStateUpdate(VeStar3,state.get(Ve3)));
        }
        else{
            updates.add(new DataStateUpdate(VeStar3,0.0));
        }


        // new values for neurotransmitter from first neuron, T1 - reaction r16 can produce it
        if(state.get(VeStar1)>0){
            updates.add(new DataStateUpdate(T1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T1,0.0));
        }

        // new values for neurotransmitter from second neuron, T2 - reaction r36 can produce it
        if(state.get(VeStar2)>0){
            updates.add(new DataStateUpdate(T2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T2,0.0));
        }

        // new values for neurotransmitter from third neuron, T3 - reaction r36 can produce it
        if(state.get(VeStar3)>0){
            updates.add(new DataStateUpdate(T3,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T3,0.0));
        }

        // new values for closing state of neuroreceptor of neuron 1, c1 - reactions r17 and r19 can modify it
        if((state.get(c1)>0 & state.get(T3)==0) || state.get(o1)>0){
            updates.add(new DataStateUpdate(c1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c1,0.0));
        }

        // new values for closing state of neuroreceptor of neuron 2, c2 - reactions r27 and r29 can modify it
        if((state.get(c2)>0 & state.get(T3)==0) || state.get(o2)>0){
            updates.add(new DataStateUpdate(c2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c2,0.0));
        }

        // new values for opening state of neuroreceptor of neuron 1, o1 -  reactions r18  can modify it
        if(state.get(T3)>0){
            updates.add(new DataStateUpdate(o1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o1,0.0));
        }

        // new values for opening state of neuroreceptor of neuron 2, o2 -  reactions r18  can modify it
        if(state.get(T3)>0){
            updates.add(new DataStateUpdate(o2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o2,0.0));
        }



        // New values for c31,c32,o31,o32 - rules r37a,r38a,r39a,r37b,r38b,r39b,r39c can modify them

        if(state.get(c31)>0 & state.get(T1)==0){
            updates.add(new DataStateUpdate(c31,state.get(c31)));
        }
        else{
            if(state.get(o31)==1){
                updates.add(new DataStateUpdate(c31,1.0));
            }
            else{
                updates.add(new DataStateUpdate(c31, 0.0));
            }
        }

        if(state.get(c32)>0 & state.get(T2)==0){
            updates.add(new DataStateUpdate(c32,state.get(c32)));
        }
        else{
            if(state.get(o32)==1){
                updates.add(new DataStateUpdate(c32,1.0));
            }
            else{
                updates.add(new DataStateUpdate(c32, 0.0));
            }
        }

        // new values for opening state of first neuroreceptor of neuron 3, o31 -  reactions r38a  can modify it
        if(state.get(T1)>0){
            updates.add(new DataStateUpdate(o31,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o31,0.0));
        }

        // new values for opening state of second neuroreceptor of neuron 3, o32 -  reactions r38b  can modify it
        if(state.get(T2)>0){
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


}