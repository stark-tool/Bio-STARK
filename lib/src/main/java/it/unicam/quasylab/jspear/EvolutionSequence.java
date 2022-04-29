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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class represent a collection of sequences of data sampled from a model described in terms
 * of a {@link DataStateFunction}.
 */
public class EvolutionSequence {

    protected         SampleSet<SystemState>              lastGenerated;
    private final   ArrayList<SampleSet<SystemState>>   sequence;
    private final   RandomGenerator                     rg;
    private final   SimulationMonitor                   monitor;

    /**
     * Creates an evolution sequence generated by the given model and.
     *
     *
     */
    public EvolutionSequence(SimulationMonitor monitor, RandomGenerator rg, Function<RandomGenerator, SystemState> generator, int size) {
        this.lastGenerated = SampleSet.generate(rg, generator, size);
        this.sequence = new ArrayList<>();
        this.rg = rg;
        this.monitor = monitor;
        this.sequence.add(lastGenerated);
    }


    /**
     * Creates an evolution sequence generated by the given model and.
     *
     *
     */
    protected EvolutionSequence(SimulationMonitor monitor, RandomGenerator rg, List<SampleSet<SystemState>> sequence, SampleSet<SystemState> lastGenerated) {
        this.lastGenerated = lastGenerated;
        this.sequence = new ArrayList<>(sequence);
        this.rg = rg;
        this.monitor = monitor;
        this.sequence.add(lastGenerated);
    }

    protected EvolutionSequence(EvolutionSequence originalSequence, int lastIndex) {
        this(originalSequence.monitor, originalSequence.rg, originalSequence.subSequence(lastIndex), originalSequence.get(lastIndex));
    }

    protected List<SampleSet<SystemState>> subSequence(int to) {
        if (to<0) {
            return List.of();
        }
        return this.sequence.stream().limit(to).toList();
    }


    /**
     * Returns the length of the evolution sequence.
     *
     * @return the length of the evolution sequence.
     */
    public int length() {
        return sequence.size();
    }

    /**
     * Returns the sample set at the given step.
     *
     * @param i step index.
     * @return the sample set at the given step.
     * @throws IndexOutOfBoundsException if <code>((i<0)||(i>=length()))</code>.
     */
    public SampleSet<SystemState> get(int i) {
        if (getLastGeneratedStep()<i) {
            generateUpTo(i);
        }
        return sequence.get(i);
    }

    private int getLastGeneratedStep() {
        return sequence.size()-1;
    }

    /**
     * This method is used to generate the evolution sequence up to the given index.
     *
     * @param n index of the last generated samplings.
     */
    public synchronized void generateUpTo(int n) {
        while (getLastGeneratedStep()<n) {
            int lastGeneratedStep = getLastGeneratedStep();
            startSamplinngsOfStep(lastGeneratedStep);
            doAdd( generateNextStep() );
            endSamplingsOfStep(lastGeneratedStep);
        }
    }

    protected void doAdd(SampleSet<SystemState> sampling) {
        lastGenerated = sampling;
        sequence.add(lastGenerated);
        System.out.println(lastGenerated.size());
    }

    protected SampleSet<SystemState> generateNextStep() {
        return lastGenerated.apply(s -> s.sampleNext(rg));
    }

    private void endSamplingsOfStep(int j) {
        if (monitor != null) {
            monitor.endSamplingsOfStep(j);
        }
    }

    private void startSamplinngsOfStep(int j) {
        if (monitor != null) {
            monitor.startSamplingsOfStep(j);
        }
    }



    /**
     * Returns the evaluation of the given penalty function at the given time step.
     *
     * @return the evaluation of the given penalty function at the given time step.
     */
    public double[] evalPenaltyFunction(DataStateExpression f, int t) {
        return get(t).evalPenaltyFunction(f);
    }

    protected RandomGenerator getRandomGenerator() {
        return rg;
    }

    public EvolutionSequence apply(Perturbation perturbation, int from, int size) {
        if (from<0) {
            throw new IllegalArgumentException();
        }
        return new PerturbedEvolutionSequence(this.monitor, this.rg, subSequence(from-1), get(from).replica(size), perturbation);
    }

}
