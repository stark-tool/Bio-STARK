package reacsys;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import org.apache.commons.math3.random.RandomGenerator;
import stark.*;
import stark.controller.Controller;
import stark.controller.NilController;
import stark.ds.DataState;

public class Main {
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
}