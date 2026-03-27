package reacsys;

import org.apache.commons.math3.random.RandomGenerator;
import stark.ControlledSystem;
import stark.DefaultRandomGenerator;
import stark.SystemState;
import stark.controller.Controller;
import stark.controller.ControllerRegistry;
import stark.controller.NilController;
import stark.controller.ParallelController;
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

    public static void main(String[] args){

        /*
            INITIAL CONFIGURATION
            In order to perform simulations/analysis/model checking for a particular system, we need to create its
            initial configuration, which is an instance of <code>ControlledSystem>/code>
            */


            /*
            One of the elements of a system configuration is the "controller", i.e. an instance of <code>Controller</code>.
            In this example we use the controller named <code>context</code> that is returned by static method
            <code>getController</code>. Essentially, the controller implements contexts of the Reaction System
            */

        Controller context = getController();


        /*
            Another element of a system configuration is the "data state", i.e. an instance of <code>DataState</code>,
            which models the state of the data. Instances of <code>DataState</code> contains values for variables
            representing the quantities of the system.
            The initial state <code>initialState</code> is constructed by exploiting the static method
            <code>getInitialState</code>, which will be defined later and assigns the initial value to all 28
            variables defined above.
             */
        DataState initialState = getInitialState();



        /*
            In order to model probabilistic evolution, a system configuration needs a random generator.
             */
        RandomGenerator rand = new DefaultRandomGenerator();

            /*
            We define the <code>ControlledSystem</code> <code>system</code>, which will be the starting configuration from
            which the evolution sequence will be constructed.
            This configuration consists of 3 elements:
            - the controller <code>controller</code> defined above,
            - a random function over data states, which implements interface <code>DataStateFunction</code> and maps a
            random generator <code>rg</code> and a data state <code>ds</code> to the data state obtained by updating
            <code>ds</code> with the list of changes given by method <code>applyReactions</code>. Essentially,
            this static method, defined later, applies the reactions that are promoted/inhibited by the entities in
            <code>ds</code> and produces new entities, which will be available at next instant.
            - the data state <code>initialState</state> defined above,
             */
        SystemState system = new ControlledSystem(context, (rg, ds) -> ds.apply(applyReactions(rg, ds)), initialState);


    }






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

    public static Controller getController() {
        ControllerRegistry registry = getControllerRegistry();
        return new ParallelController(registry.reference("Ctrl"), registry.reference("IDS"));
    }

    public static ControllerRegistry getControllerRegistry() {
        ControllerRegistry registry = new ControllerRegistry();
        registry.set("DefaultCondition",
                Controller.doAction(
                        (rg,ds)->List.of(
                                new DataStateUpdate(lac,1.0),
                                new DataStateUpdate(lacI,1.0),
                                new DataStateUpdate(I,1.0),
                                new DataStateUpdate(cya,1.0),
                                new DataStateUpdate(cAMP,1.0),
                                new DataStateUpdate(crp,1.0),
                                new DataStateUpdate(CAP,1.0)),
                        registry.reference("DefaultCondition")
                )
        );
        registry.set("Glucose5",
                Controller.doAction((rg,ds)->List.of(
                        new DataStateUpdate(glucose,1.0)),
                        registry.reference("Glucose4")
                )
        );
        registry.set("Glucose4",
                Controller.doAction((rg,ds)->List.of(
                                new DataStateUpdate(glucose,1.0)),
                        registry.reference("Glucose3")
                )
        );
        registry.set("Glucose3",
                Controller.doAction((rg,ds)->List.of(
                                new DataStateUpdate(glucose,1.0)),
                        registry.reference("Glucose2")
                )
        );
        registry.set("Glucose2",
                Controller.doAction((rg,ds)->List.of(
                                new DataStateUpdate(glucose,1.0)),
                        registry.reference("Glucose1")
                )
        );
        registry.set("Glucose1",
                Controller.doAction((rg,ds)->List.of(
                                new DataStateUpdate(glucose,1.0)),
                        registry.reference("Glucose0")
                )
        );



        )

        return registry;
    }




}
