/*
 * JSpear: a SimPle Environment for statistical estimation of Adaptation and Reliability.
 *
 *              Copyright (C) 2020.
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

package it.unicam.quasylab.jspear.speclang;

import it.unicam.quasylab.jspear.*;
import it.unicam.quasylab.jspear.ds.DataState;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.robtl.RobustnessFormula;
import it.unicam.quasylab.jspear.robtl.TruthValues;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationLoaderTest {

    public static final String RANDOM_WALK = "./RandomWalk.jspec";
    public static final String ENGINE = "./Engine.jspec";
    public static final String VEHICLE = "./Vehicle.jspec";

    @Test
    @Disabled
    void loadRandomWalkSpecification() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(RANDOM_WALK)).openStream());
        assertNotNull(spec);
    }

    @Test
    @Disabled
    void loadEngineSpecification() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(ENGINE)).openStream());
        assertNotNull(spec);
    }

    @Test
    void loadVehicleSpecification() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        assertNotNull(spec);
    }

    @Test
    void loadVehicleSystem() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        ControlledSystem system = spec.getSystem();
        assertNotNull(system);
    }

    @Test
    void simulateVehicleSystem() throws IOException {
            SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        assertNotNull(spec.getSamplesAt(50));
    }

    @Test
    void stepVehicleSystem() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        ControlledSystem system = spec.getSystem();
        assertNotNull(system.sampleNext(new DefaultRandomGenerator()));
    }


    @Test
    void loadVehicleProperty() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        RobustnessFormula formula = spec.getFormula("phi_crash_speed");
        assertNotNull(formula);
    }

    @Test
    @Disabled
    void loadSlowProperty() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        RobustnessFormula formula = spec.getFormula("phi_slow");
        assertNotNull(formula);
    }

    @Test
    @Disabled
    void loadSample() throws IOException{
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setM(1);
        ControlledSystem system = spec.getSystem();
        EvolutionSequence sequence = spec.getSequence();
        DataStateExpression f = spec.getPenalty("rho_crash_speed");
        SampleSet sample = spec.getSamplesAt(300);
        double[] gino = sample.evalPenaltyFunction(f);
        for (int i = 0; i<gino.length; i++){
            System.out.println(gino[i]);
        }
        double[] data = SystemState.sample(new DefaultRandomGenerator(), f, system, 300, 1);
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%d> %f\n", i, data[i]);
        }
    }


    @Test
    @Disabled
    void vehicleEvalPenaltyFunction() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        DataStateExpression f = spec.getPenalty("rho_crash");
        assertEquals(0, f.eval(spec.getSystem().getDataState()));
    }


    @Test
    void testApplyPerturbation() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        assertNotNull(spec.applyPerturbation("p_ItSlow_02", 0, 60, 400));
    }

    @Test
    void testEvalDistance() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        assertTrue(spec.evalDistanceExpression("exp_crash", "p_ItSlow_03", 350, 60)>0);
    }

    @Test
    void vehicleSlow02x10BoolCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        Boolean[] expected = new Boolean[10];
        Arrays.fill(expected,true);
        assertArrayEquals(expected, spec.evalBooleanSemantic("phi_slow_02", 60, 0,100,10));
    }

    @Test
    void vehicleSlow02x30BoolCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        Boolean[] expected = new Boolean[10];
        Arrays.fill(expected,true);
        assertArrayEquals(expected, spec.evalBooleanSemantic("phi_slow_02", 60, 0,300,30));
    }

    @Test
    void vehicleSlow02x50BoolCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        Boolean[] expected = new Boolean[10];
        Arrays.fill(expected,true);
        assertArrayEquals(expected, spec.evalBooleanSemantic("phi_slow_02", 60, 0,500,50));
    }

    @Test
    void vehicleComb04x10ThreeValCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(50);
        spec.setZ(1.96);
        TruthValues[] expected = new TruthValues[10];
        Arrays.fill(expected,TruthValues.FALSE);
        assertArrayEquals(expected, spec.evalThreeValuedSemantic("phi_comb_04", 60, 0,100,10));
    }

    @Test
    void vehicleComb04x30ThreeValCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(50);
        spec.setZ(1.96);
        TruthValues[] expected = new TruthValues[10];
        Arrays.fill(expected,TruthValues.FALSE);
        assertArrayEquals(expected, spec.evalThreeValuedSemantic("phi_comb_04", 60, 0,300,30));
    }


    @Test
    void vehicleComb04x50ThreeValCheck() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(50);
        spec.setZ(1.96);
        TruthValues[] expected = {TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.FALSE, TruthValues.TRUE, TruthValues.TRUE};
        assertArrayEquals(expected, spec.evalThreeValuedSemantic("phi_comb_04", 60, 0,500,50));
    }

    @Test
    void vehicleSlowOffset03() throws IOException{
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(50);
        spec.setZ(1.96);
        assertEquals(TruthValues.UNKNOWN, spec.evalThreeValuedSemantic("phi_slow_03", 60,0));
    }

    @Test
    void vehicleCombOffset03() throws IOException{
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(50);
        spec.setZ(1.96);
        assertEquals(TruthValues.UNKNOWN, spec.evalThreeValuedSemantic("phi_comb_03", 60,0));
    }

    @Test
    void vehicleCrashThreeValuedCheck005x10() throws IOException {
        SpecificationLoader loader = new SpecificationLoader();
        SystemSpecification spec = loader.loadSpecification(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(VEHICLE)).openStream());
        spec.setSize(1);
        spec.setM(40);
        spec.setZ(1.96);
        TruthValues[] expected = new TruthValues[10];
        Arrays.fill(expected,TruthValues.FALSE);
        assertArrayEquals(expected, spec.evalThreeValuedSemantic("phi_crash_speed", 60, 0,100,10));
    }


}