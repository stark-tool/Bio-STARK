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

package it.unicam.quasylab.jspear;

import it.unicam.quasylab.jspear.distance.DistanceExpression;
import it.unicam.quasylab.jspear.ds.DataStateExpression;
import it.unicam.quasylab.jspear.perturbation.Perturbation;
import it.unicam.quasylab.jspear.robtl.RobustnessFormula;
import it.unicam.quasylab.jspear.robtl.RobustnessFunction;
import it.unicam.quasylab.jspear.robtl.TruthValues;

import java.util.Map;
import java.util.stream.IntStream;

public class SystemSpecification {

    private final static int DEFAULT_SIZE = 50;

    private final ControlledSystem system;

    private final Map<String, DataStateExpression> penalties;

    private final Map<String, RobustnessFormula> formulas;
    private EvolutionSequence sequence;
    private int size = DEFAULT_SIZE;

    private final Map<String, Perturbation> perturbations;

    private final Map<String, DistanceExpression> expressions;

    private int m = 50;
    private double z = 1.96;

    public SystemSpecification(ControlledSystem system, Map<String, DataStateExpression> penalties, Map<String, RobustnessFormula> formulas, Map<String, Perturbation> perturbations, Map<String, DistanceExpression> expressions) {
        this.system = system;
        this.penalties = penalties;
        this.formulas = formulas;
        this.perturbations = perturbations;
        this.expressions = expressions;
    }

    public String[] getPenalties() {
        return penalties.keySet().toArray(new String[0]);
    }

    public DataStateExpression getPenalty(String name) {
        return penalties.get(name);
    }

    public ControlledSystem getSystem() {
        return system;
    }

    public String[] getFormulas() { return formulas.keySet().toArray(new String[0]); }

    public RobustnessFormula getFormula(String name) {
        return formulas.get(name);
    }

    public Perturbation getPerturbation(String name) {
        return perturbations.get(name);
    }

    public String[] getPerturbations() {
        return perturbations.keySet().toArray(new String[0]);
    }

    public DistanceExpression getDistanceExpression(String name) {
        return expressions.get(name);
    }

    public String[] getDistanceExpressions() {
        return expressions.keySet().toArray(new String[0]);
    }

    public void generateSequence() {
        this.sequence = new EvolutionSequence(new DefaultRandomGenerator(), rg -> system, this.size);
    }

    public void setSize(int size) {
        this.size = size;
    }


    private <T> T eval(RobustnessFunction<T> evaluationFunction, int sampleSize, int step) {
        return evaluationFunction.eval(sampleSize, step, getSequence());
    }

    private <T> void eval(RobustnessFunction<T> evaluationFunction, int sampleSize, int from, int by, T[] data) {
        for(int i=0; i<data.length; i++) {
            data[i] = eval(evaluationFunction, sampleSize, from+by*i);
        }
    }

    public boolean evalBooleanSemantic(String name, int sampleSize, int step) {
        RobustnessFormula formula = getFormula(name);
        if (formula == null) {
            return false;
        }
        return eval(RobustnessFormula.getBooleanEvaluationFunction(formula), sampleSize, step);
    }

    public Boolean[] evalBooleanSemantic(String name, int sampleSize, int from, int to, int by) {
        RobustnessFormula formula = getFormula(name);
        if (formula == null) {
            return null;
        }
        Boolean[] data = new Boolean[(to-from)/by];
        eval(RobustnessFormula.getBooleanEvaluationFunction(formula), sampleSize, from, by, data);
        return data;
    }


    public TruthValues evalThreeValuedSemantic(String name, int sampleSize, int step) {
        RobustnessFormula formula = getFormula(name);
        if (formula == null) {
            return TruthValues.FALSE;
        }
        return eval(RobustnessFormula.getThreeValuedEvaluationFunction(m, z, formula), sampleSize, step);
    }

    public TruthValues[] evalThreeValuedSemantic(String name, int sampleSize, int from, int to, int by) {
        RobustnessFormula formula = getFormula(name);
        if (formula == null) {
            return null;
        }
        TruthValues[] data = new TruthValues[(to-from)/by];
        eval(RobustnessFormula.getThreeValuedEvaluationFunction(m, z, formula), sampleSize, from, by, data);
        return data;
    }

    public EvolutionSequence getSequence() {
        if (this.sequence == null) {
            generateSequence();
        }
        return this.sequence;
    }

    public int getSize() {
        return size;
    }

    public int getM() {
        return m;
    }

    public double getZ() {
        return z;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public SampleSet<SystemState> getSamplesAt(int step) {
        return getSequence().get(step);
    }

    public EvolutionSequence applyPerturbation(String name, int step, int scale, int deadline) {
        EvolutionSequence perturbed = getSequence().apply(getPerturbation(name), step, scale);
        perturbed.generateUpTo(deadline);
        return perturbed;
    }

    public double evalDistanceExpression(String expressionName, String perturbationName, int step, int scale) {
        EvolutionSequence perturbed = getSequence().apply(getPerturbation(perturbationName), step, scale);
        DistanceExpression expr = getDistanceExpression(expressionName);
        return expr.compute(step, getSequence(), perturbed);
    }

    public double[] evalDistanceExpression(String expressionName, String perturbationName, int perturbationStep, int scale, int[] steps) {
        EvolutionSequence perturbed = getSequence().apply(getPerturbation(perturbationName), perturbationStep, scale);
        DistanceExpression expr = getDistanceExpression(expressionName);
        return IntStream.of(steps).mapToDouble(i -> expr.compute(i, getSequence(), perturbed)).toArray();
    }

    public double[] evalPenalty(int step, String name) {
        DataStateExpression f = penalties.get(name);
        if (f == null) {
            return new double[0];
        } else {
            return getSequence().get(step).evalPenaltyFunction(f);
        }
    }

    public double[][] evalPenalties(int[] steps, String name) {
        DataStateExpression f = penalties.get(name);
        if (f == null) {
            return new double[0][0];
        } else {
            return IntStream.of(steps).sequential().mapToObj(i -> getSequence().get(i).evalPenaltyFunction(f)).toArray(double[][]::new);
        }
    }

}
