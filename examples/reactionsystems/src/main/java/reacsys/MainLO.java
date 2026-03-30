package reacsys;

import org.apache.commons.math3.random.RandomGenerator;
import stark.ds.DataState;
import stark.ds.DataStateUpdate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainLO {

    /*
    The LAC OPERON was modeled in Reaction System in the following paper:
    Luca Corolli, Carlo Maja, Fabrizio Marini, Daniela Besozzi, Giancarlo Mauri:
    An excursion in reaction systems: From computer science to biology.
    Theoretical Computer Science 454(2012) 95-108.

    We start with the list of variables that are used to track the status of the system.
     */

    public static final int lac = 0;
    public static final int Z = 1; // enzyme
    public static final int Y = 2; // transporter
    public static final int A = 3; // enzyme
    public static final int lacI = 4; // gene encoding repressor protein
    public static final int I = 5; // represson protein
    public static final int IOP = 6; // repressor bounded to operator
    public static final int cya = 7; // gene encoding protein CAP
    public static final int cAMP = 8; // signal molecule
    public static final int crp = 9; // gene encoding signal molecule cAMP
    public static final int CAP = 10; // protein
    public static final int cAMPCAP= 11; // complex CAP - cAMP
    public static final int lactose = 12;
    public static final int glucose = 13;

    private static final int NUMBER_OF_VARIABLES = 14;









    private static List<DataStateUpdate> applyReactions(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();

        // reaction r1 - lac operon duplication
        if(state.get(lac)==1){
            updates.add(new DataStateUpdate(lac,1));
        }
        else{
            updates.add(new DataStateUpdate(lac,0));
        }

        // reaction r2 - repressor gene duplication
        if(state.get(lacI)==1){
            updates.add(new DataStateUpdate(lacI,1));
        }
        else{
            updates.add(new DataStateUpdate(lacI,0));
        }

        // reaction r3 - repressor gene expression
        if(state.get(lacI)==1){
            updates.add(new DataStateUpdate(I,1));
        }
        else{
            updates.add(new DataStateUpdate(I,0));
        }

        // reaction r4 - regulation mediated by lactose
        if(state.get(I)==1 & state.get(lactose)==0){
            updates.add(new DataStateUpdate(IOP,1));
        }
        else{
            updates.add(new DataStateUpdate(IOP,0));
        }

        // reaction r5 - gene encoding signal molecule cAMP duplication
        if(state.get(cya)==1){
            updates.add(new DataStateUpdate(cya,1));
        }
        else{
            updates.add(new DataStateUpdate(cya,0));
        }

        // reaction r6 - gene encoding signal molecule cAMP expression
        if(state.get(cya)==1){
            updates.add(new DataStateUpdate(cAMP,1));
        }
        else{
            updates.add(new DataStateUpdate(cAMP,0));
        }

        // reaction r7 - gene encoding protein CAP duplication
        if(state.get(crp)==1){
            updates.add(new DataStateUpdate(crp,1));
        }
        else{
            updates.add(new DataStateUpdate(crp,0));
        }

        // reaction r8 - gene encoding protein CAP expression
        if(state.get(crp)==1){
            updates.add(new DataStateUpdate(CAP,1));
        }
        else{
            updates.add(new DataStateUpdate(CAP,0));
        }

        // reaction r9 - regulation mediated by lactose
        if(state.get(cAMP)==1 & state.get(CAP)==1 & state.get(glucose)==0){
            updates.add(new DataStateUpdate(cAMPCAP,1));
        }
        else{
            updates.add(new DataStateUpdate(cAMPCAP,0));
        }

        // reaction r10 - lac operon expression
        if(state.get(cAMPCAP)==1 & state.get(lac)==1 & state.get(IOP)==0){
            updates.add(new DataStateUpdate(Z,1));
            updates.add(new DataStateUpdate(Y,1));
            updates.add(new DataStateUpdate(A,1));
        }
        else{
            updates.add(new DataStateUpdate(Z,0));
            updates.add(new DataStateUpdate(Y,0));
            updates.add(new DataStateUpdate(A,0));
        }

        return updates;
    }


    // Method <code>getInitialState</code> assigns the initial value to all 14 variables.
    // To reproduce the same results as in Corolli et al. we start with ....
    // .....
    // .....
    // Technically, the metrod returns an instance of <code>DataState</code>
    private static DataState getInitialState() {
        Map<Integer, Double> initialValues = new HashMap<>();
        initialValues.put(lac, 0.0);
        initialValues.put(Z, 0.0);
        initialValues.put(Y, 0.0);
        initialValues.put(A, 0.0);
        initialValues.put(lac, 0.0);
        initialValues.put(I, 0.0);
        initialValues.put(IOP, 0.0);
        initialValues.put(cya, 0.0);
        initialValues.put(cAMP, 0.0);
        initialValues.put(crp, 0.0);
        initialValues.put(CAP, 0.0);
        initialValues.put(cAMPCAP, 0.0);
        initialValues.put(lactose, 0.0);
        initialValues.put(glucose, 0.0);
        return new DataState(NUMBER_OF_VARIABLES, i -> initialValues.getOrDefault(i, Double.NaN));
    }




}
