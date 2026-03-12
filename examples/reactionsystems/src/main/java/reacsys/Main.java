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
    public static final int c3 = 22;
    public static final int o3 = 23;

    private static final int NUMBER_OF_VARIABLES = 24;





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

        // new values for Ca1,Ca2,Ca3 - reactions r11,r12,r13
        if(state.get(Ca1)>0 & state.get(Ca1)<10){
            updates.add(new DataStateUpdate(Ca1,state.get(Ca1)*2));}
        else{
            updates.add(new DataStateUpdate(Ca1,0.0));
        }

        if(state.get(Ca2)>0 & state.get(Ca2)<10){
            updates.add(new DataStateUpdate(Ca2,state.get(Ca2)*2));}
        else{
            updates.add(new DataStateUpdate(Ca2,0.0));
        }

        if(state.get(Ca3)>0 & state.get(Ca3)<10){
            updates.add(new DataStateUpdate(Ca3,state.get(Ca3)*2));}
        else {
            updates.add(new DataStateUpdate(Ca3,0.0));
        }

        // new values for X1,X2,X3 - reactons r21,r22,r23
        if(state.get(X1)>0 & state.get(XStar1)==0){
            updates.add(new DataStateUpdate(X1,state.get(X1)));}
        else {
            updates.add(new DataStateUpdate(X1,0.0));
        }

        if(state.get(X2)>0 & state.get(XStar2)==0){
            updates.add(new DataStateUpdate(X2,state.get(X2)));}
        else {
            updates.add(new DataStateUpdate(X2,0.0));
        }

        if(state.get(X3)>0 & state.get(XStar3)==0){
            updates.add(new DataStateUpdate(X3,state.get(X3)));}
        else {
            updates.add(new DataStateUpdate(X3,0.0));
        }

        // new values for Ve1,Ve2,Ve3 - reactons r31,r32,r33 and r61,r62,r63

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



        // new values for XStar1,XStar2,XStar3 - reactons r41,r42,r43
        if(state.get(Ca1)>=10 & state.get(X1)>=10){
            updates.add(new DataStateUpdate(XStar1,state.get(X1)));
        }
        else{
            updates.add(new DataStateUpdate(XStar1,0.0));
        }

        if(state.get(Ca2)>=10 & state.get(X2)>=10){
            updates.add(new DataStateUpdate(XStar2,state.get(X2)));
        }
        else{
            updates.add(new DataStateUpdate(XStar2,0.0));
        }

        if(state.get(Ca3)>=10 & state.get(X3)>=10){
            updates.add(new DataStateUpdate(XStar3,state.get(X3)));
        }
        else{
            updates.add(new DataStateUpdate(XStar3,0.0));
        }

        // new values for VeStar1,VeStar2,VeStar3 - reactons r51,r52,r53
        if(state.get(XStar1)>=10 & state.get(Ve1)>0){
            updates.add(new DataStateUpdate(VeStar1,state.get(Ve1)));
            updates.add(new DataStateUpdate(X1,10.0));
        }
        else{
            updates.add(new DataStateUpdate(VeStar1,0.0));
        }


        // new values for T1,T2,T3 - rules r61,r62,r63
        if(state.get(VeStar1)>0){
            updates.add(new DataStateUpdate(T1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T1,0.0));
        }

        if(state.get(VeStar2)>0){
            updates.add(new DataStateUpdate(T2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T2,0.0));
        }

        if(state.get(VeStar3)>0){
            updates.add(new DataStateUpdate(T3,1.0));
        }
        else{
            updates.add(new DataStateUpdate(T3,0.0));
        }

        // new values for c1,c2 - reactions r17,r27,r19,r29
        if((state.get(c1)>0 & state.get(T3)==0)||state.get(o1)>0){
            updates.add(new DataStateUpdate(c1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c1,0.0));
        }

        if((state.get(c2)>0 & state.get(T3)==0)||state.get(o2)>0){
            updates.add(new DataStateUpdate(c2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(c2,0.0));
        }

        // new values of o1,o2 - reactions r18-r28
        if(state.get(T3)>0){
            updates.add(new DataStateUpdate(o1,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o1,0.0));
        }

        if(state.get(T3)>0){
            updates.add(new DataStateUpdate(o2,1.0));
        }
        else{
            updates.add(new DataStateUpdate(o2,0.0));
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
        initialValues.put(c3,1.0);
        initialValues.put(o3,0.0);

        return new DataState(NUMBER_OF_VARIABLES,i -> initialValues.getOrDefault(i, Double.NaN));
    }


}