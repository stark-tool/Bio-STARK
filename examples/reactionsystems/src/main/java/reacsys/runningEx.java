package reacsys;

import stark.*;
import stark.controller.*;
import stark.distance.*;
import stark.ds.*;
import org.apache.commons.math3.random.RandomGenerator;
import stark.perturbation.AtomicPerturbation;
import stark.perturbation.IterativePerturbation;
import stark.perturbation.Perturbation;
import stark.perturbation.SequentialPerturbation;
import stark.robtl.AtomicRobustnessFormula;
import stark.robtl.RobustnessFormula;
import stark.robtl.ThreeValuedSemanticsVisitor;
import stark.robtl.TruthValues;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.*;

public class runningEx {

    public static final int a = 0;
    public static final int b = 1;
    public static final int c = 2;
    public static final int d = 3;

    private static final int NUMBER_OF_VARIABLES = 4;

    public static void main(String[] args) {

        try {

            Controller controller = new NilController();

            DataState initialState = getInitialState();

            RandomGenerator rand = new DefaultRandomGenerator();

            SystemState system = new ControlledSystem(controller, (rg, ds) -> ds.apply(applyReactions(rg, ds)), initialState);

            int N = 10; // number of steps simulated
            int size = 1; // size of evolution sequences, i.e. cardinality of sample sets (put to 1 as there is no randomness/nondeterminism in the running example)

            ArrayList<DataStateExpression> F = new ArrayList<>();
            ArrayList<String> L = new ArrayList<>();
            L.add("       a   ");
            L.add("     b     ");
            L.add("    c    ");
            L.add("    d  ");
            F.add(ds -> ds.get(a));
            F.add(ds -> ds.get(b));
            F.add(ds -> ds.get(c));
            F.add(ds -> ds.get(d));

            printData(rand, L, F, system, N, size);

        }

        catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    // INITIAL DATA STATE

    private static DataState getInitialState() {
        Map<Integer, Double> initialValues = new HashMap<>();

        initialValues.put(a, 1.0);
        initialValues.put(b, 0.0);
        initialValues.put(c, 1.0);
        initialValues.put(d, 1.0);

        return new DataState(NUMBER_OF_VARIABLES, i -> initialValues.getOrDefault(i, Double.NaN));
    }

    /*- The method <code>applyReactions</code> maps a random generator <code>rg</code> and a data state <code>ds</code>
    to a list of "updates", which are instances of class <code>DataStateUpdate</code>. Essentially, these updates
    will be used to update the data state <ds> so that the new data state is the one available at the next instant
    after the reactions are applied on <code>ds</code> at present instant.
    */
    private static List<DataStateUpdate> applyReactions(RandomGenerator rg, DataState state) {
        List<DataStateUpdate> updates = new LinkedList<>();

        // auxiliary variables used to ensure the no permanency principle

        double new_a = 0.0;
        double new_b = 0.0;
        double new_c = 0.0;
        double new_d = 0.0;

        // modelling reaction a_1
        if (state.get(a) * state.get(d)==1.0 && state.get(b)==0.0) { // enabling predicate of a_1
            new_a = 1.0;
            new_b = 1.0; // products of a_1
        }
        // modelling reaction a_2
        if (state.get(b)==1.0 & state.get(c)==0.0) { // enabling predicate of a_2
            new_a = 1.0;
            new_d = 1.0; // products of a_2
        }
        updates.add(new DataStateUpdate(a,new_a));
        updates.add(new DataStateUpdate(b,new_b));
        updates.add(new DataStateUpdate(c,new_c));
        updates.add(new DataStateUpdate(d,new_d)); // the updates assign the result of the evaluation of res_A on the current data state to the next data state

        return (updates);

    }


    private static void printData(RandomGenerator rg, ArrayList<String> label, ArrayList<DataStateExpression> F, SystemState s, int steps, int size) {
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







}
